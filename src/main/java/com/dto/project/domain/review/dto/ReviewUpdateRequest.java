package com.dto.project.domain.review.dto;

import lombok.Getter;

@Getter
public class ReviewUpdateRequest {

    private Integer rating;
    private String content;
    private String imageUrl;
}