FROM tubopo/jdk-17
ARG JAR_FILE=target/embedded-kafka-image-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
