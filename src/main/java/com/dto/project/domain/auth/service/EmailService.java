package com.dto.project.domain.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[D-TO] 비밀번호 재설정 안내드립니다.");

            String content =
                    "<div style='font-family: sans-serif; color: #071431;'>" +
                            "  <p>안녕하세요. [D-TO] 임직원 전용 스토어입니다.</p>" +
                            "  <p>아래 링크를 클릭하여 비밀번호 재설정을 완료해 주세요.</p>" +
                            "  <p><b>링크 유효 시간은 30분입니다.</b></p>" +
                            "  <div style='margin: 25px 0;'>" +
                            "    <a href='" + resetLink + "' style='background-color: #ff4b11; color: white; padding: 12px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;'>비밀번호 재설정하기</a>" +
                            "  </div>" +
                            "  <p style='font-size: 13px; color: #6f819a;'>버튼이 눌리지 않는다면 아래 주소를 직접 복사해 사용하세요:<br/>" + resetLink + "</p>" +
                            "  <br/>" +
                            "  <p>본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.</p>" +
                            "</div>";

            helper.setText(content, true);

            mailSender.send(message);
            log.info("비밀번호 재설정 메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("메일 발송 중 오류 발생: {}", e.getMessage());
            // 실제 운영 시에는 여기서 사용자 정의 예외를 던지는 방향으로 수정
            throw new RuntimeException("메일 발송 실패", e);
        }
    }
}