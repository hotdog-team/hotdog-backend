package com.dto.project.domain.product.service;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.product.dto.ProductPageResponse;
import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductListResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import com.dto.project.domain.weighting.service.MemberWeightScoreReadService;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MetaTagProductRepository metaTagProductRepository;
    private final MemberWeightScoreReadService memberWeightScoreReadService;
    private final SecurityUtil securityUtil;
    private final StringRedisTemplate redisTemplate;
    
    // 상품 목록 조회
    @Override
    public ProductPageResponse getProductList(ProductSearchCondition condition) {
        ProductSort sort = ProductSort.fromJson(condition.getSort());

        int size = condition.getSize() != null ? condition.getSize() : 20;
        int page = condition.getPage() != null ? condition.getPage() : 0;

        Long memberId = securityUtil.resolveMemberId();

        condition.setSize(null);

        List<Product> products = productRepository.searchProducts(condition);

        // 검색 시에는 당분간 보지 않기가 적용되지 않도록 한다
        boolean isKeywordSearch = condition.getKeyword() != null && !condition.getKeyword().isBlank();

        if (!products.isEmpty() && !isKeywordSearch) {
            Set<String> hidden = redisTemplate.opsForZSet()
                    .rangeByScore("dislike:hide:" + memberId, System.currentTimeMillis(), Double.MAX_VALUE);
            if (hidden != null && !hidden.isEmpty()) {
                Set<Long> hiddenIds = hidden.stream().map(Long::parseLong).collect(Collectors.toSet());
                products = products.stream()
                        .filter(p -> !hiddenIds.contains(p.getId()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        int totalElements = products.size();

        if (sort != ProductSort.RECOMMEND && sort != ProductSort.ATTENTION) {
            
            //sort 방식에 따라 다르게 sort처리
            Comparator<Product> comparator = switch (sort) {
                case LATEST    -> Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getId, Comparator.reverseOrder());
                case LOW_PRICE -> Comparator.comparing(Product::getSalePrice, Comparator.nullsLast(Comparator.naturalOrder()));
                case HIGH_PRICE-> Comparator.comparing(Product::getSalePrice, Comparator.nullsLast(Comparator.reverseOrder()));
                case SALES     -> Comparator.comparing(Product::getSalesCount, Comparator.nullsLast(Comparator.reverseOrder()));
                case POPULAR   -> Comparator.comparing(Product::getWeightScore, Comparator.nullsLast(Comparator.reverseOrder()));
                default        -> Comparator.comparing(Product::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
            };

            List<ProductListResponse> content = products.stream()
                    .sorted(comparator)
                    .skip((long) page * size)
                    .limit(size)
                    .map(ProductListResponse::new)
                    .toList();
            return new ProductPageResponse(content, totalElements, page, size);
        }

        if (sort == ProductSort.ATTENTION) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
            List<ProductListResponse> content = products.stream()
                    .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(cutoff))
                    .sorted(Comparator.comparing(Product::getSalesCount, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(4)
                    .map(ProductListResponse::new)
                    .toList();
            return new ProductPageResponse(content, content.size(), 0, 4);
        }

        Map<Long, Double> tagScoreMap = memberWeightScoreReadService.getEffectiveTagWeights(memberId);
        List<Long> productIds = products.stream().map(Product::getId).toList();
        List<MetaTagProduct> mappings = metaTagProductRepository.findAllByProductIdInWithMetaTag(productIds);

        Map<Long, List<Long>> productTagIdsMap = mappings.stream()
                .collect(Collectors.groupingBy(
                        mp -> mp.getProduct().getId(),
                        Collectors.mapping(mp -> mp.getMetaTag().getId(), Collectors.toList())
                ));

        Map<Long, Double> productScoreMap = new HashMap<>();
        for (Product product : products) {
            List<Long> tagIds = productTagIdsMap.getOrDefault(product.getId(), List.of());
            double score = 0;
            for (Long tagId : tagIds) {
                score += tagScoreMap.getOrDefault(tagId, 0.0);
            }
            productScoreMap.put(product.getId(), score);
        }

        products.sort(Comparator
                .comparingDouble((Product p) -> productScoreMap.getOrDefault(p.getId(), 0.0))
                .reversed()
                .thenComparing(Product::getWeightScore, Comparator.nullsLast(Comparator.reverseOrder())));

        List<ProductListResponse> content = products.stream()
                .skip((long) page * size)
                .limit(size)
                .map(ProductListResponse::new)
                .toList();

        return new ProductPageResponse(content, totalElements, page, size);
    }
    
    // 상품 상세 조회
    @Override
    public ProductResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        List<Long> metaTagIds = metaTagProductRepository.findByProduct_Id(productId)
                .stream()
                .map(mapping -> mapping.getMetaTag().getId())
                .collect(Collectors.toList());

        return new ProductResponse(product, metaTagIds);
    }
    
    // 관련 상품 조회
    @Override
    public List<ProductResponse> getRelatedProducts(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다.")
                );

        List<Product> relatedProducts =
                productRepository.findTop4ByCategoryIdAndStatusAndIdNot(
                        product.getCategoryId(),
                        "ON_SALE",
                        productId
                );

        return relatedProducts.stream()
                .map(ProductResponse::new)
                .toList();
    }
}
