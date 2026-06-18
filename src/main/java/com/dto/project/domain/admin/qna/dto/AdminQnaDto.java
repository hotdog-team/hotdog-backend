package com.dto.project.domain.admin.qna.dto;

import com.dto.project.domain.qna.entity.Qna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class AdminQnaDto {

    // 1. 관리자 답변 등록 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class AnswerRequest {
        private String answer;
    }

    // 2. 관리자용 목록 조회 응답 DTO
    @Getter
    public static class ListResponse {
        private Long id;
        private String memberEmail;
        private String memberName;
        private String title;
        private String writerEmail; // 작성자 이메일
        private String status;
        private LocalDateTime createdAt;

        public ListResponse(Qna qna) {
            this.id = qna.getId();
            this.memberEmail = (qna.getMember() != null) ? qna.getMember().getEmail() : "알 수 없음";
            this.memberName = qna.getMember().getName();
            this.title = qna.getTitle();
            this.writerEmail = qna.getMember().getEmail();
            this.status = qna.getStatus().name();
            this.createdAt = qna.getCreatedAt();
        }
    }

    // 3. 관리자용 상세 조회 응답 DTO
    @Getter
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String answer;
        private String writerEmail;
        private String status;
        private LocalDateTime createdAt;

        public DetailResponse(Qna qna) {
            this.id = qna.getId();
            this.title = qna.getTitle();
            this.content = qna.getContent();
            this.answer = qna.getAnswer();
            this.writerEmail = qna.getMember().getEmail();
            this.status = qna.getStatus().name();
            this.createdAt = qna.getCreatedAt();
        }
    }
}