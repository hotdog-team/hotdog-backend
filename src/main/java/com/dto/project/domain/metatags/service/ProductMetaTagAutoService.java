package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.config.ProductMetaTagAutoProperties;
import com.dto.project.domain.metatags.dto.AutoTagBatchContext;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.weighting.repository.ProductWeightLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductMetaTagAutoService {

    private final ProductRepository productRepository;
    private final MetaTagRepository metaTagRepository;
    private final MetaTagProductRepository metaTagProductRepository;
    private final ProductMetaTagAutoProperties autoProps;

    private static final Set<MetaTagType> AUTO_TYPES = EnumSet.of(
            MetaTagType.POPULARITY,
            MetaTagType.RELEASE_OR_UPDATE
    );

    // 오토 태그 추가
    @Transactional
    public void syncAutoTagsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. ID: " + productId));
        syncAutoTagsForProduct(product, buildContext());
    }

    @Transactional
    public void syncAllAutoTags() {
        AutoTagBatchContext context = buildContext();
        List<Product> products = productRepository.findByStatus("ON_SALE");
        for (Product product : products) {
            syncAutoTagsForProduct(product, context);
        }
    }

    private void syncAutoTagsForProduct(Product product, AutoTagBatchContext context) {
        // 전부 삭제 처리
        removeAutoTags(product.getId());

        // ON_SALE이 아니라면 이대로 return
        if (!"ON_SALE".equals(product.getStatus())) {
            return;
        }

        List<Long> autoTagIds = evaluateAutoTagIds(product, context);
        insertAutoTags(product.getId(), autoTagIds);
    }

    // 각 메타태그에 맞춰 처리
    private List<Long> evaluateAutoTagIds(Product product, AutoTagBatchContext context) {
        List<Long> ids = new ArrayList<>();

        resolveDiscountSaleTag(product).ifPresent(ids::add);
        resolveReleaseTag(product).ifPresent(ids::add);
        resolvePopularTag(product, context).ifPresent(ids::add);
        resolveTrendingTag(product, context).ifPresent(ids::add);
        resolveSteadySellerTag(product).ifPresent(ids::add);

        return ids.stream().distinct().toList();
    }

    // 자동 태그들을 전부 제거한다
    private void removeAutoTags(Long productId) {
        metaTagProductRepository.deleteByProduct_IdAndMetaTag_TypeIn(productId, AUTO_TYPES);

        //flush 강제 처리
        metaTagProductRepository.flush();
    }

    // 자동 태그들을 추가한다
    private void insertAutoTags(Long productId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<Long> distinctIds = tagIds.stream().distinct().toList();
        Product productRef = productRepository.getReferenceById(productId);
        LocalDateTime now = LocalDateTime.now();

        List<MetaTagProduct> rows = distinctIds.stream()
                .map(tagId -> MetaTagProduct.builder()
                        .product(productRef)
                        .metaTag(metaTagRepository.getReferenceById(tagId))
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .toList();
        metaTagProductRepository.saveAll(rows);
    }

    // 특가 세일
    private Optional<Long> resolveDiscountSaleTag(Product product) {
        int discountRate = product.getDiscountRate() != null ? product.getDiscountRate() : 0;
        if (discountRate < autoProps.getDiscountRateMin()) {
            return Optional.empty();
        }

        return metaTagRepository
                .findByNameAndType("특가 세일", MetaTagType.POPULARITY)
                .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                .map(MetaTagEntity::getId);
    }

    // 신상품 or 업데이트 태그
    private Optional<Long> resolveReleaseTag(Product product) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = product.getCreatedAt();
        LocalDateTime updatedAt = product.getUpdatedAt();

        if (createdAt != null && !createdAt.isBefore(now.minusDays(7))) {
            return metaTagRepository
                    .findByNameAndType("신상품", MetaTagType.RELEASE_OR_UPDATE)
                    .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                    .map(MetaTagEntity::getId);
        }

        if (createdAt != null
                && createdAt.isBefore(now.minusDays(7))
                && updatedAt != null
                && !updatedAt.isBefore(now.minusDays(3))
        ) {
            return metaTagRepository
                    .findByNameAndType("업데이트", MetaTagType.RELEASE_OR_UPDATE)
                    .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                    .map(MetaTagEntity::getId);
        }

        return Optional.empty();
    }

    // 스테디 셀러
    private Optional<Long> resolveSteadySellerTag(Product product) {

        long salesCount = product.getSalesCount() != null ? product.getSalesCount() : 0L;

        if (salesCount < autoProps.getSteadySellerMinSales()) {
            return Optional.empty();
        }

        return metaTagRepository
                .findByNameAndType("스테디셀러", MetaTagType.POPULARITY)
                .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                .map(MetaTagEntity::getId);
    }

    // 인기 상품 - weight_score 상위 5%, 마지막 로그 6개월 이내
    private Optional<Long> resolvePopularTag(Product product, AutoTagBatchContext context) {
        if (!context.getPopularProductIds().contains(product.getId())) {
            return Optional.empty();
        }

        return metaTagRepository
                .findByNameAndType("인기 상품", MetaTagType.POPULARITY)
                .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                .map(MetaTagEntity::getId);
    }

    // 주목 상품 - 로그가 최근 기준으로 많이 쌓인 상품
    private Optional<Long> resolveTrendingTag(Product product, AutoTagBatchContext context) {
        if (!context.getTrendingProductIds().contains(product.getId())) {
            return Optional.empty();
        }

        return metaTagRepository
                .findByNameAndType("주목 상품", MetaTagType.POPULARITY)
                .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                .map(MetaTagEntity::getId);
    }

    // AutoTag BatchContext - 배치 시작 시 repo SQL 1회 호출 결과(id Set) 보관
    private AutoTagBatchContext buildContext() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime popularActiveSince = now.minus(autoProps.getPopularMaxInactive());
        LocalDateTime trendingSince = now.minus(autoProps.getTrendingDays());

        Set<Long> popularProductIds = new HashSet<>(
                productRepository.findPopularProductIds(popularActiveSince, autoProps.getPopularTopPercent())
        );
        Set<Long> trendingProductIds = new HashSet<>(
                productRepository.findTrendingProductIds(trendingSince, autoProps.getTrendingTopPercent())
        );

        return AutoTagBatchContext.builder()
                .popularProductIds(popularProductIds)
                .trendingProductIds(trendingProductIds)
                .build();
    }
}
