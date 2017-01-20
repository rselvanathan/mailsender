package com.mailsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Will clean up the thread pools once the Application has been stopped. Will listen to the Spring Application context
 * to kick off the clean up.
 */
@Component
public class ThreadPoolCleaner implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolCleaner.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService fixedThreadPool;

    @Autowired
    public ThreadPoolCleaner(@Qualifier("scheduledExecutor") ScheduledExecutorService service,
                             @Qualifier("fixedThreadPool") ExecutorService pool) {
        scheduledExecutorService = service;
        fixedThreadPool = pool;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Cleaning Thread pool before shutdown.");
        scheduledExecutorService.shutdown();
        fixedThreadPool.shutdown();
    }
}
