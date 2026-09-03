package com.bosu.housebook.featurerequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 기능 요청에 답변이 달렸을 때 작성자에게 알림 메일을 보낸다. {@code spring.mail.host}가
 * 비어있으면(로컬 개발 등, SMTP 계정을 아직 안 만든 경우) 메일을 시도하지 않고 조용히
 * 건너뛴다 — 국토부/카카오 API 키가 없을 때와 같은 방식. 실제 발송이 실패해도(자격 증명
 * 오류 등) 답변 저장 자체는 이미 끝난 뒤라 예외를 삼키고 로그만 남긴다.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String text) {
        if (mailHost == null || mailHost.isBlank() || to == null || to.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("메일 발송에 실패했습니다. to={}", to, e);
        }
    }
}
