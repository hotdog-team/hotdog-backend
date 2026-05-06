package com.dto.project.domain.member.repository;

import com.dto.project.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인할 때 이메일로 유저를 찾기 위한 메서드
    Optional<Member> findByEmail(String email);

    // 회원가입할 때 이미 존재하는 이메일인지 중복 확인하기
    boolean existsByEmail(String email);
}