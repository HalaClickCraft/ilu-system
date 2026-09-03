package com.ilu.system.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.test-recipient:#{null}}")
    private String testRecipient;

    @Value("${spring.mail.username:#{null}}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String toAddress, String subject, String body) {
        String target = (testRecipient != null && !testRecipient.isBlank()) ? testRecipient : toAddress;

        if (target == null || target.isBlank()) {
            log.warn("Email dispatch skipped: No recipient address specified (target email is empty)");
            return false;
        }

        if (mailSender == null) {
            log.info("MAIL SENDER NOT ACTIVE - Logged email notification to [{}]: {}\nBody: {}", target, subject, body);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(target);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Successfully sent email notification to [{}] for subject: {}", target, subject);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to [{}]: {}", target, e.getMessage(), e);
            return false;
        }
    }
}
