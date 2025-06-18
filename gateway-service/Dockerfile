FROM openjdk:17-jdk-slim
# ------------------------------------------------------------------
# <--- 여기가 가장 중요한 부분입니다. 'gateway-service/' 부분을 제거해야 합니다.
COPY build/libs/gateway-service-0.0.1-SNAPSHOT.jar app.jar
# ------------------------------------------------------------------

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-jar", "/app.jar"]