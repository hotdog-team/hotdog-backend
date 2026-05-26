package com.dto.project.domain.review.repository;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByProductOrderByCreatedAtDesc(Product product);

    List<Review> findAllByMemberOrderByCreatedAtDesc(Member member);

    Optional<Review> findByMemberAndProduct(Member member, Product product);

    boolean existsByMemberAndProduct(Member member, Product product);
}