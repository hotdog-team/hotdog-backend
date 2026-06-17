package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    void deleteByProductId(Long productId);

    @Query("""
            SELECT pi
            FROM ProductImage pi
            WHERE pi.productId = :productId
            ORDER BY pi.isMain DESC, pi.id ASC
            """)
    List<ProductImage> findByProductIdOrderByMainFirst(@Param("productId") Long productId);
}