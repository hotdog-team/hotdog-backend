package com.dto.project.domain.search.service;

import com.dto.project.domain.search.dto.PopularKeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {

    private static final String POPULAR_KEYWORDS_KEY = "search:popular:keywords:v2";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String trimmedKeyword = keyword.trim();

        stringRedisTemplate.opsForZSet()
                .incrementScore(POPULAR_KEYWORDS_KEY, trimmedKeyword, 1);
    }

    public List<PopularKeywordResponse> getPopularKeywords() {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        return tuples.stream()
                .map(tuple -> PopularKeywordResponse.builder()
                        .keyword(tuple.getValue())
                        .searchCount(tuple.getScore() != null ? tuple.getScore().intValue() : 0)
                        .build())
                .toList();
    }
}