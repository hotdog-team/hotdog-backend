package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.weighting.entity.MemberTagWeight;
import com.dto.project.domain.weighting.repository.MemberTagWeightRepository;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.weighting.config.WeightingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

//기존 Service Refactor 및 직종, 나이대 정보 동기화 처리
//변경값만 반영되도록 처리
@Service
@RequiredArgsConstructor
public class MemberTagWeightService {

    private final MemberTagWeightRepository memberTagWeightRepository;
    private final MetaTagRepository metaTagRepository;
    private final WeightingProperties weightProps;

    //MetaTagType에서 CATEGORY, PURPOSE, MERCHANDISING을 가져와 EnumSet으로 묶는다
    private static final Set<MetaTagType> PROFILE_TAG_TYPES = EnumSet.of(
            MetaTagType.CATEGORY,
            MetaTagType.PURPOSE,
            MetaTagType.MERCHANDISING
    );

    //Member의 profileTagIds를 찾아 List화한다
    public List<Long> findProfileTagIds(Member member) {
        return memberTagWeightRepository.findMetaTagIdsByMemberIdAndMetaTagTypeInAndProfileScoreGreaterThanEqual(
                member.getId(), PROFILE_TAG_TYPES, weightProps.getProfileInitialScore());
    }

    // 회원가입 시 프로필 기반 member_tag_weights 초기 설정
    public void initializeFromSignup(Member member, List<Long> profileTagIds) {
        if (profileTagIds != null && !profileTagIds.isEmpty()) {
            saveInitialWeights(member, resolveProfileTagIds(profileTagIds));
        }
        if (member.isJobRecommendEnabled()) {
            syncOccupation(member, member.getJobType());
        }
        syncAgeRange(member, member.getAgeRange());
    }

    //Profile 업데이트 동기 처리
    public void syncFromProfileUpdate(Member member,
                                      List<Long> profileTagIds,
                                      String ageRange,
                                      String jobType,
                                      Boolean isJobRecommendEnabled) {

        //isJobRecommendEnabled 분기
        //isJobRecommendEnabled가 false라면 가중치는 반영되지 않음
        if (isJobRecommendEnabled != null) {
            if (!isJobRecommendEnabled) {
                syncOccupation(member, null);
            } else {
                syncOccupation(member, member.getJobType());
            }
        } else if (jobType != null && member.isJobRecommendEnabled()) {
            syncOccupation(member, jobType);
        }

        // 프로필 태그 가중치 동기화
        if (profileTagIds != null) {
            syncProfileTags(member, profileTagIds);
        }

        // 연령대 가중치 동기화 (AGE_PREFERENCE는 1개만 유지)
        if (ageRange != null) {
            syncAgeRange(member, ageRange);
        }
    }

    // 직종 가중치 정보 동기화(5점)
    public void syncOccupation(Member member, String jobType) {
        int baseScore = weightProps.getProfileOccupationScore();
        Optional<MetaTagEntity> newTag = (jobType == null || jobType.isBlank())
                ? Optional.empty()
                : metaTagRepository.findByNameAndType(jobType, MetaTagType.OCCUPATION);

        List<MemberTagWeight> current = memberTagWeightRepository.findAllByMemberIdAndMetaTagTypeIn(
                member.getId(), Set.of(MetaTagType.OCCUPATION));

        Long newTagId = newTag.map(MetaTagEntity::getId).orElse(null);
        for (MemberTagWeight weight : current) {
            if (!weight.getMetaTag().getId().equals(newTagId)) {
                adjustProfileOrDelete(weight, -baseScore);
            }
        }

        newTag.ifPresent(tag -> {
            boolean alreadyPresent = current.stream()
                    .anyMatch(weight -> weight.getMetaTag().getId().equals(tag.getId()));
            if (!alreadyPresent) {
                applyProfileDelta(member, tag, baseScore);
            }
        });
    }

    // 연령대 가중치 정보 동기화(5점)
    public void syncAgeRange(Member member, String ageRange) {
        memberTagWeightRepository.deleteByMemberAndMetaTagType(member, MetaTagType.AGE_PREFERENCE);
        if (ageRange == null || ageRange.isBlank()) {
            return;
        }
        metaTagRepository.findByNameAndType(ageRange, MetaTagType.AGE_PREFERENCE)
                .ifPresent(tag -> memberTagWeightRepository.save(
                        buildProfileWeight(member, tag, weightProps.getProfileAgeScore())));
    }

    // profileTagIds 동기화
    public void syncProfileTags(Member member, List<Long> profileTagIds) {
        int baseScore = weightProps.getProfileInitialScore();
        Set<Long> newTagIds = resolveProfileTagIds(profileTagIds);

        List<MemberTagWeight> current = memberTagWeightRepository.findAllByMemberIdAndMetaTagTypeIn(
                member.getId(), PROFILE_TAG_TYPES);

        Set<Long> currentTagIds = new HashSet<>();
        for (MemberTagWeight weight : current) {
            Long tagId = weight.getMetaTag().getId();
            currentTagIds.add(tagId);
            if (!newTagIds.contains(tagId)) {
                adjustProfileOrDelete(weight, -baseScore);
            }
        }

        for (Long tagId : newTagIds) {
            if (!currentTagIds.contains(tagId)) {
                metaTagRepository.findById(tagId)
                        .ifPresent(tag -> applyProfileDelta(member, tag, baseScore));
            }
        }
    }

    // 개인화 데이터 가중치 인덱스 일괄 삭제
    public void deleteAllForMember(Member member) {
        memberTagWeightRepository.deleteAllByMember(member);
    }

    private Set<Long> resolveProfileTagIds(List<Long> profileTagIds) {
        Set<Long> resolved = new HashSet<>();
        if (profileTagIds == null) {
            return resolved;
        }
        for (Long tagId : profileTagIds) {
            metaTagRepository.findById(tagId).ifPresent(tag -> {
                if (PROFILE_TAG_TYPES.contains(tag.getType())) {
                    resolved.add(tagId);
                }
            });
        }
        return resolved;
    }

    private void saveInitialWeights(Member member, Set<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }

        List<MemberTagWeight> weightsToSave = new ArrayList<>();

        // 취합된 태그들을 순회하며 영속성 객체 생성
        for (Long tagId : tagIds) {
            metaTagRepository.findById(tagId).ifPresent(metaTag ->
                    weightsToSave.add(buildProfileWeight(member, metaTag, weightProps.getProfileInitialScore())));
        }

        // DB 부하를 줄이고 트랜잭션 안전성을 위해 saveAll로 일괄 저장
        if (!weightsToSave.isEmpty()) {
            memberTagWeightRepository.saveAll(weightsToSave);
        }
    }

    // 프로필 점수만 반영한다 (decay 대상 아님)
    private void applyProfileDelta(Member member, MetaTagEntity metaTag, int delta) {
        if (delta <= 0) {
            return;
        }
        memberTagWeightRepository.findByMember_IdAndMetaTag_Id(member.getId(), metaTag.getId())
                .ifPresentOrElse(
                        weight -> {
                            weight.adjustProfileScore(delta);
                            memberTagWeightRepository.save(weight);
                        },
                        () -> memberTagWeightRepository.save(buildProfileWeight(member, metaTag, delta))
                );
    }

    private void adjustProfileOrDelete(MemberTagWeight weight, int delta) {
        weight.adjustProfileScore(delta);
        if (weight.hasNoScore()) {
            memberTagWeightRepository.delete(weight);
            return;
        }
        memberTagWeightRepository.save(weight);
    }

    private void adjustBehaviorOrDelete(MemberTagWeight weight, int delta) {
        weight.adjustWeightScore(delta);
        if (weight.hasNoScore()) {
            memberTagWeightRepository.delete(weight);
            return;
        }
        memberTagWeightRepository.save(weight);
    }

    private MemberTagWeight buildProfileWeight(Member member, MetaTagEntity metaTag, int profileScore) {
        return MemberTagWeight.builder()
                .member(member)
                .metaTag(metaTag)
                .profileScore(profileScore)
                .weightScore(0)
                .effectiveScore(0.0)
                .build();
    }

    private MemberTagWeight buildBehaviorWeight(Member member, MetaTagEntity metaTag, int behaviorScore) {
        return MemberTagWeight.builder()
                .member(member)
                .metaTag(metaTag)
                .profileScore(0)
                .weightScore(behaviorScore)
                .effectiveScore((double) behaviorScore)
                .build();
    }

    //행동에 따른 score(delta) 추가 — weight_score만 갱신 (updated_at·decay 대상)
    public void applyBehaviorScore(Member member, MetaTagEntity metaTag, int delta){
        if (delta == 0) return;
        memberTagWeightRepository
                .findByMember_IdAndMetaTag_Id(member.getId(), metaTag.getId())
                .ifPresentOrElse(
                        weight -> adjustBehaviorOrDelete(weight, delta),
                        () -> {
                            if (delta > 0) {
                                memberTagWeightRepository.save(buildBehaviorWeight(member, metaTag, delta));
                            }
                        }
                );
    }

}
