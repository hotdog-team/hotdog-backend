package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Product> searchProducts(ProductSearchCondition condition) {
        String jpql = "SELECT p FROM Product p WHERE p.status = 'ON_SALE'";
        return em.createQuery(jpql, Product.class).getResultList();
    }

    @Override
    public ProductResponse findProductDetail(Long productId) {
        Product product = em.find(Product.class, productId);

        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }

        return new ProductResponse(product);
    }
}