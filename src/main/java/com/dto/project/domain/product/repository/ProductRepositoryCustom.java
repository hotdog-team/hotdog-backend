package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;

import java.util.List;

public interface ProductRepositoryCustom {

    List<ProductResponse> searchProducts(ProductSearchCondition condition);

    ProductResponse findProductDetail(Long productId);
}