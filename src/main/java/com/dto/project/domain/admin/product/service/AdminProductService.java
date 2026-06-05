package com.dto.project.domain.admin.product.service;

import com.dto.project.domain.admin.product.dto.AdminProductRequest;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final MetaTagProductRepository metaTagProductRepository;
    private final MetaTagRepository metaTagRepository;

    /**
     * 1. 관리자 상품 등록
     */
    @Transactional
    public void createProduct(AdminProductRequest request) {
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDiscountRate(request.getDiscountRate() != null ? request.getDiscountRate() : 0);
        product.setDeliveryFee(request.getDeliveryFee());
        product.setStockQuantity(request.getStockQuantity());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setOrigin(request.getOrigin());
        product.setSpecInfo(request.getSpecInfo());
        product.setAltText(request.getAltText());
        product.setWeightScore(0.0);

        product.setStatus("ON_SALE");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        saveMetaTags(product, request.getMetaTagIds());
    }

    /**
     * 2. 관리자 상품 수정
     */
    @Transactional
    public void updateProduct(Long productId, AdminProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + productId));

        // updateProductInfo 메서드에 discountRate 파라미터 추가 전달
        int discountRateToUpdate = request.getDiscountRate() != null ? request.getDiscountRate() : 0;

        // 상품 기본 정보 업데이트
        product.updateProductInfo(
                request.getCategoryId(), request.getName(), request.getPrice(),
                request.getDeliveryFee(), request.getStockQuantity(), request.getShortDescription(),
                request.getDescription(), request.getBrand(), request.getOrigin(),
                request.getSpecInfo(), request.getAltText()
        );

        if (product.getStatus() == null) {
            product.setStatus("ON_SALE");
        }

        // 기존 매핑 끊고 새로 매핑
        metaTagProductRepository.deleteByProduct_Id(productId);

        saveMetaTags(product, request.getMetaTagIds());
    }

    /**
     * 3. 관리자 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + productId));

        product.changeStatus("DELETED");

        // 상품 상태가 삭제 처리될 때 연관된 메타태그 매핑도 제거하여 무결성 유지
        metaTagProductRepository.deleteByProduct_Id(productId);
    }

    /**
     * 상품-메타태그 연관관계 매핑 저장
     */
    private void saveMetaTags(Product product, List<Long> metaTagIds) {
        if (metaTagIds != null && !metaTagIds.isEmpty()) {
            for (Long tagId : metaTagIds) {
                // 1. 태그 존재 여부 확인
                MetaTagEntity metaTag = metaTagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 메타태그 ID입니다: " + tagId));

                // 2. ACTIVE 상태인지 검증
                if (metaTag.getMetaTagStatus() != MetaTagStatus.ACTIVE) {
                    throw new IllegalArgumentException("사용할 수 없는 상태의 메타태그입니다: " + metaTag.getName());
                }

                // 3. 매핑 객체 생성 및 저장
                MetaTagProduct metaTagProduct = MetaTagProduct.builder()
                        .product(product)
                        .metaTag(metaTag)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                metaTagProductRepository.save(metaTagProduct);
            }
        }
    }
}