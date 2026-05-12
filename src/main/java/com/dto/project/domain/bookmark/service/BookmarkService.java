package com.dto.project.domain.bookmark.service;

import com.dto.project.domain.bookmark.dto.BookmarkResponse;
import com.dto.project.domain.bookmark.entity.Bookmark;
import com.dto.project.domain.bookmark.repository.BookmarkRepository;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public void addBookmark(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (bookmarkRepository.existsByMemberAndProduct(member, product)) {
            throw new IllegalArgumentException("이미 찜한 상품입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);
    }

    public void removeBookmark(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        bookmarkRepository.deleteByMemberAndProduct(member, product);
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarks(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return bookmarkRepository.findAllByMember(member)
                .stream()
                .map(BookmarkResponse::from)
                .toList();
    }
}