package com.dto.project.domain.weighting.repository;

import com.dto.project.domain.weighting.entity.ProductWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductWeightLogRepository extends JpaRepository<ProductWeightLog, Long> {

}
