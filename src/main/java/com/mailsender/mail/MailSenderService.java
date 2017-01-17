package com.mailsender.mail;


import com.mailsender.dto.EmailMessage;

public interface MailSenderService {
    void sendMail(EmailMessage emailMessage);
}
