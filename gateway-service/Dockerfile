FROM openjdk:17-jdk-slim
COPY gateway-service/build/libs/gateway-service-0.0.1-SNAPSHOT.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-jar", "/app.jar"]