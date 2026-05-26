package com.dto.project.domain.weighting.controller;

import com.dto.project.domain.weighting.dto.ProductWeightLogRequest;
import com.dto.project.domain.weighting.service.ProductWeightLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ProductWeightLogController {
    private final ProductWeightLogService productWeightLogService;

    //로그 보내기
    @PostMapping("/behavior")
    public ResponseEntity<Void> view(@RequestBody ProductWeightLogRequest request) {
        //리퀘스트 전체 보냄
        productWeightLogService.recordLogs(request);
        return ResponseEntity.noContent().build();
    }


}
