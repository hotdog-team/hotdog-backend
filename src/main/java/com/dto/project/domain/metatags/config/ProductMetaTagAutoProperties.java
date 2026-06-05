package com.dto.project.domain.metatags.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "product-meta-tag.auto")
public class ProductMetaTagAutoProperties {
    private int steadySellerMinSales = 100;
    private int discountRateMin = 50;
    private int popularTopPercent = 5;
    private int trendingTopPercent = 5;
    private Duration popularMaxInactive = Duration.ofDays(180);
    private Duration trendingDays = Duration.ofDays(14);
    private Duration releaseNewDays = Duration.ofDays(7);
    private Duration releaseUpdateDays = Duration.ofDays(3);

}
