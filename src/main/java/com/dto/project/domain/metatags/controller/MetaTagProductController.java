package com.dto.project.domain.metatags.controller;

import com.dto.project.domain.metatags.dto.MetaTagResponse;
import com.dto.project.domain.metatags.service.MetaTagProductService;
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

}
