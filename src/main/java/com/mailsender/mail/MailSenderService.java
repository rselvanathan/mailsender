package com.mailsender.mail;


import com.mailsender.dto.EmailMessage;

/**
 * Will be implemented by any class that want to send a certain type of Mail.
 */
public interface MailSenderService {
    void sendMail(EmailMessage emailMessage);
}
