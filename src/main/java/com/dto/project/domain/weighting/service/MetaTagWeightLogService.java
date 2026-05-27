package com.dto.project.domain.weighting.service;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
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
    public void recordFromProduct(Long memberId, Long productId, WeightLogType action, Long referenceId){
        List<MetaTagProduct> mappings = metaTagProductRepository.findByProduct_Id(productId);
        if (mappings.isEmpty()) return;

        Integer weight = weightProps.getActionWeight().get(action);
        LocalDateTime now = LocalDateTime.now();

        List<MetaTagWeightLog> logs = mappings.stream()
                .map(m -> MetaTagWeightLog.builder()
                        .memberId(memberId)
                        .metaTagId(m.getMetaTag().getId())
                        .actionType(action)
                        .appliedWeight(weight)
                        .referenceId(referenceId)
                        .eventTimeStamp(now)
                        .createdAt(now)
                        .build())
                .toList();

        metaTagWeightLogRepository.saveAll(logs);

    }
}
