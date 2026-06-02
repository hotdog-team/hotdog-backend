package com.dto.project.domain.cart.repository;

import com.dto.project.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByMemberId(Long memberId);

    Optional<Cart> findByMemberIdAndProductId(Long memberId, Long productId);

    void deleteByMemberId(Long memberId);
    
    List<Cart> findByIdInAndMemberId(List<Long> ids, Long memberId);

    void deleteByIdInAndMemberId(List<Long> ids, Long memberId);
}