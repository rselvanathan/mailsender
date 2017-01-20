package com.mailsender.queueprocessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Will run the queue processing on a scheduled time delay. This will run continuously until stopped
 */
@Component
public class QueueRunner {
    private final QueueProcessor processor;
    private final ScheduledExecutorService executorService;

    @Autowired
    public QueueRunner(QueueProcessor processor,
                       @Qualifier("scheduledExecutor") ScheduledExecutorService service) {
        this.processor = processor;
        executorService = service;
    }

    public void runQueue() {
        executorService.scheduleWithFixedDelay(processor, 1,60, TimeUnit.SECONDS);
    }
}
