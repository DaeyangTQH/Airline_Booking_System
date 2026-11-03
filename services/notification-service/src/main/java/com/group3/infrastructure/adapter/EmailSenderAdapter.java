package com.group3.infrastructure.adapter;

import com.group3.application.port.EmailSenderPort;
import com.group3.domain.vo.Recipient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    public EmailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(Recipient recipient, String subject, String body) {
        System.out.println(">>> BẮT ĐẦU GỬI EMAIL TỚI: " + recipient.value());

        // Bỏ try-catch, để lỗi tự động ném lên GlobalExceptionHandler
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient.value());
        message.setSubject("Airline Booking - " + subject);
        message.setText(body);

        mailSender.send(message);

        System.out.println("====== [REAL EMAIL SENT] ======");
    }
}