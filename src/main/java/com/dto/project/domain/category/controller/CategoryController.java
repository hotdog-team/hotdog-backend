package com.dto.project.domain.category.controller;

import com.dto.project.domain.category.dto.CategoryRequest;
import com.dto.project.domain.category.dto.CategoryResponse;
import com.dto.project.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    //카테고리 리스트 조회
    @GetMapping("/categories")
    public List<CategoryResponse> listCategories() {
        return categoryService.listCategories();
    }

    //카테고리 상세 조회
    @GetMapping("/categories/{id}")
    public CategoryResponse getCategoryDetail(@PathVariable("id") Long id){
        return categoryService.detailViewCategory(id);
    }

    //카테고리 등록
    @PostMapping("/admin/categories")
    public ResponseEntity<Void> insertCategory(@Valid @RequestBody CategoryRequest request){
        categoryService.insertCategory(request);
        return ResponseEntity.ok().build();
    }

    //카테고리 수정
    @PatchMapping("/admin/categories/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryRequest request){
        categoryService.updateCategory(id, request);
        return ResponseEntity.ok().build();
    }

    //카테고리 삭제
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable("id") Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
