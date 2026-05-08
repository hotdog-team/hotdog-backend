package com.dto.project.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[D-TO] 비밀번호 재설정 안내드립니다.");
        message.setText("안녕하세요. [D-TO] 임직원 전용 스토어입니다.\n\n" +
                "아래 링크를 클릭하여 비밀번호 재설정을 완료해 주세요.\n" +
                "링크 유효 시간은 30분입니다.\n\n" +
                resetLink + "\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.");

        try {
            mailSender.send(message);
            log.info("비밀번호 재설정 메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("메일 발송 중 오류 발생: {}", e.getMessage());
            // 실제 운영 시에는 여기서 사용자 정의 예외를 던지는 방향으로 수정
        }
    }
}