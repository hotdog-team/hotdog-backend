package com.dto.project.domain.payment.controller;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.payment.dto.PaymentConfirmRequest;
import com.dto.project.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.processPayment(request, member);
        return ResponseEntity.ok().build();
    }
}