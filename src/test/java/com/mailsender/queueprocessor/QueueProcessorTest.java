package com.mailsender.queueprocessor;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueProcessorTest {

    private static final String QUEUE_NAME = "QUEUE_NAME";

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final int MAX_NUMBER_MESSAGES = 3;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AmazonSQSAsyncClient amazonSQSAsyncClientMock;

    @Mock
    private MessageProcessor messageProcessorMock;

    @Mock
    private MessageDeleter messageDeleterMock;
    private QueueProcessor queueProcessor;

    @Before
    public void setup() {
        queueProcessor = new QueueProcessor(amazonSQSAsyncClientMock, messageProcessorMock, messageDeleterMock, executorService);
        ReflectionTestUtils.setField(queueProcessor, "queueURL", QUEUE_NAME);
    }

    @Test
    public void whenMessageResultsAreEmptyThenDoNotCallAnyProcessingOrDeletionMethods() throws Exception {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME).withMaxNumberOfMessages(MAX_NUMBER_MESSAGES);

        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        receiveMessageResult.setMessages(Collections.emptyList());

        queueProcessor.run();

        // Argument Captor ordering and verify ordering done on purpose.
        ArgumentCaptor<AsyncHandler> asyncHandlerArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);
        verify(amazonSQSAsyncClientMock).receiveMessageAsync(eq(receiveMessageRequest), asyncHandlerArgumentCaptor.capture());

        asyncHandlerArgumentCaptor.getValue().onSuccess(receiveMessageRequest, receiveMessageResult);

        verify(messageProcessorMock, never()).processMessagesAsync(anyList());

        ArgumentCaptor<CompletableFuture> completableFutureArgumentCaptor = ArgumentCaptor.forClass(CompletableFuture.class);
        verify(messageDeleterMock).deleteMessages(completableFutureArgumentCaptor.capture());

        Optional<ReceiveMessageResult> join = (Optional<ReceiveMessageResult>) completableFutureArgumentCaptor.getValue().join();
        assertThat(join.isPresent(), is(false));
    }

    @Test
    public void whenMessageResultsAreNotEmptyCallMessageProcessorAndMessageDeletor() throws Exception {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME).withMaxNumberOfMessages(MAX_NUMBER_MESSAGES);

        Message messageOne = new Message();
        Message messageTwo = new Message();

        String messageOneReceipt = "oneReceipt";
        String messageTwoReceipt = "twoReceipt";
        messageOne.setReceiptHandle(messageOneReceipt);
        messageTwo.setReceiptHandle(messageTwoReceipt);

        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        List<Message> messages = Arrays.asList(messageOne, messageTwo);
        receiveMessageResult.setMessages(messages);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);
        when(messageProcessorMock.processMessagesAsync(messages)).thenReturn(completableFuture);

        queueProcessor.run();

        // Argument Captor ordering and verify ordering done on purpose.
        ArgumentCaptor<AsyncHandler> asyncHandlerArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);
        verify(amazonSQSAsyncClientMock).receiveMessageAsync(eq(receiveMessageRequest), asyncHandlerArgumentCaptor.capture());

        asyncHandlerArgumentCaptor.getValue().onSuccess(receiveMessageRequest, receiveMessageResult);

        ArgumentCaptor<CompletableFuture> completableFutureArgumentCaptor = ArgumentCaptor.forClass(CompletableFuture.class);
        verify(messageDeleterMock).deleteMessages(completableFutureArgumentCaptor.capture());

        Optional<ReceiveMessageResult> join = (Optional<ReceiveMessageResult>) completableFutureArgumentCaptor.getValue().join();
        assertThat(join.get(), is(receiveMessageResult));

        verify(messageProcessorMock).processMessagesAsync(messages);
    }

    @Test
    public void whenSQSClientThrowsExceptionExpectAnException() throws Exception {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME).withMaxNumberOfMessages(MAX_NUMBER_MESSAGES);

        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        receiveMessageResult.setMessages(Collections.emptyList());

        queueProcessor.run();

        // Argument Captor ordering and verify ordering done on purpose.
        ArgumentCaptor<AsyncHandler> asyncHandlerArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);
        verify(amazonSQSAsyncClientMock).receiveMessageAsync(eq(receiveMessageRequest), asyncHandlerArgumentCaptor.capture());

        asyncHandlerArgumentCaptor.getValue().onError(new IllegalArgumentException());

        verify(messageProcessorMock, never()).processMessagesAsync(anyList());
    }

    @Test
    public void whenTheMessageProcessorThrowsAnExceptionDuringProcessingExpectTheFutureReturnedToBeCompletedExceptionally() throws Exception {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME).withMaxNumberOfMessages(MAX_NUMBER_MESSAGES);

        Message messageOne = new Message();

        String messageOneReceipt = "oneReceipt";
        Throwable causeException = new IOException("Some Mail error");

        messageOne.setReceiptHandle(messageOneReceipt);

        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        List<Message> messages = Collections.singletonList(messageOne);
        receiveMessageResult.setMessages(messages);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(causeException);
        when(messageProcessorMock.processMessagesAsync(messages)).thenReturn(completableFuture);

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(causeException));

        queueProcessor.run();

        // Argument Captor ordering and verify ordering done on purpose.
        ArgumentCaptor<AsyncHandler> asyncHandlerArgumentCaptor = ArgumentCaptor.forClass(AsyncHandler.class);
        verify(amazonSQSAsyncClientMock).receiveMessageAsync(eq(receiveMessageRequest), asyncHandlerArgumentCaptor.capture());

        asyncHandlerArgumentCaptor.getValue().onSuccess(receiveMessageRequest, receiveMessageResult);

        ArgumentCaptor<CompletableFuture> completableFutureArgumentCaptor = ArgumentCaptor.forClass(CompletableFuture.class);
        verify(messageDeleterMock).deleteMessages(completableFutureArgumentCaptor.capture());

        CompletableFuture value = completableFutureArgumentCaptor.getValue();
        value.get();
    }
}