package com.dto.project.domain.qna.repository;

import com.dto.project.domain.qna.entity.Qna;
import com.dto.project.domain.qna.entity.QnaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 전체 Q&A 목록 페이징 조회(유저)
    Page<Qna> findByMemberIdAndStatusNot(Long memberId, QnaStatus status, Pageable pageable);

    // 전체 Q&A 목록 페이징 조회(관리자)
    Page<Qna> findByStatusNot(QnaStatus status, Pageable pageable);
}