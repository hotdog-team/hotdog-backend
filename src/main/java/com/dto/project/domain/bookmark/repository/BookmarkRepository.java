package com.dto.project.domain.bookmark.repository;

import com.dto.project.domain.bookmark.entity.Bookmark;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndProduct(Member member, Product product);

    boolean existsByMemberAndProduct(Member member, Product product);

    List<Bookmark> findAllByMember(Member member);

    void deleteByMemberAndProduct(Member member, Product product);
}