package com.dto.project.domain.images.controller;

import com.dto.project.domain.images.dto.ImageResponse;
import com.dto.project.domain.images.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = fileService.storeFile(file);
        return ResponseEntity.ok(new ImageResponse(imageUrl));
    }
}
