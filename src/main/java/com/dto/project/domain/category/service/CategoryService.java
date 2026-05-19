package com.dto.project.domain.category.service;

import com.dto.project.domain.category.dto.CategoryRequest;
import com.dto.project.domain.category.dto.CategoryResponse;
import com.dto.project.domain.category.entity.Category;
import com.dto.project.domain.category.entity.CategoryStatus;
import com.dto.project.domain.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Primary
@Transactional
public class CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    //카테고리 목록 조회
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(){
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    //카테고리 추가
    public void insertCategory(CategoryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category entity = Category.builder()
                .name(request.getName())
                .status(CategoryStatus.ACTIVE)
                .updatedAt(now)
                .build();
        categoryRepository.save(entity);
    }

    //카테고리 수정
    public void updateCategory(Long id, CategoryRequest request){
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        entity.setName(request.getName());
        entity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(entity);
    }

    //카테고리 삭제(개념 삭제)
    public void deleteCategory(Long id){
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        entity.setStatus(CategoryStatus.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(entity);
    }

    //카테고리 view
    @Transactional(readOnly = true)
    public CategoryResponse detailViewCategory(Long id){
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        return CategoryResponse.from(entity);
    }
}
