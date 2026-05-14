package com.dto.project.domain.metatags.dto;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.entity.MetaTagType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MetaTagResponse {
    private Long id;
    private String name;
    private MetaTagType type;
    private MetaTagStatus metaTagStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MetaTagResponse from(MetaTagEntity entity) {
        if (entity == null) {
            return null;
        }
        return MetaTagResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .metaTagStatus(entity.getMetaTagStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
