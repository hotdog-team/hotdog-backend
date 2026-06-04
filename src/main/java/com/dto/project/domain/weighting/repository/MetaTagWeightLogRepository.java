package com.dto.project.domain.weighting.repository;

import com.dto.project.domain.weighting.entity.MetaTagWeightLog;
import com.dto.project.domain.weighting.entity.WeightLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MetaTagWeightLogRepository extends JpaRepository<MetaTagWeightLog, Long> {

    @Query("""
            SELECT m.id FROM MetaTagWeightLog m
            WHERE m.memberId = :memberId
              AND m.metaTagId = :metaTagId
              AND m.actionType = :referredAction
              AND m.referenceId = :productLogId
              AND NOT EXISTS (
                  SELECT 1 FROM MetaTagWeightLog c
                  WHERE c.referenceId = m.id
                    AND c.actionType = :cancelAction
              )
            ORDER BY m.eventTimeStamp DESC
            LIMIT 1
            """)
    Optional<Long> findLatestUncancelledReferredId(
            @Param("memberId") Long memberId,
            @Param("metaTagId") Long metaTagId,
            @Param("referredAction") WeightLogType referredAction,
            @Param("productLogId") Long productLogId,
            @Param("cancelAction") WeightLogType cancelAction);

    //findUncancelledEventTime - 취소되지 않은 eventTime을 List 형태로 구한다
    @Query("""
    SELECT m.eventTimeStamp FROM MetaTagWeightLog m
    WHERE m.memberId = :memberId
      AND m.metaTagId = :metaTagId
      AND m.actionType = :referredAction
      AND m.eventTimeStamp IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM MetaTagWeightLog c
          WHERE c.referenceId = m.id
            AND c.actionType = :cancelAction
      )
    ORDER BY m.eventTimeStamp ASC
    """)
    List<LocalDateTime> findUncancelledEventTimeStampAsc(
            @Param("memberId") Long memberId,
            @Param("metaTagId") Long metaTagId,
            @Param("referredAction") WeightLogType referredAction,
            @Param("cancelAction") WeightLogType cancelAction
    );
}
