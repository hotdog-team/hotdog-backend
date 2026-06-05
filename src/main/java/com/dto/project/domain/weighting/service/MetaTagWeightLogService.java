package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.repository.MetaTagWeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MetaTagWeightLogService {

    private final MetaTagWeightLogRepository metaTagWeightLogRepository;
    private final MetaTagProductRepository metaTagProductRepository;
    private final MemberRepository memberRepository;
    private final WeightingProperties weightProps;
    private final MemberTagWeightService memberTagWeightService;

    //ProductWeightLog의 값을 사용하여 기록 (applyScore=false면 로그만 등록한다)
    public void recordFromProduct(ProductWeightLog productLog, boolean applyScore) {
        List<MetaTagProduct> mappings = metaTagProductRepository.findByProduct_Id(productLog.getProductId());
        if (mappings.isEmpty()) return;

        Double configured = weightProps.getActionWeight().get(productLog.getActionType());

        double appliedWeight = productLog.getAppliedWeight() != null
                ? productLog.getAppliedWeight()
                : (configured != null ? configured : 0);
        Long memberId = productLog.getMemberId();
        WeightLogType actionType = productLog.getActionType();
        LocalDateTime eventTimeStamp = productLog.getEventTimeStamp();
        LocalDateTime createdAt = LocalDateTime.now();

        List<MetaTagWeightLog> logs = new ArrayList<>();
        for (MetaTagProduct m : mappings) {
            Long metaTagId = m.getMetaTag().getId();
            Long referenceId = resolveReferenceId(memberId, metaTagId, productLog);
            if (referenceId == null) continue;

            double tagWeight = resolveTagWeight(productLog, m, appliedWeight);
            if (tagWeight == 0) continue;

            logs.add(MetaTagWeightLog.builder()
                    .memberId(memberId)
                    .metaTagId(metaTagId)
                    .actionType(actionType)
                    .appliedWeight(tagWeight)
                    .referenceId(referenceId)
                    .eventTimeStamp(eventTimeStamp)
                    .createdAt(createdAt)
                    .build());
        }

        if (logs.isEmpty()) return;
        metaTagWeightLogRepository.saveAll(logs);

        if (!applyScore || appliedWeight == 0) return;

        memberRepository.findById(memberId).ifPresent(member -> {
            for (MetaTagProduct m : mappings) {
                double tagWeight = resolveTagWeight(productLog, m, appliedWeight);
                if (tagWeight == 0) continue;
                memberTagWeightService.applyBehaviorScore(member, m.getMetaTag(), tagWeight);
            }
        });
    }

    // metaTagWeightLog의 referenceId를 구한다
    private Long resolveReferenceId(Long memberId, Long metaTagId, ProductWeightLog productLog) {
        WeightLogType cancelAction = productLog.getActionType();
        WeightLogType referredAction = referredActionFor(cancelAction);
        if (referredAction == null) {
            return productLog.getId();
        }
        Long productReferredLogId = productLog.getReferenceId();
        if (productReferredLogId == null) {
            return null;
        }
        return metaTagWeightLogRepository
                .findLatestUncancelledReferredId(
                        memberId, metaTagId, referredAction, productReferredLogId, cancelAction)
                .orElse(null);
    }

    private static WeightLogType referredActionFor(WeightLogType actionType) {
        return switch (actionType) {
            case CANCEL_BUY -> WeightLogType.BUY;
            case CANCEL_CART -> WeightLogType.CART;
            case CANCEL_BOOKMARK -> WeightLogType.BOOKMARK;
            default -> null;
        };
    }

    // DISLIKE 메타태그 계수 적용 메서드 분리
    private double resolveTagWeight(ProductWeightLog productLog, MetaTagProduct mapping, double defaultWeight) {
        if (productLog.getActionType() != WeightLogType.DISLIKE) {
            return defaultWeight;
        }
        Double dislikeBase = weightProps.getActionWeight().get(WeightLogType.DISLIKE);
        Double coefficient = weightProps.getDislikeCoefficient().get(mapping.getMetaTag().getType());
        if (dislikeBase == null || coefficient == null || coefficient == 0) {
            return 0;
        }
        return dislikeBase * coefficient;
    }
}
