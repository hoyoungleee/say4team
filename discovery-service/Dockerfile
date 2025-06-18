FROM openjdk:17-jdk-slim
COPY discovery-service/build/libs/discovery-service-0.0.1-SNAPSHOT.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-jar", "/app.jar"]