# Base 이미지
FROM openjdk:17-jdk-slim

# 여기에 각 서비스의 실제 JAR 파일 이름을 정확히 명시합니다.
# Jenkins 빌드 로그의 'ls -al ./build/libs' 결과에서 확인한 실행 가능한 JAR 파일 이름을 사용하세요.
# 예시: config-service의 Dockerfile이라면
COPY build/libs/discovery-service-0.0.1-SNAPSHOT.jar app.jar
# 예시: ordering-service의 Dockerfile이라면
# COPY build/libs/ordering-service-0.0.1-SNAPSHOT.jar app.jar
# 예시: user-service의 Dockerfile이라면
# COPY build/libs/user-service-0.0.1-SNAPSHOT.jar app.jar


RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]