package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.dto.ProductWeightLogRequest;
import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogStatus;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.repository.ProductWeightLogRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class ProductWeightLogService {

    @Autowired
    ProductWeightLogRepository productWeightLogRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WeightingProperties weightProps;

    @Autowired
    MetaTagWeightLogService metaTagWeightLogService;

    //기록 - request 전체 받아 분할함
    public void recordLogs(ProductWeightLogRequest request) {
        Long memberId = resolveMemberId();

        if (!productRepository.existsById(request.getProductId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다");
        }

        WeightLogType action = request.getActionType();
        if(action == null) {
            return;
        }

        // VIEW일 경우 10초를 기준으로 확인
        // 2차에서 한 차례 더 필터링
        if (action == WeightLogType.VIEW
                && (request.getStayDuration() == null || request.getStayDuration() < 10000)) {
            return;
        }

        Integer weight = weightProps.getActionWeight().get(action);
        LocalDateTime now = LocalDateTime.now();

        ProductWeightLog log = ProductWeightLog.builder()
                .memberId(memberId)
                .productId(request.getProductId())
                .actionType(action)
                .appliedWeight(weight)
                .referenceId(request.getReferenceId())
                .eventTimeStamp(request.getEventTimeStamp() != null ? request.getEventTimeStamp() : now)
                .createdAt(now)
                .status(WeightLogStatus.ACTIVE)
                .build();

        productWeightLogRepository.save(log);

        //metaTagWeightLog에 정보를 인자로 넘김
        metaTagWeightLogService.recordFromProduct(memberId, request.getProductId(), action, request.getReferenceId());
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
