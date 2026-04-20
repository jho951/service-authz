# 2026-03-30 Editor Permission MVP

- 작업 목표: 수강신청 예제 서버를 Gateway/Editor 연동용 authz-service MVP로 전환
- Step 1: `docs/requirement.md` 기반으로 admin verify API, RBAC 엔티티/리포지토리, L1/L2 캐시, 감사 로그 구현
- Step 2: 참고 레포(`Api-gateway-server`, `Block-server`) 문서 확인 후 `/v1/admin/**` + `/admin/**` 경로 호환, trace header 생성/반영, 내부 시크릿 옵션 추가
- Step 3: `GET /health`, `GET /ready` 운영 API 및 Redis readiness 반영
- Step 4: `./gradlew test`로 테스트 통과 확인
- Step 5: platform-governance 2.0.0 GitHub Packages repository/BOM/starter 적용, prod governance fail-fast 설정, prod AuditLogRecorder bean, wildcard permission grant 정책 plugin 등록
- Step 6: 단일 애플리케이션만 있는 현재 단계에서는 멀티모듈 전환을 철회하고 루트 `src/` 단일 모듈 구조 유지
- Step 7: 권한 판단에서 `X-User-Role` 제거, Authz DB `user_roles` 기준 판정으로 정리, 잘못된 role 헤더 무시 테스트 반영
- Step 8: Gateway Authz 호출에 `GATEWAY_INTERNAL_REQUEST_SECRET` 기반 `X-Internal-Request-Secret` 전달을 추가해 Authz 내부 시크릿 운영 설정과 정합성 확보
- Step 9: Authz 실행 경로를 `local`, `docker dev`, `docker prod`로 분리하고 각 경로에 내부 시크릿 env 적용
- Step 10: platform repo `gradle.properties` 기준 최신 배포본 확인 후 `platform-governance` 2.0.1, `platform-security` 2.0.3 소비로 갱신; Authz 내부 검증 API를 platform-security internal boundary/pass-through 구조로 연결하고 Redis 기반 RateLimiter bean, prod security 설정, ADR 004, runbook을 반영
- Step 11: `.env.local`, `.env.dev`, `.env.prod`에 `PLATFORM_SECURITY_JWT_SECRET`, `PLATFORM_SECURITY_INTERNAL_ALLOW_CIDRS`, platform-security rate-limit 설정을 채우고 docker dev/prod compose에 `central-redis` 서비스를 추가해 Authz 컨테이너의 Redis host 연결을 보장
- Step 12: Auth-server와 구조를 맞추기 위해 `common`, `app` 멀티모듈로 전환; 기존 실행 코드는 `app`으로 이동하고 `common`에는 `BaseEntity`, `AuditableEntity`, `BaseResponse`, `ErrorResponse` 기반 타입을 추가, Docker build를 `:app:bootJar` 기준으로 수정
- Step 13: `ErrorCode`, `BaseException`, `BadRequestException`, `CommonExceptionHandler`를 `common`으로 이동하고 app의 permission 전용 bad request exception/handler를 공통 예외 기반으로 정리
- Step 14: 루트 `AGENTS.md`의 긴 협업/운영 가이드를 삭제하고 `docs/architecture.md`, `docs/auth-api.md`, `docs/ci-and-implementation.md`, `docs/database.md`, `docs/docker.md`, `docs/platform.md`, `docs/README.md`, `docs/troubleshooting.md`로 역할별 분리
- Step 15: `docs/REQUIREMENTS.md`의 활성 요구사항을 주제별 문서로 흡수하고 requirements 전용 문서를 삭제
- Step 16: 공통 `BaseEntity` id를 `Long IDENTITY`에서 UUID 문자열 `varchar(36)` 정책으로 전환하고 JPA repository 및 PostgreSQL seed SQL을 같은 기준으로 정리
