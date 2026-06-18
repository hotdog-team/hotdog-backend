package com.dto.project.domain.metatags.dto;

import com.dto.project.domain.metatags.entity.MetaTagProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MetaTagProductResponse {
    private Long id;
    private Long productId;
    private MetaTagResponse metaTag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MetaTagProductResponse from(MetaTagProduct entity) {
        if (entity == null) {
            return null;
        }
        return MetaTagProductResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .metaTag(entity.getMetaTag() != null ? MetaTagResponse.from(entity.getMetaTag()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }


}
