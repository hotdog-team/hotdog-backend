package com.dto.project.domain.weighting.entity;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_tag_weights")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MemberTagWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_tag_id", nullable = false)
    private MetaTagEntity metaTag;

    @Column(name = "weight_score", nullable = false)
    private Integer weightScore;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //프로필 수정에 따른 가중치 점수 조정 로직
    public void adjustWeightScore(int delta) {
        this.weightScore += delta;
    }
}