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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductWeightLogService {

    public static final String BEHAVIOR_QUEUE_KEY = "behavior:queue";
    public static final String CART_PENDING_KEY = "cart:pending";

    private final ProductWeightLogRepository productWeightLogRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final MemberTagWeightHotService memberTagWeightHotService;
    //log-write
    private final WeightingProperties weightProps;
    private final MetaTagWeightLogService metaTagWeightLogService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    //기록 - request 전체 받아 분할함
    //TODO: 조회 및 구매에 있어서 재행동할 경우 +@
    //TODO: 다수 구매/취소 처리 관련
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

        //VIEW: 30분 기준으로 redis 기존 key 확인, 있다면 돌려보낸다.
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

        //북마크
        if (action == WeightLogType.BOOKMARK){
           //ZADD - 북마크 추가
            redisTemplate.opsForZSet().add("bookmark:pending", memberId + ":" + productId, System.currentTimeMillis());
            return;
        }

        //북마크 취소 처리
        if(action == WeightLogType.CANCEL_BOOKMARK) {
            //ZADD 취소 처리(확정 전)
            redisTemplate.opsForZSet().remove("bookmark:pending", memberId + ":" + productId);
            //queue 제거 및 DB cancel은 cancelBookmark에서 처리(BUY 이력 있으면 skip)
            cancelBookmark(memberId, productId);
            return;
        }

        if(action == WeightLogType.CANCEL_CART) {
            String cartField = memberId + ":" + productId;

            releaseHotBeforeCancel(memberId, productId, WeightLogType.CART);
            redisTemplate.opsForHash().delete(CART_PENDING_KEY, cartField);
            cancelLog(memberId, productId, WeightLogType.CART, WeightLogType.CANCEL_CART);
            return;
        }

        //결제 취소/환불 처리
        if(action == WeightLogType.CANCEL_BUY) {
            //여러차례의 과정이 필요하고, 가장 빈도가 낮기 때문에 Redis 적재 필요하지 않음
            cancelLog(memberId, productId, WeightLogType.BUY, WeightLogType.CANCEL_BUY);
            return;
        }

        Integer weight = weightProps.getActionWeight().get(action);
        if (weight == null) {
            return;
        }
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

        //구매 처리
        if (action == WeightLogType.BUY) {
            persistProductLogIfNotDuplicate(log);
            return;
        }

        //점수 Redis 처리
        memberTagWeightHotService.increaseFromProduct(memberId, productId, weight);

        //try-catch문을 이쪽에서 실행시킵니다
        //json처리
        String json;
        try {
            json = objectMapper.writeValueAsString(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("로그 등록에 문제가 생겼습니다.", e);
        }

        if(action == WeightLogType.CART) {
            redisTemplate.opsForHash().put(CART_PENDING_KEY, memberId + ":" + productId, json);
            return;
        }

        redisTemplate.opsForList().leftPush(BEHAVIOR_QUEUE_KEY, json);
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
                persistProductLogIfNotDuplicate(log);
                count++;
            } catch (JsonProcessingException e) {
                //실패 시 다음으로 넘어간다
                log.error("배치 처리 실패: {}", json, e);
            }

        }
        return count;
    }

    //CART 처리 관련
    @Transactional
    public int persistCartPendingFromHash(int max) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(CART_PENDING_KEY);
        if (entries.isEmpty()) return 0;

        int count = 0;
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            if (count >= max) break;

            String field = (String) entry.getKey();
            String json = (String) entry.getValue();

            try {
                ProductWeightLog log = objectMapper.readValue(json, ProductWeightLog.class);
                persistProductLogIfNotDuplicate(log);
                redisTemplate.opsForHash().delete(CART_PENDING_KEY, field);
                count++;
            } catch (JsonProcessingException e) {
                log.error("cart pending 배치 처리 실패: {}", json, e);
            }
        }
        return count;
    }

    //bookmark 1분 처리
    public void confirmBookmarkPending(){
        long now = System.currentTimeMillis();
        long cutoff = now - weightProps.getBookmark().getConfirmDelay().toMillis();

        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .rangeByScoreWithScores("bookmark:pending", 0, cutoff);
        if(tuples == null || tuples.isEmpty()) return;

        for(ZSetOperations.TypedTuple<String> tuple : tuples) {
            //productId 및 memberId 사용
            String member = tuple.getValue();
            double bookmarkedAtMs = tuple.getScore();
            String[] parts = member.split(":");
            Long memberId = Long.parseLong(parts[0]);
            Long productId = Long.parseLong(parts[1]);
            Integer weight = weightProps.getActionWeight().get(WeightLogType.BOOKMARK);
            if (weight == null) {
                continue;
            }

            ProductWeightLog log = ProductWeightLog.builder()
                    .memberId(memberId)
                    .productId(productId)
                    .actionType(WeightLogType.BOOKMARK)
                    .appliedWeight(weight)
                    .referenceId(null)
                    .eventTimeStamp(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli((long) bookmarkedAtMs), ZoneId.systemDefault()))
                    .createdAt(LocalDateTime.now())
                    .build();
            String json;
            try {
                json = objectMapper.writeValueAsString(log);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("로그 등록에 문제가 생겼습니다.", e);
            }
            memberTagWeightHotService.increaseFromProduct(memberId, productId, weight);
            redisTemplate.opsForList().leftPush(BEHAVIOR_QUEUE_KEY, json);
        }

        //ZSet 삭제
        redisTemplate.opsForZSet().removeRangeByScore("bookmark:pending", 0, cutoff);

    }

    public void cancelBookmark(Long memberId, Long productId){
        // 결제 이력이 존재한다면 북마크 가중치 유지(queue 제거 및 DB cancel 모두 skip)
        if (productWeightLogRepository.existsByMemberIdAndProductIdAndActionType(
                memberId, productId, WeightLogType.BUY)) {
            return;
        }

        removeBookmarkFromBehaviorQueue(memberId, productId);
        releaseHotBeforeCancel(memberId, productId, WeightLogType.BOOKMARK);
        cancelLog(memberId, productId, WeightLogType.BOOKMARK, WeightLogType.CANCEL_BOOKMARK);
    }

    private void removeBookmarkFromBehaviorQueue(Long memberId, Long productId) {
        List<String> entries = redisTemplate.opsForList().range(BEHAVIOR_QUEUE_KEY, 0, -1);
        if (entries == null || entries.isEmpty()) return;

        for (String json : entries) {
            try {
                ProductWeightLog queued = objectMapper.readValue(json, ProductWeightLog.class);
                if (memberId.equals(queued.getMemberId())
                        && productId.equals(queued.getProductId())
                        && queued.getActionType() == WeightLogType.BOOKMARK) {
                    redisTemplate.opsForList().remove(BEHAVIOR_QUEUE_KEY, 0, json);
                }
            } catch (JsonProcessingException e) {
                log.warn("북마크 json 등록에 실패했습니다: {}", json, e);
            }
        }
    }

    //CANCEL 로그 처리 일괄 메서드로 분리
    private void cancelLog(
            Long memberId,
            Long productId,
            WeightLogType referredAction,
            WeightLogType cancelAction
    ){
        Optional<Long> referredId = productWeightLogRepository
                .findLatestUncancelledId(memberId, productId, referredAction, cancelAction);
        //이력을 찾을 수 없을 경우 오류 처리하지 않고 조용히 무시한다
        if (referredId.isEmpty()) return;

        ProductWeightLog log = ProductWeightLog.builder()
                .memberId(memberId)
                .productId(productId)
                .actionType(cancelAction)
                .appliedWeight(weightProps.getActionWeight().get(cancelAction))
                .referenceId(referredId.get())
                .eventTimeStamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        persistProductLogIfNotDuplicate(log);
    }

    //중복이 아니라면 저장 - 최종 등록 필터
    private void persistProductLogIfNotDuplicate(ProductWeightLog log) {
        LocalDateTime eventTime = log.getEventTimeStamp() != null
                ? log.getEventTimeStamp()
                : LocalDateTime.now();

        boolean consumesHot = consumesHotBuffer(log.getActionType());
        if (isDuplicateAction(
                log.getMemberId(),
                log.getProductId(),
                log.getActionType(),
                eventTime,
                weightProps.getActionDedup())) {
            if (consumesHot) {
                releaseHotBuffer(log);
            }
            return;
        }
        productWeightLogRepository.save(log);
        metaTagWeightLogService.recordFromProduct(log, !consumesHot);
    }

    //VIEW | CART | BOOKMARK인지 확인한다(true/false)
    private boolean consumesHotBuffer(WeightLogType action) {
        return action == WeightLogType.VIEW
                || action == WeightLogType.CART
                || action == WeightLogType.BOOKMARK;
    }

    //중복 건너뛸 때 hot에서 제거한다(decrease)
    private void releaseHotBuffer(ProductWeightLog log) {
        Integer weight = log.getAppliedWeight();
        if (weight == null) {
            return;
        }
        memberTagWeightHotService.decreaseFromProduct(log.getMemberId(), log.getProductId(), weight);
    }

    //취소 시 hot 버퍼가 남아 있으면 회수 (merge 후에는 hot 키 없음 → skip)
    private void releaseHotBeforeCancel(Long memberId, Long productId, WeightLogType referredAction) {
        Integer weight = weightProps.getActionWeight().get(referredAction);
        memberTagWeightHotService.decreaseFromProduct(memberId, productId, weight);
    }

    //중복된 행동인지 체크
    private boolean isDuplicateAction(
            Long memberId,
            Long productId,
            WeightLogType action,
            LocalDateTime eventTimeStamp,
            Duration due
    ) {
        if (eventTimeStamp == null || due == null || due.isZero() || due.isNegative()) {
            return false;
        }
        return productWeightLogRepository
                .findFirstByMemberIdAndProductIdAndActionTypeOrderByEventTimeStampDesc(
                        memberId, productId, action)
                .map(last -> {
                    if (last.getEventTimeStamp() == null) {
                        return false;
                    }
                    return Duration.between(last.getEventTimeStamp(), eventTimeStamp).abs().compareTo(due) < 0;
                })
                .orElse(false);
    }

    private Long resolveMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, DefaultErrorDetailMessages.LOGIN_REQUIRED);
        }
        String email = auth.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."))
                .getId();
    }

    
}
