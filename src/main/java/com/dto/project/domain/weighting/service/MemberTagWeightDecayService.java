package com.dto.project.domain.weighting.service;

import com.dto.project.domain.weighting.entity.MemberTagWeight;
import com.dto.project.domain.weighting.repository.MemberTagWeightRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTagWeightDecayService {

    private final MemberTagWeightRepository memberTagWeightRepository;

    @Transactional
    public void applyDecayFromUpdatedAt(){
        List<MemberTagWeight> weights = memberTagWeightRepository.findAll();
        for (MemberTagWeight weight:weights){
            double weightScore = weight.getWeightScore() != null ? weight.getWeightScore() : 0;
            LocalDateTime updatedAt = weight.getUpdatedAt();
            if (updatedAt == null) continue;

            long days = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());

            //1년 6개월 후까지 갱신안되면 delete 처리함
            if (days > 547) {
                if(weight.getProfileScore() == null || weight.getProfileScore() <= 0) {
                    memberTagWeightRepository.delete(weight);
                }
                else {
                    memberTagWeightRepository.updateEffectiveScore(weight.getId(), 0.0);
                }

                continue;
            }

            double factor = decayFactor(days);
            //0.X를 0점 처리하도록 함
            double effectiveScore = Math.floor(weightScore * factor);

            if (weight.hasNoScore()) {
                memberTagWeightRepository.delete(weight);
                continue;
            }

            memberTagWeightRepository.updateEffectiveScore(weight.getId(), effectiveScore);
        }
    }

    private double decayFactor(long days) {
        if (days <= 30) return 1.0;
        if (days <= 60) return 0.7;
        if (days <= 90) return 0.3;
        return 0.0;
    }

}
