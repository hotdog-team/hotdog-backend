package com.dto.project.domain.cart.service;

import com.dto.project.domain.cart.dto.CartAddRequest;
import com.dto.project.domain.cart.dto.CartBulkAddRequest;
import com.dto.project.domain.cart.dto.CartBulkDeleteRequest;
import com.dto.project.domain.cart.dto.CartResponse;
import com.dto.project.domain.cart.dto.CartUpdateRequest;
import com.dto.project.domain.cart.entity.Cart;
import com.dto.project.domain.cart.repository.CartRepository;
import com.dto.project.domain.order.entity.ProductSource;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    // 장바구니 추가
    public void addCart(Long memberId, CartAddRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberIdAndProductId(memberId, request.getProductId())
                .orElse(null);

        if (cart != null) {
            cart.increaseQuantity(request.getQuantity());
            return;
        }

        Cart newCart = new Cart(memberId, ProductSource.INTERNAL, product, null, product.getName(),
        						null, product.getPrice(), request.getQuantity());
        cartRepository.save(newCart);
    }

    // 장바구니 조회
    @Transactional(readOnly = true)
    public List<CartResponse> getCarts(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .stream()
                .map(cart -> new CartResponse(
                        cart.getId(),
                        cart.getProduct().getId(),
                        cart.getProduct().getName(),
                        cart.getProduct().getPrice(),
                        cart.getQuantity(),
                        null // TODO: 상품 이미지 엔티티 연결 후 썸네일 이미지 넣기
                ))
                .toList();
    }

    // 장바구니 수량 수정
    public void updateCart(Long cartId, Long memberId, CartUpdateRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 장바구니 항목만 수정할 수 있습니다.");
        }

        cart.updateQuantity(request.getQuantity());
    }

    // 장바구니 항목 삭제
    public void deleteCart(Long cartId, Long memberId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 장바구니 항목만 삭제할 수 있습니다.");
        }

        cartRepository.delete(cart);
    }

    // 장바구니 비우기
    public void clearCart(Long memberId) {
        cartRepository.deleteByMemberId(memberId);
    }
    
    // 장바구니 다량 추가
    @Transactional
    public void addCarts(Long memberId, CartBulkAddRequest request) {
        for (CartBulkAddRequest.CartAddItem item : request.getItems()) {
            CartAddRequest addRequest = new CartAddRequest();
            addRequest.setProductId(item.getProductId());
            addRequest.setQuantity(item.getQuantity());

            addCart(memberId, addRequest);
        }
    }
    
    // 장바구니 다량 삭제
    @Transactional
    public void deleteCarts(Long memberId, CartBulkDeleteRequest request) {
        List<Cart> carts = cartRepository.findByIdInAndMemberId(
                request.getCartIds(),
                memberId
        );

        if (carts.size() != request.getCartIds().size()) {
            throw new IllegalArgumentException("삭제할 수 없는 장바구니 상품이 포함되어 있습니다.");
        }

        cartRepository.deleteAll(carts);
    }
}