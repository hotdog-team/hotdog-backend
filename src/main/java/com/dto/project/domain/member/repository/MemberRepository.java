package com.dto.project.domain.member.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인할 때 이메일로 유저를 찾기 위한 메서드
    Optional<Member> findByEmail(String email);

    // 회원가입할 때 이미 존재하는 이메일인지 중복 확인하기
    boolean existsByEmail(String email);


     //[관리자용] 동적 검색 및 필터링 쿼리
    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.email LIKE %:keyword%) AND " +
            "(:status IS NULL OR m.status = :status)")
    Page<Member> searchMembers(@Param("keyword") String keyword,
                               @Param("status") MemberStatus status,
                               Pageable pageable);
}