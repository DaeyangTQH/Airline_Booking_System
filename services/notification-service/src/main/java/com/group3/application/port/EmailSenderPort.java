package com.group3.application.port;
import com.group3.domain.vo.Recipient;
public interface EmailSenderPort {
    void send(Recipient recipient, String subject, String body);
}