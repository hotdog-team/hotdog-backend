package com.dto.project.domain.admin.product.controller;

import com.dto.project.domain.admin.product.dto.NaverProductCreateRequest;
import com.dto.project.domain.admin.product.service.NaverShoppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products/naver")
public class NaverProductController {
	
    private final NaverShoppingService naverShoppingService;
    
    // 네이버 쇼핑 상품 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchNaverProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(naverShoppingService.searchProducts(query, page, size));
    }
    
    // 네이버 쇼핑 상품 등록
    @PostMapping
    public ResponseEntity<Void> createProductFromNaver(
            @RequestBody NaverProductCreateRequest request
    ) {

        naverShoppingService.createProductFromNaver(request);

        return ResponseEntity.ok().build();
    }
}