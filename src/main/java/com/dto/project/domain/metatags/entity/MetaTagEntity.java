package com.dto.project.domain.metatags.entity;

import com.dto.project.domain.metatags.dto.MetaTagDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="meta_tags")
public class MetaTagEntity {
    @Id
    private int id;
    private String name;
    @Enumerated(EnumType.STRING)
    private MetaTagType type;
    @Enumerated(EnumType.STRING)
    private MetaTagStatus status;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public static MetaTagEntity toEntity(MetaTagDTO dto){
        return MetaTagEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .status(dto.getStatus())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
