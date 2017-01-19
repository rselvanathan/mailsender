package com.mailsender.queueprocessor;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.factories.MailSenderServiceProducer;
import com.mailsender.mail.RomCharmMailService;
import com.mailsender.util.JSONMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RomCharmMailService romCharmMailServiceMock;

    @Mock
    private MailSenderServiceProducer mailSenderServiceProducer;

    private MessageProcessor messageProcessor;

    @Before
    public void setup() {
        JSONMapper jsonMapper = new JSONMapper(objectMapper);
        messageProcessor = new MessageProcessor(mailSenderServiceProducer, jsonMapper, executorService);
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
        String value = "{\"Message\":\"{\\\"email\\\":\\\"romesh1305@googlemail.com\\\",\\\"firstName\\\":\\\"Romesh\\\",\\\"lastName\\\":\\\"Selvanathan\\\",\\\"areAttending\\\":true,\\\"numberAttending\\\":1}\",\"MessageAttributes\":{\"apptype\":{\"Type\":\"String\",\"Value\":\"ROMCHARM\"}}}";
        String second = "{\"email\":\"romesh1305@googlemail.com\",\"firstName\":\"Romesh\",\"lastName\":\"Selvanathan\",\"areAttending\":true,\"numberAttending\":1}";
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        JsonNode jsonNodeInner = Mockito.mock(JsonNode.class);
        when(jsonNode.get("Message")).thenReturn(jsonNodeInner);
        when(jsonNodeInner.asText()).thenReturn(second);
        RomCharmEmail romCharmEmail = objectMapper.readValue(second, RomCharmEmail.class);

        List<Message> messages = Collections.singletonList(new Message().withBody(value));

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);

        CompletableFuture<Void> futures = messageProcessor.processMessagesAsync(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(1)).sendMail(romCharmEmail);
    }

    @Test
    public void whenMessageListHasTwoMessagesThenOnlyCallMailSenderProducerTwice() throws IOException {
        String value = "{\"Message\":\"{\\\"email\\\":\\\"romesh1305@googlemail.com\\\",\\\"firstName\\\":\\\"Romesh\\\",\\\"lastName\\\":\\\"Selvanathan\\\",\\\"areAttending\\\":true,\\\"numberAttending\\\":1}\",\"MessageAttributes\":{\"apptype\":{\"Type\":\"String\",\"Value\":\"ROMCHARM\"}}}";
        String second = "{\"email\":\"romesh1305@googlemail.com\",\"firstName\":\"Romesh\",\"lastName\":\"Selvanathan\",\"areAttending\":true,\"numberAttending\":1}";
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        JsonNode jsonNodeInner = Mockito.mock(JsonNode.class);
        when(jsonNode.get("Message")).thenReturn(jsonNodeInner);
        when(jsonNodeInner.asText()).thenReturn(second);
        RomCharmEmail romCharmEmail = objectMapper.readValue(second, RomCharmEmail.class);

        Message message = new Message().withBody(value);

        List<Message> messages = Arrays.asList(message, message);

        when(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM)).thenReturn(romCharmMailServiceMock);

        CompletableFuture<Void> futures = messageProcessor.processMessagesAsync(messages);

        futures.join();

        verify(mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM), times(2)).sendMail(romCharmEmail);
    }

    @Test
    public void whenObjectContainsDataNotExpectedThrowIllegalArguementException() throws JsonProcessingException {
        String value = "{\"Message\":{\"email\":\"romesh1305@googlemail.com\",\"firstName\":\"Romesh\",\"lastName\":\"Selvanathan\",\"areAttending\":true,\"numberAttending\":1},\"MessageAttributes\":{\"apptype\":{\"Type\":\"String\",\"Value\":\"DEFAULT\"}}}";

        Message message = new Message().withBody(value);
        List<Message> messages = Arrays.asList(message, message);

        expectedException.expect(IllegalArgumentException.class);

        messageProcessor.processMessagesAsync(messages);
    }
}