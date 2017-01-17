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
public class QueueProcessor implements Callable<CompletableFuture<Boolean>> {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);

    private final AmazonSQSAsyncClient amazonSQSAsyncClient;

    @Value("${AWS_SQS_QUEUE_ARN}")
    private String queueARN;

    @Autowired
    public QueueProcessor(AmazonSQSAsyncClient client) {
        amazonSQSAsyncClient = client;
    }

    @Override
    public CompletableFuture<Boolean> call() throws Exception {
        ReceiveMessageRequest request = new ReceiveMessageRequest(queueARN);
        request.setMaxNumberOfMessages(5);
        CompletableFuture<Optional<ReceiveMessageResult>> futureMessages = new CompletableFuture<>();

        amazonSQSAsyncClient.receiveMessageAsync(request, asyncMessagesReceiveHandler(futureMessages));

        futureMessages.thenComposeAsync(optionalResult -> {
            CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
            if(!optionalResult.isPresent()) {
                resultFuture.complete(false);
                return resultFuture;
            }
            ReceiveMessageResult messageResult = optionalResult.get();

            return resultFuture;
        });

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
                future.complete(Optional.of(result));
            }
        };
    }
}
