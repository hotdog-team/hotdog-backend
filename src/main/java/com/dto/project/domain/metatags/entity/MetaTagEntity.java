package com.dto.project.domain.metatags.entity;

import com.dto.project.domain.metatags.dto.MetaTagRequest;
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
    @GeneratedValue
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private MetaTagType type;
    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private MetaTagStatus metaTagStatus;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public static MetaTagEntity toEntity(MetaTagRequest request){
        return MetaTagEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .metaTagStatus(request.getMetaTagStatus())
                .build();
    }
}
