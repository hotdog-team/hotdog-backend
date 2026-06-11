package com.dto.project.domain.payment.controller;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.payment.dto.PaymentConfirmRequest;
import com.dto.project.domain.payment.service.PaymentService;
import com.dto.project.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    private Member getLoginMember() {
        Long memberId = securityUtil.resolveMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
                );
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(
            @RequestBody PaymentConfirmRequest request
    ) {
        Member member = getLoginMember();

        paymentService.processPayment(request, member);

        return ResponseEntity.ok().build();
    }
}