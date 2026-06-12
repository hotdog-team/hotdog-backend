package com.dto.project.domain.search.dto;

import com.dto.project.domain.search.entity.SearchKeyword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PopularKeywordResponse {

    private String keyword;
    private Integer searchCount;

    public static PopularKeywordResponse from(SearchKeyword searchKeyword) {
        return PopularKeywordResponse.builder()
                .keyword(searchKeyword.getKeyword())
                .searchCount(searchKeyword.getSearchCount())
                .build();
    }
}