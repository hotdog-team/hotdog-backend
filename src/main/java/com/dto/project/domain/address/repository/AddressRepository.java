package com.dto.project.domain.address.repository;

import com.dto.project.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 특정 회원의 기본 배송지를 조회하는 쿼리 메서드
    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);
}