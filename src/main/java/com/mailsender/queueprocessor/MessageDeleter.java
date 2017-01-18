package com.mailsender.queueprocessor;


import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class MessageDeleter {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeleter.class);

    private final AmazonSQSAsyncClient amazonSQSAsyncClient;

    @Value("${AWS_SQS_QUEUE_URL}")
    private String queueURL;

    @Autowired
    public MessageDeleter(AmazonSQSAsyncClient amazonSQSAsyncClient) {
        this.amazonSQSAsyncClient = amazonSQSAsyncClient;
    }

    public void deleteMessages(CompletableFuture<Optional<ReceiveMessageResult>> completableFuture) {
        completableFuture.thenAccept(optionalResult -> {
            if (optionalResult.isPresent()) {
                ReceiveMessageResult receiveMessageResult = optionalResult.get();
                List<CompletableFuture<DeleteMessageResult>> deleteFutures = new ArrayList<>(receiveMessageResult.getMessages().size());
                receiveMessageResult.getMessages().forEach(message -> {
                    CompletableFuture<DeleteMessageResult> deleteFuture = new CompletableFuture<>();
                    DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueURL, message.getReceiptHandle());
                    amazonSQSAsyncClient.deleteMessageAsync(deleteMessageRequest, asyncMessageDeleteHandler(deleteFuture));
                    deleteFutures.add(deleteFuture);
                });
                CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[deleteFutures.size()]));
            }
        });
    }

    private AsyncHandler<DeleteMessageRequest, DeleteMessageResult> asyncMessageDeleteHandler(
            final CompletableFuture<DeleteMessageResult> deleteFuture) {
        return new AsyncHandler<DeleteMessageRequest, DeleteMessageResult>() {
            @Override
            public void onError(Exception e) {
                logger.error("Error when trying to delete message", e);
                deleteFuture.completeExceptionally(e);
            }

            @Override
            public void onSuccess(DeleteMessageRequest request, DeleteMessageResult deleteMessageResult) {
                deleteFuture.complete(deleteMessageResult);
            }
        };
    }
}
