package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.factories.MailSenderServiceProducer;
import com.mailsender.mail.RomCharmMailService;
import com.mailsender.util.JSONMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageProcessorTest {

    private static final String APP_TYPE = "ROMCHARM";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private JSONMapper jsonMapper;

    @Mock
    private RomCharmMailService romCharmMailServiceMock;

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
    public void whenMessageListHasOneMessageThenOnlyCallMailSenderProducerOnce() throws JsonProcessingException {
        RomCharmEmail romCharmEmail = getDefaultEmail();
        String bodyJSON = getDefaultBodyJSON(romCharmEmail);
        Message message = getDefaultMessage(bodyJSON, APP_TYPE);

        List<Message> messages = Collections.singletonList(message);

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);
        when(jsonMapper.getObjectFromJSONString(bodyJSON, RomCharmEmail.class)).thenReturn(romCharmEmail);

        CompletableFuture<Void> futures = messageProcessor.getMessageProcessingFuture(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(1)).sendMail(romCharmEmail);
    }

    @Test
    public void whenMessageListHasTwoMessagesThenOnlyCallMailSenderProducerTwice() throws JsonProcessingException {
        RomCharmEmail romCharmEmail = getDefaultEmail();
        String bodyJSON = getDefaultBodyJSON(romCharmEmail);
        Message message = getDefaultMessage(bodyJSON, APP_TYPE);

        List<Message> messages = Arrays.asList(message, message);

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);
        when(jsonMapper.getObjectFromJSONString(bodyJSON, RomCharmEmail.class)).thenReturn(romCharmEmail);

        CompletableFuture<Void> futures = messageProcessor.getMessageProcessingFuture(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(2)).sendMail(romCharmEmail);
    }

    @Test
    public void whenObjectContainsDataNotExpectedThrowIllegalArguementException() throws JsonProcessingException {
        RomCharmEmail romCharmEmail = getDefaultEmail();
        String bodyJSON = getDefaultBodyJSON(romCharmEmail);
        Message message = getDefaultMessage(bodyJSON, "random");
        List<Message> messages = Arrays.asList(message, message);

        expectedException.expect(IllegalArgumentException.class);

        messageProcessor.getMessageProcessingFuture(messages);
    }

    private Message getDefaultMessage(String bodyJSON, String appValue) {
        Message message = new Message();
        HashMap<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("apptype", new MessageAttributeValue().withStringValue(appValue));
        message.setMessageAttributes(messageAttributes);

        message.setBody(bodyJSON);
        return message;
    }

    private String getDefaultBodyJSON(Object romCharmEmail) throws JsonProcessingException {
        return objectMapper.writeValueAsString(romCharmEmail);
    }

    private RomCharmEmail getDefaultEmail() {
        return new RomCharmEmail("email", "first", "last", true, 5);
    }
}