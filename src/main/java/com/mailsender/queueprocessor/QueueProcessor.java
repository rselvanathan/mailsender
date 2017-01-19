package com.mailsender.queueprocessor;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class QueueProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);

    private final AmazonSQSAsyncClient amazonSQSAsyncClient;

    private final MessageProcessor messageProcessor;

    private final MessageDeleter messageDeleter;

    private final ExecutorService executorService;

    @Value("${AWS_SQS_QUEUE_URL}")
    private String queueURL;

    @Autowired
    public QueueProcessor(AmazonSQSAsyncClient client,
                          MessageProcessor messageProcessor,
                          MessageDeleter messageDeleter,
                          @Qualifier("fixedThreadPool") ExecutorService service) {
        amazonSQSAsyncClient = client;
        this.messageProcessor = messageProcessor;
        this.messageDeleter = messageDeleter;
        executorService = service;
    }

    @Override
    public void run() {
        ReceiveMessageRequest request = new ReceiveMessageRequest(queueURL);
        request.setMaxNumberOfMessages(5);
        CompletableFuture<Optional<ReceiveMessageResult>> futureMessages = new CompletableFuture<>();

        amazonSQSAsyncClient.receiveMessageAsync(request, asyncMessagesReceiveHandler(futureMessages));

        CompletableFuture<Optional<ReceiveMessageResult>> futureMessagesCopy = futureMessages.thenComposeAsync(optionalResult -> {

            CompletableFuture<Optional<ReceiveMessageResult>> resultFuture = new CompletableFuture<>();
            // First Process the message
            if (!optionalResult.isPresent()) {
                resultFuture.complete(optionalResult);
                return resultFuture;
            }
            ReceiveMessageResult messageResult = optionalResult.get();
            CompletableFuture<Void> messageProcessFuture = messageProcessor.processMessagesAsync(messageResult.getMessages());
            // Once finished re-return the messages for deletion process
            return messageProcessFuture.thenComposeAsync(voidObject -> {
                        resultFuture.complete(optionalResult);
                        return resultFuture;
                    }, executorService);

        }, executorService);

        messageDeleter.deleteMessages(futureMessagesCopy);
    }

    private AsyncHandler<ReceiveMessageRequest, ReceiveMessageResult> asyncMessagesReceiveHandler(
        final CompletableFuture<Optional<ReceiveMessageResult>> future) {
        return new AsyncHandler<ReceiveMessageRequest, ReceiveMessageResult>() {
            @Override
            public void onError(Exception e) {
                logger.error("Error when trying to receive messages", e);
                future.complete(Optional.empty());
            }

            @Override
            public void onSuccess(ReceiveMessageRequest request, ReceiveMessageResult result) {
                if(result.getMessages().isEmpty()) {
                    future.complete(Optional.empty());
                } else {
                    future.complete(Optional.of(result));
                }
            }
        };
    }
}
