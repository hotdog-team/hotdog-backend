package com.dto.project.domain.product.service;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductListResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductListResponse> getProductList(ProductSearchCondition condition) {
        return productRepository.searchProducts(condition);
    }

    @Override
    public ProductResponse getProductDetail(Long productId) {
        return productRepository.findProductDetail(productId);
    }
}