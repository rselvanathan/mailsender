package com.mailsender.queueprocessor;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MessageDeleterTest {

    private static final String QUEUE_NAME = "QUEUE_NAME";

    @Mock
    private AmazonSQSAsyncClient amazonSQSAsyncClientMock;

    @InjectMocks
    private MessageDeleter messageDeleter;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(messageDeleter, "queueURL", QUEUE_NAME);
    }

    @Test
    public void whenTheFutureHasAResultWithMessagesThenTryAndDeleteMessages() {
        Message messageOne = new Message();
        Message messageTwo = new Message();

        String messageOneReceipt = "oneReceipt";
        String messageTwoReceipt = "twoReceipt";
        messageOne.setReceiptHandle(messageOneReceipt);
        messageTwo.setReceiptHandle(messageTwoReceipt);

        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        List<Message> messages = Arrays.asList(messageOne, messageTwo);
        receiveMessageResult.setMessages(messages);

        CompletableFuture<Optional<ReceiveMessageResult>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(Optional.of(receiveMessageResult));

        messageDeleter.deleteMessages(completableFuture);

        // Argument Captor ordering and verify ordering done on purpose.
        DeleteMessageRequest deleteMessageOneRequest = new DeleteMessageRequest(QUEUE_NAME, messageOneReceipt);
        DeleteMessageRequest deleteMessageTwoRequest = new DeleteMessageRequest(QUEUE_NAME, messageTwoReceipt);

        ArgumentCaptor<AsyncHandler> asyncHandlerDeleteOneArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);
        ArgumentCaptor<AsyncHandler> asyncHandlerDeleteTwoArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);

        verify(amazonSQSAsyncClientMock).deleteMessageAsync(eq(deleteMessageOneRequest), asyncHandlerDeleteOneArgumentCaptor.capture());
        verify(amazonSQSAsyncClientMock).deleteMessageAsync(eq(deleteMessageTwoRequest), asyncHandlerDeleteTwoArgumentCaptor.capture());

        DeleteMessageResult deleteMessageResult = new DeleteMessageResult();

        asyncHandlerDeleteOneArgumentCaptor.getValue().onSuccess(deleteMessageOneRequest, deleteMessageResult);
        asyncHandlerDeleteTwoArgumentCaptor.getValue().onSuccess(deleteMessageTwoRequest, deleteMessageResult);
    }

    @Test
    public void whenTheFutureHasAResultWithNoMessagesWhichMeansOptionalIsEmptyThenDoNothing() {
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        receiveMessageResult.setMessages(Collections.emptyList());

        CompletableFuture<Optional<ReceiveMessageResult>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(Optional.of(receiveMessageResult));

        messageDeleter.deleteMessages(completableFuture);

        verify(amazonSQSAsyncClientMock, never()).deleteMessageAsync(any(DeleteMessageRequest.class), any(AsyncHandler.class));
    }
}