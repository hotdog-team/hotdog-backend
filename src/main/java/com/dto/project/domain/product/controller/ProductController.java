package com.dto.project.domain.product.controller;

import com.dto.project.domain.product.dto.ProductPageResponse;
import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    
    // 상품 목록 조회
    @GetMapping
    public ProductPageResponse getProductList(ProductSearchCondition condition) {
        return productService.getProductList(condition);
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ProductResponse getProductDetail(@PathVariable Long productId) {
        return productService.getProductDetail(productId);
    }
    
    // 관련 상품 조회
    @GetMapping("/{productId}/related")
    public List<ProductResponse> getRelatedProducts(
            @PathVariable Long productId
    ) {
        return productService.getRelatedProducts(productId);
    }
    
    
}