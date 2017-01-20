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
import java.util.function.BiFunction;

/**
 * Processor that will grab the messages from a SQS Queue and will try to send the messages to the appropriate e-mail
 * destination. Once the mail processing has been completed it will then delete the messages from the SQS Queue to
 * mark them as done.
 */
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
        request.setMaxNumberOfMessages(3);
        CompletableFuture<Optional<ReceiveMessageResult>> receiveResultFuture = new CompletableFuture<>();

        amazonSQSAsyncClient.receiveMessageAsync(request, asyncReceiveResultHandler(receiveResultFuture));

        CompletableFuture<Optional<ReceiveMessageResult>> receiveResultCopyFuture = receiveResultFuture.thenComposeAsync(optionalResult -> {

            CompletableFuture<Optional<ReceiveMessageResult>> optionalCopyResultFuture = new CompletableFuture<>();
            // First Process the message
            if (!optionalResult.isPresent()) {
                optionalCopyResultFuture.complete(optionalResult);
                return optionalCopyResultFuture;
            }
            ReceiveMessageResult messageResult = optionalResult.get();
            CompletableFuture<Void> messageProcessFuture = messageProcessor
                    .processMessagesAsync(messageResult.getMessages());

            // Once finished re-return the SQS Receive object for deletion process
            return messageProcessFuture
                    // Handle Exception
                    .handleAsync( handleMessageProcessorAsync(optionalCopyResultFuture, messageResult), executorService)
                    // Transform the future
                    .thenComposeAsync(voidObject -> optionalCopyResultFuture, executorService);

        }, executorService);

        messageDeleter.deleteMessages(receiveResultCopyFuture);
    }

    /**
     * A asynchronous message receive handler for the SQS call
     *
     * @param future An empty completable future to be filled with an {@link Optional} {@link ReceiveMessageResult}
     * @return a filled {@link CompletableFuture} with an {@link Optional} {@link ReceiveMessageResult}. The {@link CompletableFuture}
     * can also complete exceptionally, if an error has occured.
     */
    private AsyncHandler<ReceiveMessageRequest, ReceiveMessageResult> asyncReceiveResultHandler(
        final CompletableFuture<Optional<ReceiveMessageResult>> future) {
        return new AsyncHandler<ReceiveMessageRequest, ReceiveMessageResult>() {
            @Override
            public void onError(Exception e) {
                logger.error("Error when trying to receive messages", e);
                future.completeExceptionally(e);
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

    /**
     * An asynchronous exception handler method that will check whether or not an exception has been thrown by the Message Processor Future.
     * If an exception has been thrown then the provided empty {@link CompletableFuture} will complete exceptionally. Otherwise
     * a copy of an {@link Optional} {@link ReceiveMessageResult} will be set within the provided empty {@link CompletableFuture}.
     *
     * @param resultFuture The empty future to be filled with either the exception or the {@link Optional} {@link ReceiveMessageResult}
     * @param receiveMessageResult The {@link ReceiveMessageResult} to be added in the empty {@link CompletableFuture}
     * @return nothing
     */
    private BiFunction<? super Void, Throwable, ? extends CompletableFuture<Void>> handleMessageProcessorAsync(CompletableFuture<Optional<ReceiveMessageResult>> resultFuture,
                                                                                                               ReceiveMessageResult receiveMessageResult) {
        return (voidMessageProcessorResult, th) -> {
            if (th != null) {
                logger.error("An unexpected error has occured", th);
                resultFuture.completeExceptionally(th);
            }
            resultFuture.complete(Optional.of(receiveMessageResult));
            return null;
        };
    }
}
