package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.producers.EmailProducer;
import com.mailsender.mail.producers.MailSenderServiceProducer;
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

/**
 * Will go through a list of SQS Messages and send a mail to the appropriate e-mail addresses.
 * Each message will contain a raw SNS message body, which this object will go process to retrieve the E-mail details body.
 */
@Component
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final MailSenderServiceProducer mailSenderServiceFactory;

    private final JSONMapper jsonMapper;

    private final ExecutorService executorService;

    private final EmailProducer emailProducer;

    @Autowired
    public MessageProcessor(MailSenderServiceProducer service,
                            JSONMapper jsonMapper,
                            @Qualifier("fixedThreadPool") ExecutorService executorService, EmailProducer emailProducer) {
        mailSenderServiceFactory = service;
        this.jsonMapper = jsonMapper;
        this.executorService = executorService;
        this.emailProducer = emailProducer;
    }

    /**
     * Asynchronous message processing function, that will create a separate thread for each message to be sent.
     * @param messages List of {@link Message}
     * @return A {@link CompletableFuture} representing the different mailing jobs
     */
    CompletableFuture<Void> processMessagesAsync(List<Message> messages) {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        logger.info(String.format("Processing %s of messages", messages.size()));
        messages.forEach(message -> addMailSendTask(completableFutures, message));
        if(completableFutures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture<?>[completableFutures.size()]));
    }

    /**
     * Generate a Mail task (thread) for a single message.
     * @param completableFutures List of {@link CompletableFuture}
     * @param message The {@link Message} containing the E-mail details
     */
    private void addMailSendTask(List<CompletableFuture<Void>> completableFutures, Message message) {
        JsonNode jsonNode = jsonMapper.getJsonNode(message.getBody());
        AppType appType = AppType.valueOf(getAppTypeString(jsonNode));
        String jsonMessageBody = jsonNode.get("Message").textValue();
        EmailMessage emailMessage = emailProducer.getEmailMessage(appType, jsonMessageBody);
        logger.info("Processing message : " + jsonMessageBody);
        completableFutures.add(CompletableFuture.runAsync(() -> mailSenderServiceFactory.getMailSenderService(appType).sendMail(emailMessage),
                executorService));
    }

    /**
     * Get the App type string from the Raw {@link JsonNode}
     * @param messageBodyNode The raw SQS {@link JsonNode}
     * @return The String representing an Application type
     */
    private String getAppTypeString(JsonNode messageBodyNode) {
        Map<String, LinkedHashMap<String, String>> map = jsonMapper.convertValue(messageBodyNode.get("MessageAttributes"), Map.class);
        return map.get("apptype").get("Value");
    }
}
