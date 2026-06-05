package com.dto.project.domain.address.repository;

import com.dto.project.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 특정 회원의 기본 배송지를 조회하는 쿼리 메서드
    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    // 회원의 전체 배송지 목록 조회 (최신순)
    List<Address> findAllByMemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId,
            String status
    );

    // 배송지 단건 조회
    Optional<Address> findByIdAndMemberIdAndStatus(
            Long id,
            Long memberId,
            String status
    );

 // 회원의 기본 배송지 목록 조회
    List<Address> findAllByMemberIdAndIsDefaultTrueAndStatus(
            Long memberId,
            String status
    );

    // 회원이 배송지를 가지고 있는지 확인
    boolean existsByMemberIdAndStatus(
            Long memberId,
            String status
    );
}