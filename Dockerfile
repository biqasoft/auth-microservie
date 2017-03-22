FROM java:8
VOLUME /tmp
ADD target/artifact.biqasoft.com.jar app.jar

EXPOSE 8080
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "--spring.profiles.active=production", "--server.port=8080"]
