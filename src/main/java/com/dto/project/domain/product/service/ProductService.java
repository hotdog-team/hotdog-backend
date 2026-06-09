package com.dto.project.domain.product.service;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductListResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;

import java.util.List;

public interface ProductService {
	
	// 상품 목록 조회
    List<ProductListResponse> getProductList(ProductSearchCondition condition);
    
    // 상품 상세 조회
    ProductResponse getProductDetail(Long productId);
    
    // 관련 상품 조회
    List<ProductResponse> getRelatedProducts(Long productId);
}