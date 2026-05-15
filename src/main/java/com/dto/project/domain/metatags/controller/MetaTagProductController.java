package com.dto.project.domain.metatags.controller;

import com.dto.project.domain.metatags.dto.MetaTagProductReplaceRequest;
import com.dto.project.domain.metatags.dto.MetaTagResponse;
import com.dto.project.domain.metatags.service.MetaTagProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MetaTagProductController {

    private final MetaTagProductService metaTagProductService;
    public MetaTagProductController(MetaTagProductService metaTagProductService) {
        this.metaTagProductService = metaTagProductService;
    }

    //상품 id에 따른 tags 목록 조회
    @GetMapping("/api/products/{id}/tags")
    public List<MetaTagResponse> listTagsByProduct(@PathVariable("id") Long productId) {
        return metaTagProductService.findAllByProductId(productId);
    }

    //메타태그 일괄 매핑 업데이트
    @PostMapping("/api/admin/products/{id}/tags")
    public ResponseEntity<Void> replaceTags(@PathVariable("id") Long productId,
                                            @Valid @RequestBody MetaTagProductReplaceRequest request) {
        metaTagProductService.replaceMetaTagsForProduct(productId, request.getMetaTagIds());
        return ResponseEntity.ok().build();
    }

}
