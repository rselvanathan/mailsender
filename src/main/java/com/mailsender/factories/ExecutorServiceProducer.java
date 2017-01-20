package com.mailsender.factories;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorServiceProducer {

    @Bean
    @Qualifier("scheduledExecutor")
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean
    @Qualifier("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(3);
    }
}
