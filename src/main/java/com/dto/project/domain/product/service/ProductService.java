package com.dto.project.domain.product.service;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getProductList(ProductSearchCondition condition);

    ProductResponse getProductDetail(Long productId);
}