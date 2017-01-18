package com.mailsender.queueprocessor;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Component
public class QueueProcessor implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);

    private final AmazonSQSAsyncClient amazonSQSAsyncClient;

    private final MessageProcessor messageProcessor;

    private final MessageDeleter messageDeleter;

    @Value("${AWS_SQS_QUEUE_URL}")
    private String queueURL;

    @Autowired
    public QueueProcessor(AmazonSQSAsyncClient client, MessageProcessor messageProcessor, MessageDeleter messageDeleter) {
        amazonSQSAsyncClient = client;
        this.messageProcessor = messageProcessor;
        this.messageDeleter = messageDeleter;
    }

    @Override
    public Void call() throws Exception {
        ReceiveMessageRequest request = new ReceiveMessageRequest(queueURL);
        request.setMaxNumberOfMessages(5);
        CompletableFuture<Optional<ReceiveMessageResult>> futureMessages = new CompletableFuture<>();

        amazonSQSAsyncClient.receiveMessageAsync(request, asyncMessagesReceiveHandler(futureMessages));

        // First Process the message
        CompletableFuture<Optional<ReceiveMessageResult>> futureMessagesCopy = futureMessages.thenComposeAsync(optionalResult -> {
            CompletableFuture<Optional<ReceiveMessageResult>> resultFuture = new CompletableFuture<>();
            if (!optionalResult.isPresent()) {
                resultFuture.complete(optionalResult);
                return resultFuture;
            }
            ReceiveMessageResult messageResult = optionalResult.get();
            return messageProcessor
                    .getMessageProcessingFuture(messageResult.getMessages())
                    .thenComposeAsync(voidObject -> {
                        resultFuture.complete(optionalResult);
                        return resultFuture;
                    });
        });

        messageDeleter.deleteMessages(futureMessagesCopy);
        return null;
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
