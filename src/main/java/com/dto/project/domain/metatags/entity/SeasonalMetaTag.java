package com.dto.project.domain.metatags.entity;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

/** meta_tags SEASONAL id 27~38 */
public enum SeasonalMetaTag {

    EARLY_SPRING(27, "초봄"),
    SPRING(28, "봄"),
    SUMMER(29, "여름"),
    EARLY_AUTUMN(30, "초가을"),
    AUTUMN(31, "가을"),
    EARLY_WINTER(32, "초겨울"),
    WINTER(33, "겨울"),
    FLOWER_COLD(34, "꽃샘추위"),
    MONSOON(35, "장마"),
    YEAR_END(36, "연말"),
    YEAR_START(37, "연초"),
    HOLIDAY(38, "명절");

    private final long metaTagId;
    private final String dbName;

    SeasonalMetaTag(long metaTagId, String dbName) {
        this.metaTagId = metaTagId;
        this.dbName = dbName;
    }

    public long getMetaTagId() {
        return metaTagId;
    }

    public boolean isActive(LocalDate date) {
        MonthDay d = MonthDay.from(date);
        return switch (this) {
            case EARLY_SPRING -> between(d, 2, 15, 3, 20);
            case SPRING -> between(d, 3, 21, 5, 31);
            case SUMMER -> between(d, 6, 1, 8, 31);
            case EARLY_AUTUMN -> between(d, 9, 1, 9, 20);
            case AUTUMN -> between(d, 9, 21, 11, 20);
            case EARLY_WINTER -> between(d, 11, 21, 12, 15);
            case WINTER -> betweenWrap(d, 12, 16, 2, 14);
            case FLOWER_COLD -> between(d, 2, 20, 3, 15);
            case MONSOON -> between(d, 6, 25, 7, 25);
            case YEAR_END -> between(d, 12, 15, 12, 31);
            case YEAR_START -> between(d, 1, 1, 1, 15);
            case HOLIDAY -> between(d, 1, 1, 2, 29) || between(d, 9, 1, 10, 31);
        };
    }

    public static Optional<SeasonalMetaTag> fromMetaTagId(long metaTagId) {
        for (SeasonalMetaTag tag : values()) {
            if (tag.metaTagId == metaTagId) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

    private static boolean between(MonthDay d, int m1, int day1, int m2, int day2) {
        return !d.isBefore(MonthDay.of(m1, day1)) && !d.isAfter(MonthDay.of(m2, day2));
    }

    private static boolean betweenWrap(MonthDay d, int m1, int day1, int m2, int day2) {
        return !d.isBefore(MonthDay.of(m1, day1)) || !d.isAfter(MonthDay.of(m2, day2));
    }
}
