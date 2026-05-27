package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.dto.ProductWeightLogRequest;
import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.repository.ProductWeightLogRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductWeightLogService {

    public static final String BEHAVIOR_QUEUE_KEY = "behavior:queue";

    private final ProductWeightLogRepository productWeightLogRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    //log-write
    private final WeightingProperties weightProps;
    private final MetaTagWeightLogService metaTagWeightLogService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    //기록 - request 전체 받아 분할함
    @Transactional
    public void recordLogs(ProductWeightLogRequest request) {
        Long memberId = resolveMemberId();
        Long productId = request.getProductId();

        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다");
        }


        WeightLogType action = request.getActionType();
        if (action == null) {
            return;
        }

        //VIEW: 24시간 기준으로 redis 기존 key 확인, 있다면 돌려보낸다.
        if (action == WeightLogType.VIEW) {
            String redisKey = "view:dedup:%d:%d".formatted(memberId, productId);
            Boolean isFirst = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "1", weightProps.getView().getDedup());
            if (Boolean.FALSE.equals(isFirst)) return;
        }

        // VIEW일 경우 10초를 기준으로 확인
        // 2차에서 한 차례 더 필터링
        if (action == WeightLogType.VIEW
                && (request.getStayDuration() == null || request.getStayDuration() < 10000)) {
            return;
        }

        if (action == WeightLogType.BOOKMARK){
           //ZADD - 북마크 추가
            redisTemplate.opsForZSet().add("bookmark:pending", memberId + ":" + productId, System.currentTimeMillis());
            return;
        }

        if(action == WeightLogType.CANCEL_BOOKMARK) {
            //ZADD 취소 처리(확정 전)
            redisTemplate.opsForZSet().remove("bookmark:pending", memberId + ":" + productId);

            //확정 후는 이쪽으로 처리
            cancelBookmark(memberId, productId);
            return;
        }

        Integer weight = weightProps.getActionWeight().get(action);
        LocalDateTime now = LocalDateTime.now();

        ProductWeightLog log = ProductWeightLog.builder()
                .memberId(memberId)
                .productId(productId)
                .actionType(action)
                .appliedWeight(weight)
                .referenceId(request.getReferenceId())
                .eventTimeStamp(request.getEventTimeStamp() != null ? request.getEventTimeStamp() : now)
                .createdAt(now)
                .build();

        //try-catch문을 이쪽에서 실행시킵니다
        //json처리
        String json;
        try {
            json = objectMapper.writeValueAsString(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("로그 등록에 문제가 생겼습니다.", e);
        }
        redisTemplate.opsForList().leftPush(BEHAVIOR_QUEUE_KEY, json);

        //metaTagWeightLog에 정보를 인자로 넘김
        metaTagWeightLogService.recordFromProduct(memberId, productId, action, request.getReferenceId());
    }

    //메서드 분리로 안정성 강화
    @Transactional
    public int persistBehaviorLogsFromQueue(int max) {
        int count = 0;
        for (int i = 0; i < max; i++) {
            //큐에 등재된 behavior
            String json = redisTemplate.opsForList().rightPop(BEHAVIOR_QUEUE_KEY);
            if (json == null) return count;

            try {
                ProductWeightLog log = objectMapper.readValue(json, ProductWeightLog.class);
                productWeightLogRepository.save(log);
                metaTagWeightLogService.recordFromProduct(
                        log.getMemberId(),
                        log.getProductId(),
                        log.getActionType(),
                        log.getReferenceId());
                count++;
            } catch (JsonProcessingException e) {
                //실패 시 다음으로 넘어간다
                log.error("배치 처리 실패: {}", json, e);
            }

        }
        return count;
    }

    //bookmark 1분 처리
    public void confirmBookmarkPending(){
        Long now = System.currentTimeMillis();
        Long cutoff = now - weightProps.getBookmark().getConfirmDelay().toMillis();
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .rangeByScoreWithScores("bookmark:pending", 0, cutoff);
        if(tuples == null || tuples.isEmpty()) return;

        for(ZSetOperations.TypedTuple<String> tuple : tuples) {
            //productId 및 memberId 사용
            String member = tuple.getValue();
            double score = tuple.getScore();
            String[] parts = member.split(":");
            Long memberId = Long.parseLong(parts[0]);
            Long productId = Long.parseLong(parts[1]);

            ProductWeightLog log = ProductWeightLog.builder()
                    .memberId(memberId)
                    .productId(productId)
                    .actionType(WeightLogType.BOOKMARK)
                    .appliedWeight(weightProps.getActionWeight().get(WeightLogType.BOOKMARK))
                    .referenceId(null)
                    .eventTimeStamp(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli((long) score), ZoneId.systemDefault()))
                    .createdAt(LocalDateTime.now())
                    .build();
            String json;
            try {
                json = objectMapper.writeValueAsString(log);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("로그 등록에 문제가 생겼습니다.", e);
            }
            redisTemplate.opsForList().leftPush(BEHAVIOR_QUEUE_KEY, json);

            metaTagWeightLogService.recordFromProduct(memberId, productId, WeightLogType.BOOKMARK, null);
        }

        //ZSet 삭제
        redisTemplate.opsForZSet().removeRangeByScore("bookmark:pending", 0, cutoff);

    }

    public void cancelBookmark(Long memberId, Long productId){
        // 결제 이력이 존재한다면 북마크에 의한 가중치 롤백 없음
        if (productWeightLogRepository.existsByMemberIdAndProductIdAndActionType(
                memberId, productId, WeightLogType.BUY)) {
            return;
        }

        Optional<Long> bookmarkLogId = productWeightLogRepository
                .findByMemberIdAndProductIdAndActionType(memberId, productId, WeightLogType.BOOKMARK);

        //이력을 찾을 수 없을 경우 오류 처리하지 않고 조용히 무시한다
        if (bookmarkLogId.isEmpty()) {
            return;
        }

        ProductWeightLog log = ProductWeightLog.builder()
                .memberId(memberId)
                .productId(productId)
                .actionType(WeightLogType.CANCEL_BOOKMARK)
                .appliedWeight(weightProps.getActionWeight().get(WeightLogType.CANCEL_BOOKMARK))
                .referenceId(bookmarkLogId.get())
                .eventTimeStamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        //저장 후 종료
        productWeightLogRepository.save(log);
    }

    private Long resolveMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }
        String email = auth.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."))
                .getId();
    }

    
}
