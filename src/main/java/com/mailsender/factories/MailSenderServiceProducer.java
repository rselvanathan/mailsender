package com.mailsender.factories;

import com.mailsender.defaults.AppType;
import com.mailsender.mail.MailSenderService;
import com.mailsender.mail.RomCharmMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailSenderServiceProducer {

    private final MailSenderProducer mailSenderProducer;

    @Autowired
    public MailSenderServiceProducer(MailSenderProducer mailSenderProducer) {
        this.mailSenderProducer = mailSenderProducer;
    }

    public MailSenderService getMailSenderService(AppType appType) {
        if (appType == AppType.ROMCHARM) {
            return new RomCharmMailService(mailSenderProducer.mailSender());
        } else {
            throw new IllegalArgumentException("Application type not recognised");
        }
    }
}
