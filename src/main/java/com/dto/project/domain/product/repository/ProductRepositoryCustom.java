package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {

    List<Product> searchProducts(ProductSearchCondition condition);

    ProductResponse findProductDetail(Long productId);
}