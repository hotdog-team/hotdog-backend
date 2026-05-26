package com.dto.project.domain.search.controller;

import com.dto.project.domain.search.dto.PopularKeywordResponse;
import com.dto.project.domain.search.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @PostMapping("/log")
    public String saveSearchKeyword(@RequestParam String keyword) {
        searchKeywordService.saveSearchKeyword(keyword);
        return "검색어 저장 완료";
    }

    @GetMapping("/popular")
    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchKeywordService.getPopularKeywords();
    }
}