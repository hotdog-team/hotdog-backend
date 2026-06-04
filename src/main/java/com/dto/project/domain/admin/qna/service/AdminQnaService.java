package com.dto.project.domain.admin.qna.service;

import com.dto.project.domain.admin.qna.dto.AdminQnaDto;
import com.dto.project.domain.qna.entity.Qna;
import com.dto.project.domain.qna.entity.QnaStatus;
import com.dto.project.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQnaService {

    private final QnaRepository qnaRepository;

    // 1. 관리자: 상태별 Q&A 목록 조회
    public Page<AdminQnaDto.ListResponse> getQnaList(QnaStatus status, Pageable pageable) {
        if (status != null) {
            // 특정 상태만 조회
            return qnaRepository.findByStatusNot(QnaStatus.DELETED, pageable)
                    .map(AdminQnaDto.ListResponse::new);
        }
        // 전체 조회
        return qnaRepository.findByStatusNot(QnaStatus.DELETED, pageable)
                .map(AdminQnaDto.ListResponse::new);
    }

    // 2. 관리자: 특정 Q&A 상세 조회
    public AdminQnaDto.DetailResponse getQnaDetail(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));
        return new AdminQnaDto.DetailResponse(qna);
    }

    // 3. 관리자: 문의 답변 등록
    @Transactional
    public void answerQna(Long qnaId, AdminQnaDto.AnswerRequest request) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));

        qna.addAnswer(request.getAnswer());
    }

    // 4. 관리자: 부적절한 문의글 강제 삭제
    @Transactional
    public void softDeleteQna(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));

        qna.softDelete();
    }
}