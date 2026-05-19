package com.dto.project.domain.accessibility.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accessibility_settings")
public class Accessibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "font_size_step", nullable = false)
    private int fontSizeStep;

    @Column(name = "high_contrast_enabled", nullable = false)
    private boolean highContrastEnabled;

    @Column(name = "screen_reader_optimized", nullable = false)
    private boolean screenReaderOptimized;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
