package com.dto.project.domain.metatags.controller;

import com.dto.project.domain.metatags.dto.MetaTagRequest;
import com.dto.project.domain.metatags.dto.MetaTagResponse;
import com.dto.project.domain.metatags.service.MetaTagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MetaTagController {
    private final MetaTagService metaTagService;

    public MetaTagController(MetaTagService metaTagService){
        this.metaTagService = metaTagService;
    }

    //메타태그 조회
    @GetMapping("/api/tags")
    public List<MetaTagResponse> listAllMetaTags() {
        return metaTagService.listAllMetaTag();
    }

    //메타태그 상세 조회
    @GetMapping("/api/tags/{id}")
    public MetaTagResponse getTagDetail(@PathVariable("id") Long id){
        return metaTagService.detailViewMetaTag(id);
    }

    //메타태그 등록
    @PostMapping("/api/admin/tags")
    public ResponseEntity<Void> insertTag(@Valid @RequestBody MetaTagRequest request) {
        metaTagService.insertMetaTag(request);
        return ResponseEntity.ok().build();
    }

    //메타태그 수정
    @PatchMapping("/api/admin/tags/{id}")
    public ResponseEntity<Void> updateTag(
            @PathVariable("id") Long id,
            @Valid @RequestBody MetaTagRequest request){
        metaTagService.updateMetaTag(id, request);
        return ResponseEntity.ok().build();
    }

    //메타태그 삭제
    @DeleteMapping("/api/admin/tags/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable("id") Long id){
        metaTagService.deleteMetaTag(id);
        return ResponseEntity.ok().build();
    }

}
