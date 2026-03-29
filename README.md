# Permission Service (MVP)

Gateway의 관리자 경로(`/admin/**`, `/v1/admin/**`) 인가를 판정하는 내부 서비스입니다.

## 실행 환경
- Java 17
- Spring Boot 3.3.x
- Gradle
- H2 (JPA)
- Redis(Optional, L2 캐시/ready 체크)

## 실행
```bash
./gradlew bootRun
```

## 테스트
```bash
./gradlew test
```

## 주요 API
- `POST /permissions/internal/admin/verify`
  - 입력 헤더: `X-User-Id`, `X-Original-Method`, `X-Original-Path` (필수), `X-User-Role` (선택)
  - 응답: `200`(ALLOW), `403`(DENY), `400`(입력 오류)
- `GET /health`
- `GET /ready` (DB + Redis 연결 확인)

## 포트/문서
- 기본 포트: `8084`
- Swagger: `GET /swagger-ui.html`
- 요구사항: `docs/REQUIREMENTS.md`, `docs/requirement.md`
