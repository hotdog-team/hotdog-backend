package com.dto.project.domain.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 발송 중 서버 오류가 발생했습니다.");
        }
    }
}