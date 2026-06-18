package com.dto.project.domain.weighting.config;

public class ViewRepeatScorePolicy {

    public static final String REDIS_KEY_PREFIX = "view:repeat:";

    //redis Key 사용
    public static String redisKey(Long memberId, Long productId) {
        return REDIS_KEY_PREFIX + memberId + ":" + productId;
    }

    public static double pointsForCount(long count) {
        if (count <= 0) return 0.0;
        if (count <= 5) return 1.0;
        if (count <= 10) return 0.5;
        return 0.1;
    }
}
