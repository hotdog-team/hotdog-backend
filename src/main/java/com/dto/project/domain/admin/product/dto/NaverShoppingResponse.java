package com.dto.project.domain.admin.product.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NaverShoppingResponse {
	
	// 응답 생성 시간
    private String lastBuildDate;
    // 전체 검색 결과 수
    private Integer total;
    // 시작 위치
    private Integer start;
    // 조회 개수
    private Integer display;
    // 상품 목록
    private List<NaverShoppingItem> items;
}