package com.mailsender.mail.producers;

import com.mailsender.defaults.AppType;
import com.mailsender.mail.MailSenderService;
import com.mailsender.mail.RomCharmMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Mail Sender Service producer that will generate a {@link MailSenderService} based on the {@link AppType} provided
 */
@Component
public class MailSenderServiceProducer {

    private final MailSenderProducer mailSenderProducer;

    @Autowired
    public MailSenderServiceProducer(MailSenderProducer mailSenderProducer) {
        this.mailSenderProducer = mailSenderProducer;
    }

    public MailSenderService getMailSenderService(AppType appType) {
        if (appType.equals(AppType.ROMCHARM)) {
            return new RomCharmMailService(mailSenderProducer.mailSender());
        } else {
            throw new IllegalArgumentException("Application type not recognised");
        }
    }
}
