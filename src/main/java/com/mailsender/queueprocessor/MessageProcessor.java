package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.factories.MailSenderServiceProducer;
import com.mailsender.util.JSONMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final MailSenderServiceProducer mailSenderServiceFactory;

    private final JSONMapper jsonMapper;

    private final ExecutorService executorService;

    @Autowired
    public MessageProcessor(MailSenderServiceProducer service,
                            JSONMapper jsonMapper,
                            @Qualifier("fixedThreadPool") ExecutorService executorService) {
        mailSenderServiceFactory = service;
        this.jsonMapper = jsonMapper;
        this.executorService = executorService;
    }

    CompletableFuture<Void> processMessagesAsync(List<Message> messages) {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        messages.forEach(message -> {
            JsonNode jsonNode = jsonMapper.getJsonNode(message.getBody());
            AppType appType = AppType.valueOf(getAppTypeString(jsonNode));
            String messageBody = getMessageBody(jsonNode);
            EmailMessage emailMessage = getMessage(messageBody, appType);
            logger.info("Processing message : " + messageBody);
            completableFutures.add(
                CompletableFuture.runAsync(
                    () -> mailSenderServiceFactory.getMailSenderService(appType).sendMail(emailMessage),
                    executorService));
        });
        if(completableFutures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture<?>[completableFutures.size()]));
    }

    private EmailMessage getMessage(String messageBody, AppType appType) {
        if(appType.equals(AppType.ROMCHARM)) {
            return jsonMapper.getObjectFromJSONString(messageBody, RomCharmEmail.class);
        } else {
            throw new IllegalArgumentException("The App type has not been recognised");
        }
    }

    private String getAppTypeString(JsonNode messageBodyNode) {
        Map<String, LinkedHashMap<String, String>> map = jsonMapper.convertValue(messageBodyNode.get("MessageAttributes"), Map.class);
        return map.get("apptype").get("Value");
    }

    private String getMessageBody(JsonNode messageBodyNode) {
        return messageBodyNode.get("Message").textValue();
    }
}
