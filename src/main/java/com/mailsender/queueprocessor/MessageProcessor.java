package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.factories.MailSenderServiceProducer;
import com.mailsender.util.JSONMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final MailSenderServiceProducer mailSenderServiceFactory;

    private final JSONMapper jsonMapper;

    @Autowired
    public MessageProcessor(MailSenderServiceProducer service, JSONMapper jsonMapper) {
        mailSenderServiceFactory = service;
        this.jsonMapper = jsonMapper;
    }

    public CompletableFuture<Void> getMessageProcessingFuture(List<Message> messages) {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        messages.forEach(message -> {
            AppType appType = AppType.valueOf(message.getMessageAttributes().get("apptype").getStringValue());
            EmailMessage emailMessage = getMessage(message.getBody(), appType);
            CompletableFuture<Void> futureSendMail = CompletableFuture
                .runAsync(() -> mailSenderServiceFactory.getMailSenderService(appType).sendMail(emailMessage));
            completableFutures.add(futureSendMail);
        });
        if(completableFutures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Object[] objects = completableFutures.toArray();
        return CompletableFuture.allOf((CompletableFuture<Void>[]) objects);
    }

    private EmailMessage getMessage(String messageBody, AppType appType) {
        if(appType == AppType.ROMCHARM) {
            return jsonMapper.getObjectFromJSONString(messageBody, RomCharmEmail.class);
        } else {
            throw new IllegalArgumentException("The App type has not been recognised");
        }
    }
}
