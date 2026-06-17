package com.dto.project.domain.cart.service;

import com.dto.project.domain.cart.dto.CartAddRequest;
import com.dto.project.domain.product.entity.ProductImage;
import com.dto.project.domain.cart.dto.CartBulkAddRequest;
import com.dto.project.domain.cart.dto.CartBulkDeleteRequest;
import com.dto.project.domain.cart.dto.CartResponse;
import com.dto.project.domain.cart.dto.CartUpdateRequest;
import com.dto.project.domain.cart.entity.Cart;
import com.dto.project.domain.cart.repository.CartRepository;
import com.dto.project.domain.order.entity.ProductSource;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.weighting.entity.WeightLogType;
import com.dto.project.domain.weighting.service.ProductWeightLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductWeightLogService productWeightLogService;

    // 장바구니 추가
    public void addCart(Long memberId, CartAddRequest request) {

        // 수량 검증
        validateQuantity(request.getQuantity());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 장바구니 추가 가능 상품 검증
        validateAvailableProduct(product);

        Cart cart = cartRepository.findByMemberIdAndProductId(memberId, request.getProductId())
                .orElse(null);

        if (cart != null) {

            int updatedQuantity = cart.getQuantity() + request.getQuantity();

            // 재고 초과 검증
            validateStock(product, updatedQuantity);

            cart.increaseQuantity(request.getQuantity());
            recordCartBehavior(memberId, product.getId());
            return;
        }

        // 재고 초과 검증
        validateStock(product, request.getQuantity());
        
        String imageUrl = getThumbnailImage(product);

        Cart newCart = new Cart(
                memberId,
                ProductSource.INTERNAL,
                product,
                null,
                product.getName(),
                imageUrl,
                product.getPrice(),
                request.getQuantity()
        );

        cartRepository.save(newCart);
        recordCartBehavior(memberId, product.getId());
    }

 // 장바구니 조회
    @Transactional(readOnly = true)
    public List<CartResponse> getCarts(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .stream()

                // 판매 가능한 상품만 조회
                .filter(cart -> cart.getProduct() != null)
                .filter(cart -> isAvailableProduct(cart.getProduct()))

                .map(cart -> {
                    Product product = cart.getProduct();

                    return new CartResponse(
                            cart.getId(),
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            cart.getQuantity(),
                            getThumbnailImage(product),
                            product.getDiscountRate(),
                            product.getSalePrice(),
                            product.getDeliveryFee() != null ? product.getDeliveryFee() : 0
                    );
                })
                .toList();
    }

    // 장바구니 수량 수정
    public void updateCart(Long cartId, Long memberId, CartUpdateRequest request) {

        // 수량 검증
        validateQuantity(request.getQuantity());

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 장바구니 항목만 수정할 수 있습니다.");
        }

        Product product = cart.getProduct();

        // 장바구니 수정 가능 상품 검증
        validateAvailableProduct(product);

        // 재고 초과 검증
        validateStock(product, request.getQuantity());

        cart.updateQuantity(request.getQuantity());
    }

    // 장바구니 항목 삭제
    public void deleteCart(Long cartId, Long memberId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 장바구니 항목만 삭제할 수 있습니다.");
        }

        if (cart.getProduct() != null) {
            recordCancelCartBehavior(memberId, cart.getProduct().getId());
        }

        cartRepository.delete(cart);
    }

    // 장바구니 비우기
    public void clearCart(Long memberId) {
        List<Cart> carts = cartRepository.findByMemberId(memberId);
        for (Cart cart : carts) {
            if (cart.getProduct() != null) {
                recordCancelCartBehavior(memberId, cart.getProduct().getId());
            }
        }
        cartRepository.deleteByMemberId(memberId);
    }

    // 장바구니 다량 추가
    @Transactional
    public void addCarts(Long memberId, CartBulkAddRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("추가할 장바구니 상품이 없습니다.");
        }

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

        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw new IllegalArgumentException("삭제할 장바구니 상품이 없습니다.");
        }

        List<Cart> carts = cartRepository.findByIdInAndMemberId(
                request.getCartIds(),
                memberId
        );

        if (carts.size() != request.getCartIds().size()) {
            throw new IllegalArgumentException("삭제할 수 없는 장바구니 상품이 포함되어 있습니다.");
        }

        for (Cart cart : carts) {
            if (cart.getProduct() != null) {
                recordCancelCartBehavior(memberId, cart.getProduct().getId());
            }
        }

        cartRepository.deleteAll(carts);
    }

    // 수량 검증
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }

    // 장바구니 추가/수정 가능 상품 검증
    private void validateAvailableProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }

        if (!"ON_SALE".equals(product.getStatus())) {
            throw new IllegalArgumentException("판매 중인 상품만 장바구니에 담을 수 있습니다.");
        }

        if (product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("품절된 상품은 장바구니에 담을 수 없습니다.");
        }
    }

    // 판매 가능 상품 여부 확인
    private boolean isAvailableProduct(Product product) {
        return product != null
                && "ON_SALE".equals(product.getStatus())
                && product.getStockQuantity() > 0;
    }

    // 재고 검증
    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("상품 재고보다 많은 수량은 담을 수 없습니다.");
        }
    }
    
    // 장바구니 썸네일 이미지 조회
    private String getThumbnailImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(ProductImage::isMain)
                .map(ProductImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElseGet(() ->
                        product.getImages().stream()
                                .map(ProductImage::getImageUrl)
                                .filter(url -> url != null && !url.isBlank())
                                .findFirst()
                                .orElse(null)
                );
    }

    private void recordCartBehavior(Long memberId, Long productId) {
        try {
            productWeightLogService.recordBehavior(memberId, productId, WeightLogType.CART);
        } catch (Exception e) {
            log.warn("장바구니 behavior log(CART) 기록 실패: memberId={}, productId={}", memberId, productId, e);
        }
    }

    private void recordCancelCartBehavior(Long memberId, Long productId) {
        try {
            productWeightLogService.recordBehavior(memberId, productId, WeightLogType.CANCEL_CART);
        } catch (Exception e) {
            log.warn("장바구니 behavior log(CANCEL_CART) 기록 실패: memberId={}, productId={}", memberId, productId, e);
        }
    }
}