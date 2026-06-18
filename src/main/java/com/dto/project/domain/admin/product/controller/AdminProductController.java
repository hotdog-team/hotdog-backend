package com.dto.project.domain.admin.product.controller;

import com.dto.project.domain.admin.product.dto.AdminProductRequest;
import com.dto.project.domain.admin.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    // 1. 상품 등록 (단건)
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody AdminProductRequest request) {
        adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. 상품 등록 (다건)
    @PostMapping("/bulk")
    public ResponseEntity<Void> createProductsBulk(@RequestBody List<AdminProductRequest> requests) {
        adminProductService.createProductsBulk(requests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 3. 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId,
            @RequestBody AdminProductRequest request) {
        adminProductService.updateProduct(productId, request);
        return ResponseEntity.ok().build();
    }

    // 4. 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}