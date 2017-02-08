FROM java:openjdk-8-jre-alpine
ADD target/mailsender-1.0.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]