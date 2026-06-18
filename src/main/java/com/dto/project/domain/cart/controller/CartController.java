package com.dto.project.domain.cart.controller;

import com.dto.project.domain.cart.dto.CartAddRequest;
import com.dto.project.domain.cart.dto.CartBulkAddRequest;
import com.dto.project.domain.cart.dto.CartBulkDeleteRequest;
import com.dto.project.domain.cart.dto.CartResponse;
import com.dto.project.domain.cart.dto.CartUpdateRequest;
import com.dto.project.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

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

    // 장바구니 다량 추가
    @PostMapping("/bulk")
    public void addCarts(
            Authentication authentication,
            @RequestBody CartBulkAddRequest request
    ) {
        Long memberId = getLoginMemberId(authentication);
        cartService.addCarts(memberId, request);
    }

    // 장바구니 다량 삭제
    @DeleteMapping("/bulk")
    public void deleteCarts(
            Authentication authentication,
            @RequestBody CartBulkDeleteRequest request
    ) {
        Long memberId = getLoginMemberId(authentication);
        cartService.deleteCarts(memberId, request);
    }

    // JWT 인증 정보에서 로그인 회원 ID 추출
    private Long getLoginMemberId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}