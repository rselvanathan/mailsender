package com.mailsender.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class MailSenderService {

    private final MailSender mailSender;

    @Autowired
    public MailSenderService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("romeshselvan@hotmail.co.uk");
        mailMessage.setTo("romesh69@hotmail.com");
        mailMessage.setSubject("Automated Mail test");
        mailMessage.setText("Heya \n This is a test message \n Kind Regards \n Auto Romesh");

        mailSender.send(mailMessage);
    }
}
