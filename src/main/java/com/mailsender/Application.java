package com.mailsender;

import com.mailsender.queueprocessor.QueueRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        QueueRunner runner = applicationContext.getBean(QueueRunner.class);
        runner.runQueue();
    }
}
