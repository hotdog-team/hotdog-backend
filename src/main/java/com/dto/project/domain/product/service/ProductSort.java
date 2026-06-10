package com.dto.project.domain.product.service;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductSort {
    RECOMMEND, LATEST, SALES, LOW_PRICE, HIGH_PRICE, POPULAR, ATTENTION;

    //프론트에서 소문자로 보내어도 인식하도록 enum 처리
    @JsonCreator
    public static ProductSort fromJson(String s) {
        if (s == null) return RECOMMEND;
        return ProductSort.valueOf(s.trim().toUpperCase());
    }
}
