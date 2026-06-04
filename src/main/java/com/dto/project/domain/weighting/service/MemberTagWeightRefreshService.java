package com.dto.project.domain.weighting.service;

import com.dto.project.domain.metatags.entity.SeasonalMetaTag;
import com.dto.project.domain.weighting.entity.MemberTagWeight;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.repository.MemberTagWeightRepository;
import com.dto.project.domain.weighting.repository.MetaTagWeightLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberTagWeightRefreshService {

    private final MetaTagWeightLogRepository metaTagWeightLogRepository;
    private final MemberTagWeightRepository memberTagWeightRepository;

    private static final Duration DEFAULT_INTERVAL = Duration.ofDays(365);
    private static final Duration MIN_INTERVAL = Duration.ofDays(30);

    @Transactional
    public void applyUplift() {
        applyUpliftFromSeason();
        applyUpliftFromUpdatedAt();
    }

    //해당 계절 기간이면 updatedAt을 끌어올린다
    @Transactional
    public void applyUpliftFromSeason() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        List<MemberTagWeight> weights = memberTagWeightRepository.findAll();
        for (MemberTagWeight weight : weights) {
            Long metaTagId = weight.getMetaTag().getId();
            Optional<SeasonalMetaTag> season = SeasonalMetaTag.fromMetaTagId(metaTagId);
            if (season.isEmpty() || !season.get().isActive(today)) {
                continue;
            }

            LocalDateTime updatedAt = weight.getUpdatedAt();
            if (updatedAt != null && ChronoUnit.DAYS.between(updatedAt, now) < 7) {
                continue;
            }

            memberTagWeightRepository.touchUpdatedAt(weight.getId(), now);
        }
    }

    //재구매 시기가 되면 updatedAt을 끌어올린다
    @Transactional
    public void applyUpliftFromUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        List<MemberTagWeight> weights = memberTagWeightRepository.findAll();
        for (MemberTagWeight weight:weights){
            Long memberId = weight.getMember().getId();
            Long metaTagId = weight.getMetaTag().getId();
            LocalDateTime updatedAt = weight.getUpdatedAt();

            //30일 미만이라면 생략한다
            if (updatedAt == null || ChronoUnit.DAYS.between(updatedAt, now) < 30) continue;

            Duration interval = resolveAveragePurchaseInterval(memberId, metaTagId);
            LocalDateTime windowStart = updatedAt.plus(interval).minusDays(7);

            if (!now.isBefore(windowStart)) {  // now >= windowStart
                memberTagWeightRepository.touchUpdatedAt(weight.getId(), now); // updatedAt 갱신
            }
        }
    }

    private Duration resolveAveragePurchaseInterval(Long memberId, Long metaTagId){
        List<LocalDateTime> times = metaTagWeightLogRepository.findUncancelledEventTimeStampAsc(
                memberId, metaTagId, WeightLogType.BUY, WeightLogType.CANCEL_BUY);

        //첫 구매(로그가 1개)일 경우 1년을 기본으로 잡는다
        if (times.size() < 2) {
            return DEFAULT_INTERVAL;
        }

        long totalDays = 0;
        int gaps = 0;
        for (int i = 1; i < times.size(); i++) {
            long days = ChronoUnit.DAYS.between(times.get(i - 1), times.get(i));
            if (days > 0) {
                totalDays += days;
                gaps++;
            }
        }
        if (gaps == 0) {
            return DEFAULT_INTERVAL;
        }
        Duration avg = Duration.ofDays(totalDays / gaps);

        //마지막으로부터 간격이 짧다면 최소치(한 달)로 처리한다
        return avg.compareTo(MIN_INTERVAL) < 0 ? MIN_INTERVAL : avg;
    }
}
