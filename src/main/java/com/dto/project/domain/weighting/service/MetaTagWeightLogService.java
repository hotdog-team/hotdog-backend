package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.repository.MetaTagWeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        if (mappings.isEmpty()) {
            return;
        }

        Integer weight = weightProps.getActionWeight().get(productLog.getActionType());
        Long memberId = productLog.getMemberId();
        LocalDateTime eventTimeStamp = productLog.getEventTimeStamp();
        LocalDateTime createdAt = LocalDateTime.now();

        List<MetaTagWeightLog> logs = mappings.stream()
                .map(m -> MetaTagWeightLog.builder()
                        .memberId(memberId)
                        .metaTagId(m.getMetaTag().getId())
                        .actionType(productLog.getActionType())
                        .appliedWeight(weight)
                        .referenceId(productLog.getReferenceId())
                        .eventTimeStamp(eventTimeStamp)
                        .createdAt(createdAt)
                        .build())
                .toList();

        metaTagWeightLogRepository.saveAll(logs);

        if (!applyScore || weight == null) {
            return;
        }

        memberRepository.findById(memberId).ifPresent(member -> {
            for (MetaTagProduct m : mappings) {
                memberTagWeightService.applyBehaviorScore(member, m.getMetaTag(), weight);
            }
        });
    }
}
