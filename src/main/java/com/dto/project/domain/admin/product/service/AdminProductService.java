package com.dto.project.domain.admin.product.service;

import com.dto.project.domain.admin.product.dto.AdminProductRequest;
import com.dto.project.domain.category.entity.Category;
import com.dto.project.domain.category.repository.CategoryRepository;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.metatags.service.ProductMetaTagAutoService;

import com.dto.project.domain.weighting.config.WeightingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final MetaTagProductRepository metaTagProductRepository;
    private final MetaTagRepository metaTagRepository;
    private final WeightingProperties weightProps;
    private final CategoryRepository categoryRepository;
    private final ProductMetaTagAutoService productMetaTagAutoService;


    private static final Set<MetaTagType> MANUAL_TYPES = EnumSet.of(
            MetaTagType.SEASONAL,
            MetaTagType.PURPOSE,
            MetaTagType.MERCHANDISING,
            MetaTagType.OCCUPATION,
            MetaTagType.AGE_PREFERENCE
    );

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

        saveMetaTags(product, request.getCategoryId(), request.getMetaTagIds());
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
        metaTagProductRepository.flush();

        saveMetaTags(product, request.getCategoryId(), request.getMetaTagIds());
        productMetaTagAutoService.syncAutoTagsForProduct(productId);
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
    private void saveMetaTags(Product product, Long categoryId, List<Long> metaTagIds) {

        List<Long> manualIds = metaTagIds != null ? metaTagIds : List.of();

        // 메타태그 검증(상한 등)
        List<MetaTagEntity> validTags = validateManualMetaTags(manualIds);
            Long categoryMetaTagId = resolveCategoryMetaTagId(categoryId);

            List<Long> finalIds = new ArrayList<>();
            finalIds.add(categoryMetaTagId);
            validTags.forEach(tag -> finalIds.add(tag.getId()));

        LocalDateTime now = LocalDateTime.now();
            for (Long tagId : finalIds) {

                // 3. 매핑 객체 생성 및 저장
                MetaTagProduct metaTagProduct = MetaTagProduct.builder()
                        .product(product)
                        .metaTag(metaTagRepository.getReferenceById(tagId))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                metaTagProductRepository.save(metaTagProduct);
            }
    }


    private List<MetaTagEntity> validateManualMetaTags(List<Long> manualMetaTagIds){
        // 중복 제거
        List<Long> distinctIds = manualMetaTagIds.stream().distinct().toList();
        List<MetaTagEntity> validTags = new ArrayList<>();

        // 사용가능한 태그인지 확인(id, active 여부, 직접 등록 가능한 태그 타입 여부)
        for (Long id : distinctIds) {

            // 1. 태그 존재 여부 확인 - 기존 검증 move
            MetaTagEntity metaTag = metaTagRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 메타태그 ID입니다: " + id));

            // 2. ACTIVE 상태인지 검증 - 기존 검증 move + contains 처리
            if (metaTag.getMetaTagStatus() != MetaTagStatus.ACTIVE || !MANUAL_TYPES.contains(metaTag.getType())) {
                throw new IllegalArgumentException("사용할 수 없는 상태의 메타태그입니다: " + metaTag.getName());
            }
            validTags.add(metaTag);
        }
        // 상한선 검증 - 메서드 분리해둠
        validateTypeCaps(validTags);

        return validTags;
    }

    private void validateTypeCaps(List<MetaTagEntity> validTags){
        Map<MetaTagType, Long> countByType = validTags.stream()
                .collect(Collectors.groupingBy(MetaTagEntity::getType, Collectors.counting()));

        for (MetaTagType type : MANUAL_TYPES) {
            int count = countByType.getOrDefault(type, 0L).intValue();
            Integer cap = weightProps.getProductTagCap().get(type);

            if (cap == null) continue;

            if(count > cap) {
                throw new IllegalArgumentException(capExceededMessage(type, cap));
            }
        }
    }

    private String capExceededMessage(MetaTagType type, int cap) {
        return switch (type) {
            case SEASONAL      -> "계절 태그는 최대 " + cap + "개까지 선택할 수 있습니다.";
            case PURPOSE       -> "목적 태그는 최대 " + cap + "개까지 선택할 수 있습니다.";
            case MERCHANDISING -> "기획 의도 태그는 최대 " + cap + "개까지 선택할 수 있습니다.";
            case OCCUPATION    -> "직종 태그는 최대 " + cap + "개까지 선택할 수 있습니다.";
            default            -> "태그는 최대 " + cap + "개까지 선택할 수 있습니다.";
        };
    }

    // 카테고리 메타태그 ID 반환
    private Long resolveCategoryMetaTagId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        MetaTagEntity categoryTag = metaTagRepository
                .findByNameAndType(category.getName(), MetaTagType.CATEGORY)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리의 메타태그를 찾을 수 없습니다."));

        if(categoryTag.getMetaTagStatus() != MetaTagStatus.ACTIVE) {
            throw new IllegalArgumentException("사용할 수 없는 메타태그입니다.");
        }

        return categoryTag.getId();
    }
}