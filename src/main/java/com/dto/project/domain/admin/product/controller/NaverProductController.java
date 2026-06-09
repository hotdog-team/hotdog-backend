package com.dto.project.domain.admin.product.controller;
import com.dto.project.domain.admin.product.dto.NaverProductCreateRequest;
import com.dto.project.domain.admin.product.dto.NaverShoppingResponse;
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
    public NaverShoppingResponse searchNaverProducts(
            @RequestParam String query
    ) {
        return naverShoppingService.searchProducts(query);
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