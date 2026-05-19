package com.dto.project.domain.category.repository;

import com.dto.project.domain.category.entity.Category;
import com.dto.project.domain.category.entity.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByStatus(CategoryStatus status);
}
