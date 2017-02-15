[![Build Status](https://travis-ci.org/rselvanathan/mailsender.svg?branch=master)](https://travis-ci.org/rselvanathan/mailsender)

##Automated Mail Sender

A simple Spring Boot mailing application that will use AWS SQS to process messages and send e-mails to the appropriate destinations.
Each type of Application will have it's own template defined, which will then be used to send a specific message to a user.

The AWS SQS is polled at a given interval. The application itself is asynchronous and will try to process multiple messages
at the same time. The thread pool can be controlled at application start up, so based on the number of messages this, can be 
increased or decreased per user preference.

This app has been replaced by the Scala version in production : https://github.com/rselvanathan/mailsender-scala

Tech Used : AWS SQS, Docker, Java 8, Spring Boot

#### Docker Usage

The application was deployed and used as a docker image (Before being replaced by Scala version). To run the docker image simply run the following command :

```bash
docker run -d --name mailsender \
-e AWS_ACCESS_KEY_ID= \
-e AWS_SECRET_ACCESS_KEY= \
-e MAIL_HOST= \
-e MAIL_PORT= \
-e MAIL_USERNAME= \
-e MAIL_PASSWORD= \
-e AWS_SQS_QUEUE_URL= \
-e THREADPOOL_SIZE= \
-it rselvanathan/mailsender:latest
```

Filling in the details as required.

###### Author

Romesh Selvanathan
