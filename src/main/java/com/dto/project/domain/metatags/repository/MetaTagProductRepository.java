package com.dto.project.domain.metatags.repository;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaTagProductRepository extends JpaRepository<MetaTagProduct, Long> {

    List<MetaTagProduct> findByProduct_Id(Long productId);
    void deleteByProduct_Id(Long productId);
}
