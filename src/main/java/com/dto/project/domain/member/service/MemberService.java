package com.dto.project.domain.member.service;

import com.dto.project.domain.address.entity.Address;
import com.dto.project.domain.address.repository.AddressRepository;
import com.dto.project.domain.member.dto.MemberResponse;
import com.dto.project.domain.member.dto.MemberUpdateRequest;
import com.dto.project.domain.member.dto.PasswordUpdateRequest;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.weighting.service.MemberTagWeightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberTagWeightService memberTagWeightService;
    private final StringRedisTemplate redisTemplate;
    private final AddressRepository addressRepository;

    // 비밀번호 검증 및 암호화를 위한 인코더 주입
    private final PasswordEncoder passwordEncoder;

    // 1. 회원 정보 및 취향 설정 변경
    @Transactional
    public void updateProfile(String email, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 기본 인적사항(연락처 phone 추가됨) 업데이트
        member.updateProfile(request.getName(), request.getPhone(), request.getAgeRange(),request.getJobType(), request.getIsJobRecommendEnabled());

        if (request.getZipCode() != null || request.getBaseAddress() != null || request.getDetailAddress() != null) {
            Address address = addressRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElse(Address.builder()
                            .member(member)
                            .isDefault(true)
                            .build());

            address.updateAddress(request.getZipCode(), request.getBaseAddress(), request.getDetailAddress());
            addressRepository.save(address);
        }

        // 가중치 동기화를 Weighting 도메인쪽으로 이관
        memberTagWeightService.syncFromProfileUpdate(member, request.getProfileTagIds(), request.getAgeRange(), request.getJobType(), request.getIsJobRecommendEnabled());
    }

    // 2. 회원 탈퇴 처리
    @Transactional
    public void withdraw(String email, String accessToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 회원 테이블의 상태값을 WITHDRAWN으로 변경 (Soft Delete)
        member.withdraw();

        // 개인화 데이터 가중치 인덱스 일괄 삭제
        memberTagWeightService.deleteAllForMember(member);

        // Redis 저장소의 세션 및 토큰 정보 즉시 파기
        redisTemplate.delete("RT:" + email); // 리프레시 토큰 제거
        if (accessToken != null) {
            // 기존 발급된 인증 토큰은 만료 전까지 재사용 불가능하도록 블랙리스트 등록 (유효시간 2시간 설정)
            redisTemplate.opsForValue().set(accessToken, "withdrawn", 2, TimeUnit.HOURS);
        }
    }

    // 3. 회원 정보 조회 (프로필 페이지용)
    @Transactional(readOnly = true)
    public MemberResponse getProfile(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 주소 조회
        Address address = addressRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                .orElse(null);

        // MemberResponse DTO에 매핑
        return MemberResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .jobType(member.getJobType())
                .ageRange(member.getAgeRange())
                .profileTagIds(memberTagWeightService.findProfileTagIds(member))
                .isJobRecommendEnabled(member.isJobRecommendEnabled())
                .zipCode(address != null ? address.getZipCode() : null)
                .baseAddress(address != null ? address.getBaseAddress() : null)
                .detailAddress(address != null ? address.getDetailAddress() : null)
                .build();
    }

    // 4. 비밀번호 변경 로직
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 현재 비밀번호가 맞는지 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 업데이트
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}