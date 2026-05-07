package com.dto.project.domain.metatags.dto;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.entity.MetaTagType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MetaTagDTO {
    private int id;
    private String name;
    private MetaTagType type;
    private MetaTagStatus status;
    private LocalDateTime updatedAt;

    public static MetaTagDTO toDTO(MetaTagEntity entity){
        return MetaTagDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .status(entity.getStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
