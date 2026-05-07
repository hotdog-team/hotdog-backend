package com.dto.project.domain.product.controller;

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

    @GetMapping
    public List<ProductResponse> getProductList(ProductSearchCondition condition) {
        return productService.getProductList(condition);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProductDetail(@PathVariable Long productId) {
        return productService.getProductDetail(productId);
    }
}