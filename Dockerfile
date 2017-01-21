FROM anapsix/alpine-java:8
ADD target/mailsender-1.0.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-Xms256M -Xmx512M"
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]