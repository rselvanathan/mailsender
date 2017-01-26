FROM anapsix/alpine-java:8
ADD target/mailsender-1.0.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Xms64M -Xmx64M -jar /app.jar"]