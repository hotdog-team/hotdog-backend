package com.dto.project.domain.cart.controller;

import com.dto.project.domain.cart.dto.CartAddRequest;
import com.dto.project.domain.cart.dto.CartResponse;
import com.dto.project.domain.cart.dto.CartUpdateRequest;
import com.dto.project.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // TODO: JWT 연결 후 실제 로그인 회원 id로 교체
    private Long getLoginMemberId() {
        return 1L;
    }

    @PostMapping
    public void addCart(@RequestBody CartAddRequest request) {
        cartService.addCart(getLoginMemberId(), request);
    }

    @GetMapping
    public List<CartResponse> getCarts() {
        return cartService.getCarts(getLoginMemberId());
    }

    @PatchMapping("/{cartId}")
    public void updateCart(
            @PathVariable Long cartId,
            @RequestBody CartUpdateRequest request
    ) {
        cartService.updateCart(cartId, getLoginMemberId(), request);
    }

    @DeleteMapping("/{cartId}")
    public void deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId, getLoginMemberId());
    }

    @DeleteMapping
    public void clearCart() {
        cartService.clearCart(getLoginMemberId());
    }
}