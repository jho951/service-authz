# Authz Service (MVP)

Gateway의 관리자 경로(`/admin/**`, `/v1/admin/**`) 인가와 capability 진실을 판정하는 내부 서비스입니다.

## Contract Source

- 공통 계약 레포: `https://github.com/jho951/contract`
- 이 서비스의 코드 SoT: `Authz-server` `main`
- 인터페이스 변경 시 본 저장소 구현보다 계약 레포 변경을 먼저 반영합니다.
- 책임 분리: `Authz-server`는 capability 진실, `User-server`는 프로필 공개 범위, `Editor`는 최종 집행을 소유합니다.

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

## Notes

- 권한 보유와 권한 공개를 분리하고, 실제 기능 집행은 소비자 서비스가 최종 강제합니다.
- 관리자 RBAC는 v1, 권한 조회/정책/버전/위임은 v2 계약을 따릅니다.

## 포트/문서
- 기본 포트: `8084`
- Swagger: `GET /swagger-ui.html`
- 요구사항: `docs/REQUIREMENTS.md`, `docs/requirement.md`
