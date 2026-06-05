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

    @Column(name = "profile_score", nullable = false)
    private Integer profileScore;

    @Column(name = "weight_score", nullable = false)
    private Integer weightScore;

    @Column(name = "effective_score", nullable = false)
    private Double effectiveScore;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //프로필 수정에 따른 가중치 점수 조정 로직
    public void adjustProfileScore(int delta) {
        int current = this.profileScore != null ? this.profileScore : 0;
        this.profileScore = current + delta;
    }

    public void adjustWeightScore(int delta) {
        int current = this.weightScore != null ? this.weightScore : 0;
        this.weightScore = current + delta;
    }

    //행동 점수 0점 처리
    public boolean hasNoScore() {
        int profile = this.profileScore != null ? this.profileScore : 0;
        int weight = this.weightScore != null ? this.weightScore : 0;
        return profile <= 0 && weight <= 0;
    }

    //Setter
    public void clearBehavior() {
        this.weightScore = 0;
        this.effectiveScore = 0.0;
    }
}