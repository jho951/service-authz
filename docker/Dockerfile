# syntax=docker/dockerfile:1.7

FROM gradle:8.14-jdk17 AS builder

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle build.gradle ./
COPY common/build.gradle common/build.gradle
COPY app/build.gradle app/build.gradle

RUN chmod +x ./gradlew
RUN --mount=type=secret,id=gh_token,required=false \
    --mount=type=secret,id=github_actor,required=false \
    if [ -f /run/secrets/gh_token ]; then export GH_TOKEN="$(cat /run/secrets/gh_token)"; fi; \
    if [ -f /run/secrets/github_actor ]; then export GITHUB_ACTOR="$(cat /run/secrets/github_actor)"; fi; \
    ./gradlew :app:dependencies --no-daemon >/dev/null

COPY common common
COPY app app

RUN --mount=type=secret,id=gh_token,required=false \
    --mount=type=secret,id=github_actor,required=false \
    if [ -f /run/secrets/gh_token ]; then export GH_TOKEN="$(cat /run/secrets/gh_token)"; fi; \
    if [ -f /run/secrets/github_actor ]; then export GITHUB_ACTOR="$(cat /run/secrets/github_actor)"; fi; \
    ./gradlew :app:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SERVER_PORT=8084 \
    REDIS_PORT=6379 \
    REDIS_TIMEOUT_MS=1000ms \
    PLATFORM_SECURITY_ADMIN_ALLOW_CIDRS=10.0.0.0/8 \
    PLATFORM_SECURITY_IP_GUARD_ENABLED=false \
    PLATFORM_SECURITY_IP_GUARD_TRUST_PROXY=false \
    PLATFORM_SECURITY_RATE_LIMIT_ENABLED=false \
    PLATFORM_SECURITY_RATE_LIMIT_INTERNAL_REQUESTS=1000 \
    PLATFORM_SECURITY_RATE_LIMIT_INTERNAL_WINDOW_SECONDS=60

COPY --from=builder /workspace/app/build/libs/*.jar /app/authz-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/authz-service.jar"]
