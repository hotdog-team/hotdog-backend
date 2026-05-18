package com.dto.project.domain.product.service;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductListResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;

import java.util.List;

public interface ProductService {

    List<ProductListResponse> getProductList(ProductSearchCondition condition);

    ProductResponse getProductDetail(Long productId);
}