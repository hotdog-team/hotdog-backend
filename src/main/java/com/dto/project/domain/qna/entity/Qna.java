package com.dto.project.domain.qna.entity;

import com.dto.project.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "qnas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QnaStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Qna(Member member, String title, String content) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.status = QnaStatus.WAITING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 관리자 답변 등록 시 상태 변경
    public void addAnswer(String answer) {
        this.answer = answer;
        this.status = QnaStatus.ANSWERED;
        this.updatedAt = LocalDateTime.now();
    }

    // 논리적 삭제
    public void softDelete() {
        this.status = QnaStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
}