package com.dto.project.domain.qna.dto;

import com.dto.project.domain.qna.entity.Qna;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class QnaDto {

    // 1. 등록 요청용 DTO
    @Getter
    @NoArgsConstructor
    public static class Request {
        private String title;
        private String content;
    }

    // 2. 조회 응답용 DTO
    @Getter
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String answer;
        private String status;
        private LocalDateTime createdAt;

        public Response(Qna qna) {
            this.id = qna.getId();
            this.title = qna.getTitle();
            this.content = qna.getContent();
            this.answer = qna.getAnswer();
            this.status = qna.getStatus().name();
            this.createdAt = qna.getCreatedAt();
        }
    }
}