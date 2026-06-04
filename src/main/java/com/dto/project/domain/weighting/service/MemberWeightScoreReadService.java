package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import com.dto.project.domain.weighting.dto.MemberTagHotScore;
import com.dto.project.domain.weighting.entity.MemberTagWeight;
import com.dto.project.domain.weighting.repository.MemberTagWeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberWeightScoreReadService {
    private final MemberTagWeightRepository memberTagWeightRepository;
    private final MetaTagRepository metaTagRepository;
    private final MemberRepository memberRepository;
    private final MemberTagWeightHotService memberTagWeightHotService;
    private final WeightingProperties weightProps;

    public Map<Long, Double> getEffectiveTagWeights(Long memberId){
        List<MemberTagWeight> memTagWeights  = memberTagWeightRepository.
                findAllByMemberIdWithMetaTag(memberId);

        //getType 사용으로 객체 자체 조회
        Map<Long, MemberTagWeight> dbScoreMap = memTagWeights.stream()
                .collect(Collectors.toMap(
                        w -> w.getMetaTag().getId(),
                        w -> w
                ));

        List<MemberTagHotScore> memTagHotScores = memberTagWeightHotService.getAllScore(memberId);

        Map<Long, Integer> hotDeltaMap = memTagHotScores == null
                ? Map.of()
                : memTagHotScores.stream()
                        .collect(Collectors.toMap(
                                MemberTagHotScore::getMetaTagId,
                                MemberTagHotScore::getHotDelta
                        ));

        Set<Long> allTagIds = new HashSet<>(dbScoreMap.keySet());
        allTagIds.addAll(hotDeltaMap.keySet());

        Map<Long, Double> result = new HashMap<>();
        for (Long metaTagId : allTagIds) {
            MemberTagWeight db = dbScoreMap.get(metaTagId);
            double dbScore = resolveDbScore(db);
            int hotDelta = hotDeltaMap.getOrDefault(metaTagId, 0);

            double weightScore = dbScore + hotDelta;
            if (weightScore <= 0) continue;

            MetaTagEntity metaTag;

            if (db != null) {
                metaTag = db.getMetaTag();
            } else {
                metaTag = metaTagRepository.findById(metaTagId).orElse(null);
            }

            if (metaTag == null) continue;

            double coefficient = weightProps.getMetaTagCoefficient()
                    .getOrDefault(metaTag.getType(), 1.0);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

            //직종 보너스
            if (metaTag.getType() == MetaTagType.OCCUPATION && member.isJobRecommendEnabled()) {
                coefficient *= weightProps.getOccupationBonus();
            }

            result.put(metaTagId, weightScore * coefficient);
        }

        return result;
    }

    // profile_score(비감쇠) + effective_score(행동 감쇠 결과, 없으면 weight_score)
    private double resolveDbScore(MemberTagWeight db) {
        if (db == null) return 0;
        int profile = db.getProfileScore() != null ? db.getProfileScore() : 0;
        double behavior;
        if (db.getEffectiveScore() != null) {
            behavior = db.getEffectiveScore();
        } else if (db.getWeightScore() != null) {
            behavior = db.getWeightScore();
        } else {
            behavior = 0;
        }
        return profile + behavior;
    }

}
