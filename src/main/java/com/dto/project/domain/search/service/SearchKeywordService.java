package com.dto.project.domain.search.service;

import com.dto.project.domain.search.dto.PopularKeywordResponse;
import com.dto.project.domain.search.entity.SearchKeyword;
import com.dto.project.domain.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchKeywordService {
	
	 private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";

    private final SearchKeywordRepository searchKeywordRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    public void saveSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String trimmedKeyword = keyword.trim();

        redisTemplate.opsForZSet()
                .incrementScore(POPULAR_KEYWORDS_KEY, trimmedKeyword, 1);
    }
    @Transactional(readOnly = true)
    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchKeywordRepository.findTop10ByOrderBySearchCountDescUpdatedAtDesc()
                .stream()
                .map(PopularKeywordResponse::from)
                .toList();
    }
}