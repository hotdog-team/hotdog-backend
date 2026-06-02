package com.dto.project.domain.weighting.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.weighting.entity.MemberTagWeight;
import com.dto.project.domain.metatags.entity.MetaTagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberTagWeightRepository extends JpaRepository<MemberTagWeight, Long> {

    @Query("""
            SELECT mtw.metaTag.id FROM MemberTagWeight mtw
            WHERE mtw.member.id = :memberId
              AND mtw.metaTag.type IN :types
              AND mtw.weightScore >= :minScore
            """)
    List<Long> findMetaTagIdsByMemberIdAndMetaTagTypeInAndWeightScoreGreaterThanEqual(
            @Param("memberId") Long memberId,
            @Param("types") Collection<MetaTagType> types,
            @Param("minScore") int minScore);

    @Query("""
            SELECT mtw FROM MemberTagWeight mtw
            JOIN FETCH mtw.metaTag
            WHERE mtw.member.id = :memberId
              AND mtw.metaTag.type IN :types
            """)
    List<MemberTagWeight> findAllByMemberIdAndMetaTagTypeIn(
            @Param("memberId") Long memberId,
            @Param("types") Collection<MetaTagType> types);

    Optional<MemberTagWeight> findByMember_IdAndMetaTag_Id(Long memberId, Long metaTagId);

    // 특정 회원의 특정 메타태그 타입 데이터만 선별 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MemberTagWeight mtw WHERE mtw.member = :member AND mtw.metaTag.type = :type")
    void deleteByMemberAndMetaTagType(@Param("member") Member member, @Param("type") MetaTagType type);

    // 회원 탈퇴 시 해당 회원의 모든 가중치 프로필 삭제
    @Modifying
    @Query("DELETE FROM MemberTagWeight mtw WHERE mtw.member = :member")
    void deleteAllByMember(@Param("member") Member member);

    // member의 메타태그 가중치 점수 불러오기
    @Query("""
           SELECT mtw FROM MemberTagWeight mtw
           JOIN FETCH mtw.metaTag
           WHERE mtw.member.id = :memberId
    """)
    List<MemberTagWeight> findAllByMemberIdWithMetaTag(@Param("memberId") Long memberId);
}