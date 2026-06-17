package com.dto.project.domain.bookmark.repository;

import com.dto.project.domain.bookmark.entity.Bookmark;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndProduct(Member member, Product product);

    boolean existsByMemberAndProduct(Member member, Product product);

    List<Bookmark> findAllByMember(Member member);
    
    Page<Bookmark> findAllByMember(Member member, Pageable pageable);

    @Query("""
            SELECT b FROM Bookmark b
            JOIN b.product p
            WHERE b.member = :member
            AND (:categoryId IS NULL OR p.categoryId = :categoryId)
            """)
    Page<Bookmark> findByMemberAndCategoryId(
            @Param("member") Member member,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    void deleteByMemberAndProduct(Member member, Product product);
}