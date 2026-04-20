FROM gradle:8.14-jdk17 AS builder

WORKDIR /workspace

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle build.gradle ./
COPY common common
COPY app app

RUN ./gradlew :app:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/app/build/libs/*.jar /app/authz-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/authz-service.jar"]
