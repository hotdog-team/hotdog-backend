package com.dto.project.domain.weighting.service;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.repository.MetaTagWeightLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MetaTagWeightLogService {

    @Autowired
    MetaTagWeightLogRepository metaTagWeightLogRepository;

    @Autowired
    MetaTagProductRepository metaTagProductRepository;

    @Autowired
    WeightingProperties weightProps;

    //ProductWeightLog의 값을 사용하여 기록
    public void recordFromProduct(ProductWeightLog productLog) {
        List<MetaTagProduct> mappings = metaTagProductRepository.findByProduct_Id(productLog.getProductId());
        if (mappings.isEmpty()) return;

        Integer weight = weightProps.getActionWeight().get(productLog.getActionType());
        LocalDateTime eventTimeStamp = productLog.getEventTimeStamp();
        LocalDateTime createdAt = LocalDateTime.now();

        List<MetaTagWeightLog> logs = mappings.stream()
                .map(m -> MetaTagWeightLog.builder()
                        .memberId(productLog.getMemberId())
                        .metaTagId(m.getMetaTag().getId())
                        .actionType(productLog.getActionType())
                        .appliedWeight(weight)
                        .referenceId(productLog.getReferenceId())
                        .eventTimeStamp(eventTimeStamp)
                        .createdAt(createdAt)
                        .build())
                .toList();

        metaTagWeightLogRepository.saveAll(logs);

    }
}
