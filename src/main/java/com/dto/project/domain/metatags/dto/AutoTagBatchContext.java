package com.dto.project.domain.metatags.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
@Builder
public class AutoTagBatchContext {
    @Builder.Default
    private final Set<Long> popularProductIds = Collections.emptySet();
    @Builder.Default
    private final Set<Long> trendingProductIds = Collections.emptySet();
}
