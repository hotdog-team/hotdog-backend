package com.dto.project.domain.review.dto;

import lombok.Getter;

@Getter
public class ReviewCreateRequest {

    private Integer rating;
    private String content;
    private String imageUrl;
}