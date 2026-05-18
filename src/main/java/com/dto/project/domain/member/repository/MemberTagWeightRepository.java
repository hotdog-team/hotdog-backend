package com.dto.project.domain.member.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberTagWeight;
import com.dto.project.domain.metatags.entity.MetaTagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberTagWeightRepository extends JpaRepository<MemberTagWeight, Long> {

    // 특정 회원의 특정 메타태그 타입 데이터만 선별 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MemberTagWeight mtw WHERE mtw.member = :member AND mtw.metaTag.type = :type")
    void deleteByMemberAndMetaTagType(@Param("member") Member member, @Param("type") MetaTagType type);

    // 회원 탈퇴 시 해당 회원의 모든 가중치 프로필 삭제
    @Modifying
    @Query("DELETE FROM MemberTagWeight mtw WHERE mtw.member = :member")
    void deleteAllByMember(@Param("member") Member member);
}