package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.dto.ProductResponse;
import com.dto.project.domain.product.dto.ProductSearchCondition;
import com.dto.project.domain.product.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Product> searchProducts(ProductSearchCondition condition) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE p.status = 'ON_SALE'");

        if (condition.getCategoryId() != null) {
            jpql.append(" AND p.categoryId = :categoryId");
        }

        boolean hasMetaTags = condition.getMetaTagIds() != null && !condition.getMetaTagIds().isEmpty();

        TypedQuery<Product> query;

        if (!hasMetaTags) {
            query = em.createQuery(jpql.toString(), Product.class);
        } else if ("all".equals(condition.getMatch())) {
            List<Long> ids = condition.getMetaTagIds();
            for (int i = 0; i < ids.size(); i++) {
                jpql.append(" AND EXISTS (SELECT mp FROM MetaTagProduct mp WHERE mp.product = p AND mp.metaTag.id = :tag").append(i).append(")");
            }
            query = em.createQuery(jpql.toString(), Product.class);
            for (int i = 0; i < ids.size(); i++) {
                query.setParameter("tag" + i, ids.get(i));
            }
        } else {
            jpql.append(" AND EXISTS (SELECT mp FROM MetaTagProduct mp WHERE mp.product = p AND mp.metaTag.id IN :tagIds)");
            query = em.createQuery(jpql.toString(), Product.class);
            query.setParameter("tagIds", condition.getMetaTagIds());
        }

        if (condition.getCategoryId() != null) {
            query.setParameter("categoryId", condition.getCategoryId());
        }
        if (condition.getSize() != null) query.setMaxResults(condition.getSize());
        return query.getResultList();
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