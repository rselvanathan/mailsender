package com.mailsender.queueprocessor;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class QueueRunner {
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
}
