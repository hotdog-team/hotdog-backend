package com.dto.project.domain.weighting.repository;

import com.dto.project.domain.weighting.entity.ProductWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductWeightLogRepository extends JpaRepository<ProductWeightLog, Long> {
    boolean existsByMemberIdAndProductIdAndActionType(Long memberId, Long productId, WeightLogType actionType);

    //findLatestUncancelledId - 취소되지 않은 referredId를 구한다
    @Query("""
            SELECT p.id FROM ProductWeightLog p
            WHERE p.memberId = :memberId
              AND p.productId = :productId
              AND p.actionType = :referredAction
              AND NOT EXISTS (
                  SELECT 1 FROM ProductWeightLog c
                  WHERE c.referenceId = p.id
                    AND c.actionType = :cancelAction
              )
            ORDER BY p.eventTimeStamp DESC
            LIMIT 1
            """)
    Optional<Long> findLatestUncancelledId(
            @Param("memberId") Long memberId,
            @Param("productId") Long productId,
            @Param("referredAction") WeightLogType referredAction,
            @Param("cancelAction") WeightLogType cancelAction);

    Optional<ProductWeightLog> findFirstByMemberIdAndProductIdAndActionTypeOrderByEventTimeStampDesc(
            Long memberId, Long productId, WeightLogType actionType);

    @Query("""
    SELECT COUNT(p) FROM ProductWeightLog p
    WHERE p.productId = :productId
      AND p.actionType = com.dto.project.domain.weighting.entity.WeightLogType.BUY
      AND NOT EXISTS (
          SELECT 1 FROM ProductWeightLog c
          WHERE c.referenceId = p.id
            AND c.actionType = com.dto.project.domain.weighting.entity.WeightLogType.CANCEL_BUY
      )
    """)
    long countUncancelledBuyByProductId(@Param("productId") Long productId);

}
