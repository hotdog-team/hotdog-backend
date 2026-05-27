package com.dto.project.domain.weighting.repository;

import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductWeightLogRepository extends JpaRepository<ProductWeightLog, Long> {
    boolean existsByMemberIdAndProductIdAndActionType(Long memberId, Long productId, WeightLogType actionType);

    @Query("SELECT p.id FROM ProductWeightLog p WHERE p.memberId = :memberId AND p.productId = :productId AND p.actionType = :actionType")
    Optional<Long> findByMemberIdAndProductIdAndActionType(Long memberId, Long productId, WeightLogType actionType);
}
