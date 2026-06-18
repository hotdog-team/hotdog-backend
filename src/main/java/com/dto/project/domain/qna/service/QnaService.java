package com.dto.project.domain.qna.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.qna.dto.QnaDto;
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
public class QnaService {

    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    // 1:1 문의 등록
    @Transactional
    public Long createQna(Long memberId, QnaDto.Request request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        Qna qna = Qna.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return qnaRepository.save(qna).getId();
    }

    // 내 문의 내역 페이징 조회
    public Page<QnaDto.Response> getMyQnas(Long memberId, Pageable pageable) {
        return qnaRepository.findByMemberIdAndStatusNot(memberId, QnaStatus.DELETED, pageable)
                .map(QnaDto.Response::new);
    }

    @Transactional
    // 문의 삭제
    public void deleteQna(Long memberId, Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));

        if (!qna.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 문의만 삭제할 수 있습니다.");
        }

        qna.softDelete();
    }
}