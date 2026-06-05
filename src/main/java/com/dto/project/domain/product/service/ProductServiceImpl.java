package com.dto.project.domain.product.service;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
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

    @Override
    public List<ProductListResponse> getProductList(ProductSearchCondition condition) {
        ProductSort sort = ProductSort.fromJson(condition.getSort());

        Long memberId = securityUtil.resolveMemberId();
        List<Product> products = productRepository.searchProducts(condition);

        // DISLIKE된 상품 숨김
        if (!products.isEmpty()) {
            Set<String> hidden = redisTemplate.opsForZSet()
                    .rangeByScore("dislike:hide:" + memberId, System.currentTimeMillis(), Double.MAX_VALUE);
            if (hidden != null && !hidden.isEmpty()) {
                Set<Long> hiddenIds = hidden.stream().map(Long::parseLong).collect(Collectors.toSet());
                products = products.stream()
                        .filter(p -> !hiddenIds.contains(p.getId()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        //기본 정렬이 아니라면 searchProducts에서 처리합니다
        //DISLIKE 처리로 수정
        if (sort != ProductSort.RECOMMEND) {
            return products.stream()
                    .sorted(Comparator.comparing(Product::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(ProductListResponse::new)
                    .toList();
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

        for(Product product: products){
            List<Long> tagIds = productTagIdsMap.getOrDefault(product.getId(), List.of());
            double score = 0;
            for (Long tagId:tagIds){
                score += tagScoreMap.getOrDefault(tagId, 0.0);
            }
            productScoreMap.put(product.getId(), score);
        }

        products.sort(Comparator
                .comparingDouble((Product p) -> productScoreMap.getOrDefault(p.getId(), 0.0))
                .reversed()
                .thenComparing(Product::getWeightScore, Comparator.nullsLast(Comparator.reverseOrder())));

        return products.stream()
                .map(ProductListResponse::new)
                .toList();
    }

    @Override
    public ProductResponse getProductDetail(Long productId) {
        return productRepository.findProductDetail(productId);
    }
}
