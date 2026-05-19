package com.dto.project.domain.cart.controller;

import com.dto.project.domain.cart.dto.CartAddRequest;
import com.dto.project.domain.cart.dto.CartResponse;
import com.dto.project.domain.cart.dto.CartUpdateRequest;
import com.dto.project.domain.cart.service.CartService;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MemberRepository memberRepository;

    // 장바구니 추가
    @PostMapping
    public void addCart(
            Authentication authentication,
            @RequestBody CartAddRequest request
    ) {
        Long memberId = getLoginMemberId(authentication);
        cartService.addCart(memberId, request);
    }

    // 장바구니 조회
    @GetMapping
    public List<CartResponse> getCarts(Authentication authentication) {
        Long memberId = getLoginMemberId(authentication);
        return cartService.getCarts(memberId);
    }

    // 장바구니 수량 수정
    @PatchMapping("/{cartId}")
    public void updateCart(
            @PathVariable Long cartId,
            Authentication authentication,
            @RequestBody CartUpdateRequest request
    ) {
        Long memberId = getLoginMemberId(authentication);
        cartService.updateCart(cartId, memberId, request);
    }

    // 장바구니 삭제
    @DeleteMapping("/{cartId}")
    public void deleteCart(
            @PathVariable Long cartId,
            Authentication authentication
    ) {
        Long memberId = getLoginMemberId(authentication);
        cartService.deleteCart(cartId, memberId);
    }

    // 장바구니 비우기
    @DeleteMapping
    public void clearCart(Authentication authentication) {
        Long memberId = getLoginMemberId(authentication);
        cartService.clearCart(memberId);
    }

    private Long getLoginMemberId(Authentication authentication) {

        String email = authentication.getName();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return member.getId();
    }
}