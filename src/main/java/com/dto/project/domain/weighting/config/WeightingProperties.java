package com.dto.project.domain.weighting.config;

import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.weighting.entity.WeightLogType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "weighting")
public class WeightingProperties {

    //로그 점수 및 계수 저장
    private Map<WeightLogType, Double> actionWeight = new EnumMap<>(WeightLogType.class);
    private Map<MetaTagType, Double>   metaTagCoefficient  = new EnumMap<>(MetaTagType.class);

    private double occupationBonus;
    private int productTagCap;
    private int profileInitialScore = 20;
    private int profileOccupationScore = 5;
    private int profileAgeScore = 5;

    private View view = new View();
    private Bookmark bookmark = new Bookmark();

    //DB 저장 직전 동일 action 중복 방지 (eventTimeStamp 기준)
    private Duration actionDedup = Duration.ofSeconds(1);

    //조회시 시간 관련
    @Getter
    @Setter
    public static class View {
        private Duration stayTimes;
        private Duration dedup;
    }

    //BookMark 시간 관련
    @Getter
    @Setter
    public static class Bookmark {
        private Duration confirmDelay;
    }
}