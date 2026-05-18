package com.dto.project.domain.member.service;

import com.dto.project.domain.member.dto.MemberUpdateRequest;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberTagWeight;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.member.repository.MemberTagWeightRepository;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberTagWeightRepository memberTagWeightRepository;
    private final MetaTagRepository metaTagRepository;
    private final StringRedisTemplate redisTemplate;

    // 1. 회원 정보 및 취향 설정 변경
    @Transactional
    public void updateProfile(String email, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 기본 인적사항 및 핵심 목적 ID 업데이트
        member.updateProfile(request.getName(), request.getJobType(), request.getPurposeId(), request.getIsJobRecommendEnabled());

        // 이용 목적 가중치 정보 동기화
        if (request.getPurposeId() != null) {
            memberTagWeightRepository.deleteByMemberAndMetaTagType(member, MetaTagType.PURPOSE);
            metaTagRepository.findById(request.getPurposeId()).ifPresent(tag ->
                    memberTagWeightRepository.save(MemberTagWeight.builder().member(member).metaTag(tag).weightScore(20).build())
            );
        }

        // 쇼핑 취향(MERCHANDISING) 가중치 정보 복수 동기화
        if (request.getMerchandisingTagIds() != null) {
            memberTagWeightRepository.deleteByMemberAndMetaTagType(member, MetaTagType.MERCHANDISING);
            for (Long tagId : request.getMerchandisingTagIds()) {
                metaTagRepository.findById(tagId).ifPresent(tag ->
                        memberTagWeightRepository.save(MemberTagWeight.builder().member(member).metaTag(tag).weightScore(20).build())
                );
            }
        }
    }

    // 2. 회원 탈퇴 처리
    @Transactional
    public void withdraw(String email, String accessToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 회원 테이블의 상태값을 WITHDRAWN으로 변경 (Soft Delete)
        member.withdraw();

        // 개인화 데이터 가중치 인덱스 일괄 삭제
        memberTagWeightRepository.deleteAllByMember(member);

        // Redis 저장소의 세션 및 토큰 정보 즉시 파기
        redisTemplate.delete("RT:" + email); // 리프레시 토큰 제거
        if (accessToken != null) {
            // 기존 발급된 인증 토큰은 만료 전까지 재사용 불가능하도록 블랙리스트 등록 (유효시간 2시간 설정)
            redisTemplate.opsForValue().set(accessToken, "withdrawn", 2, TimeUnit.HOURS);
        }
    }
}