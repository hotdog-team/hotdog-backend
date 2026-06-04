package com.dto.project.domain.metatags.repository;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetaTagProductRepository extends JpaRepository<MetaTagProduct, Long> {

    List<MetaTagProduct> findByProduct_Id(Long productId);
    void deleteByProduct_Id(Long productId);

    @Query("""
    SELECT mp FROM MetaTagProduct mp
    JOIN FETCH mp.metaTag
    WHERE mp.product.id IN :productIds
    """)
    List<MetaTagProduct> findAllByProductIdInWithMetaTag(@Param("productIds") List<Long> productIds);
}
