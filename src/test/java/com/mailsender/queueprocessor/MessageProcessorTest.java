package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.dto.producers.EmailProducer;
import com.mailsender.mail.producers.MailSenderServiceProducer;
import com.mailsender.mail.RomCharmMailService;
import com.mailsender.util.JSONMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageProcessorTest {

    private static final String APP_TYPE = "ROMCHARM";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final String DEFAULT_EMAIL_JSON = "{\"email\":\"romesh1305@googlemail.com\",\"firstName\":\"Romesh\",\"lastName\":\"Selvanathan\",\"areAttending\":true,\"numberAttending\":1}";

    private static final String DEFAULT_FULL_SQS_JSON = "{\"Message\":\"{\\\"email\\\":\\\"romesh1305@googlemail.com\\\",\\\"firstName\\\":\\\"Romesh\\\",\\\"lastName\\\":\\\"Selvanathan\\\",\\\"areAttending\\\":true,\\\"numberAttending\\\":1}\",\"MessageAttributes\":{\"apptype\":{\"Type\":\"String\",\"Value\":\"ROMCHARM\"}}}";

    private RomCharmEmail EXPECTED_DEFAULT_EMAIL;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RomCharmMailService romCharmMailServiceMock;
    @Mock
    private MailSenderServiceProducer mailSenderServiceProducer;
    @Mock
    private EmailProducer emailProducer;
    private MessageProcessor messageProcessor;

    @Before
    public void setup() throws IOException {
        EXPECTED_DEFAULT_EMAIL = objectMapper.readValue(DEFAULT_EMAIL_JSON, RomCharmEmail.class);
        JSONMapper jsonMapper = new JSONMapper(objectMapper);
        messageProcessor = new MessageProcessor(mailSenderServiceProducer, jsonMapper, executorService, emailProducer);
    }

    @Test
    public void whenReceivingMessagesAndThereAreNoneInTheListDoNothing() {
        List<Message> messages = Collections.emptyList();
        CompletableFuture<Void> future = messageProcessor.processMessagesAsync(messages);
        future.join();
        verify(mailSenderServiceProducer, never()).getMailSenderService(any(AppType.class));
    }

    @Test
    public void whenMessageListHasOneMessageThenOnlyCallMailSenderProducerOnce() throws IOException {
        List<Message> messages = Collections.singletonList(new Message().withBody(DEFAULT_FULL_SQS_JSON));

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);
        when(emailProducer.getEmailMessage(AppType.ROMCHARM, DEFAULT_EMAIL_JSON)).thenReturn(EXPECTED_DEFAULT_EMAIL);

        CompletableFuture<Void> futures = messageProcessor.processMessagesAsync(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(1)).sendMail(EXPECTED_DEFAULT_EMAIL);
    }

    @Test
    public void whenMessageListHasTwoMessagesThenOnlyCallMailSenderProducerTwice() throws IOException {
        Message message = new Message().withBody(DEFAULT_FULL_SQS_JSON);

        List<Message> messages = Arrays.asList(message, message);

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);
        when(emailProducer.getEmailMessage(AppType.ROMCHARM, DEFAULT_EMAIL_JSON)).thenReturn(EXPECTED_DEFAULT_EMAIL);

        CompletableFuture<Void> futures = messageProcessor.processMessagesAsync(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(2)).sendMail(EXPECTED_DEFAULT_EMAIL);
    }
}