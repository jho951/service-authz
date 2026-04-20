# Docker

## Image Build
Dockerfile은 멀티스테이지 빌드를 사용합니다.

1. `gradle:8.14-jdk17` builder에서 `./gradlew :app:bootJar -x test --no-daemon` 실행
2. `eclipse-temurin:17-jre` runtime image에 `/workspace/app/build/libs/*.jar` 복사
3. `/app/authz-service.jar` 실행

GitHub Packages dependency 해석을 위해 build arg를 전달합니다.
- `GITHUB_ACTOR` 기본값: `jho951`
- `GITHUB_TOKEN`

이미지 runtime 기본값은 Dockerfile에 둡니다.
- `SERVER_PORT=8084`
- `REDIS_PORT=6379`
- `REDIS_TIMEOUT_MS=1000ms`
- `PLATFORM_SECURITY_ADMIN_ALLOW_CIDRS=10.0.0.0/8`
- `PLATFORM_SECURITY_IP_GUARD_ENABLED=false`
- `PLATFORM_SECURITY_IP_GUARD_TRUST_PROXY=false`
- `PLATFORM_SECURITY_RATE_LIMIT_ENABLED=false`
- `PLATFORM_SECURITY_RATE_LIMIT_INTERNAL_REQUESTS=1000`
- `PLATFORM_SECURITY_RATE_LIMIT_INTERNAL_WINDOW_SECONDS=60`

compose는 profile, secret, host/network처럼 환경별로 달라지는 값만 선언합니다.

## 실행 스크립트
```bash
bash scripts/run.docker.sh up dev
bash scripts/run.docker.sh down dev
bash scripts/run.docker.sh build dev
bash scripts/run.docker.sh logs dev
bash scripts/run.docker.sh ps dev
bash scripts/run.docker.sh restart dev
```

`prod`도 같은 action을 사용합니다.

```bash
bash scripts/run.docker.sh up prod
```

스크립트는 `service-backbone-shared` 계열 external network가 없으면 생성합니다.

## dev compose
`docker/dev/compose.yml`은 아래 서비스를 포함합니다.
- `central-redis`
- `permission-service`

dev Redis:
- image: `redis:7-alpine`
- host port: `${REDIS_EXPOSE_PORT:-6379}:6379`
- command: `redis-server --appendonly yes`
- healthcheck: `redis-cli ping`

dev Authz:
- host port: `8084:8084`
- profile: `dev`
- default Redis host: `central-redis`
- platform-security IP guard/rate limit은 기본 비활성화

## prod compose
`docker/prod/compose.yml`은 `permission-service`를 운영 profile로 실행합니다.

필수 환경변수:
- `GITHUB_TOKEN`
- `AUTHZ_SERVICE_HOST_BIND`
- `PERMISSION_INTERNAL_JWT_SECRET`
- `REDIS_HOST`
- `PLATFORM_SECURITY_JWT_SECRET`
- `PLATFORM_SECURITY_INTERNAL_ALLOW_CIDRS`

운영 compose는 Redis를 직접 띄우지 않고 외부 Redis endpoint를 `REDIS_HOST`로 받습니다.

## 환경 파일
`.env.example`을 기준으로 아래 파일을 만듭니다.
- `.env.local`
- `.env.dev`
- `.env.prod`

주요 값:
- `SERVER_PORT=8084`
- `PERMISSION_INTERNAL_AUTH_MODE`
- `PERMISSION_INTERNAL_JWT_SECRET`
- `REDIS_HOST`
- `REDIS_PORT`
- `PLATFORM_SECURITY_JWT_SECRET`
- `PLATFORM_SECURITY_INTERNAL_ALLOW_CIDRS`
- `PLATFORM_SECURITY_ADMIN_ALLOW_CIDRS`
- `PLATFORM_SECURITY_RATE_LIMIT_ENABLED`
- `GITHUB_ACTOR`
- `GITHUB_TOKEN`
