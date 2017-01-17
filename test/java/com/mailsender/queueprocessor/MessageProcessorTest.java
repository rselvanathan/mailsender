package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.factories.MailSenderServiceProducer;
import com.mailsender.util.JSONMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageProcessorTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JSONMapper jsonMapper;

    @Mock
    private MailSenderServiceProducer mailSenderServiceProducer;

    @InjectMocks
    private MessageProcessor messageProcessor;

    @Test
    public void whenReceivingMessagesAndThereAreNoneInTheListDoNothing() {
        List<Message> messages = Collections.emptyList();
        CompletableFuture<Void> future = messageProcessor.getMessageProcessingFuture(messages);
        future.join();
        verify(mailSenderServiceProducer, never()).getMailSenderService(any(AppType.class));
    }

    @Test
    public void whenMessageListHasOneMessageThenOnlyCalllMailSenderProducerOnce() throws JsonProcessingException {
        Message message = new Message();
        HashMap<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("apptype", new MessageAttributeValue().withStringValue("ROMCHARM"));
        message.setMessageAttributes(messageAttributes);

        RomCharmEmail romCharmEmail = new RomCharmEmail("email", "first", "last", true, 5);
        message.setBody(objectMapper.writeValueAsString(romCharmEmail));

        List<Message> messages = Collections.singletonList(message);

        CompletableFuture<Void> futures = messageProcessor.getMessageProcessingFuture(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(1)).sendMail(romCharmEmail);
    }
}