package com.dto.project.domain.search.scheduler;

import com.dto.project.domain.search.entity.SearchKeyword;
import com.dto.project.domain.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SearchKeywordScheduler {

    private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchKeywordRepository searchKeywordRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushPopularKeywordsToDb() {
        Set<ZSetOperations.TypedTuple<String>> keywords =
                redisTemplate.opsForZSet()
                        .rangeWithScores(POPULAR_KEYWORDS_KEY, 0, -1);

        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> keywordData : keywords) {
            String keyword = keywordData.getValue();
            Double score = keywordData.getScore();

            if (keyword == null || score == null) {
                continue;
            }

            int count = score.intValue();

            SearchKeyword searchKeyword = searchKeywordRepository.findByKeyword(keyword)
                    .orElseGet(() -> SearchKeyword.builder()
                            .keyword(keyword)
                            .build());

            searchKeyword.addCount(count);
            searchKeywordRepository.save(searchKeyword);
        }

        redisTemplate.delete(POPULAR_KEYWORDS_KEY);
    }
}