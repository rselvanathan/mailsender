package com.mailsender.producers;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonSQSProducer {

    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    @Bean
    public AmazonSQSAsyncClient amazonSQSAsyncClient() {
        AmazonSQSAsyncClient client = new AmazonSQSAsyncClient(new BasicAWSCredentials(accessKey, secretKey));
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return client;
    }
}
