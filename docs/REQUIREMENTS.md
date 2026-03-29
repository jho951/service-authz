# Permission Service Requirements (Active)

## 1. 목적
- 본 서비스는 `api-gateway`가 `/admin/**` 또는 `/v1/admin/**` 경로로 라우팅하는 요청에 대해 최종 인가 판정을 제공한다.
- 에디터 서버(Block-server) 관리자 API 보호를 위한 내부용 서비스로 사용한다.

## 2. 인가 API
- `POST /permissions/internal/admin/verify`
- 필수 헤더:
- `X-User-Id`
- `X-Original-Method`
- `X-Original-Path`
- 선택 헤더:
- `X-User-Role`
- `X-Request-Id`
- `X-Correlation-Id`
- `X-Internal-Request-Secret` (설정 시 필수)
- 응답:
- `200 OK`: ALLOW
- `403 FORBIDDEN`: DENY
- `400 BAD_REQUEST`: 헤더 누락/형식 오류

## 3. 정책 모델
- Role: `ADMIN`, `MANAGER`, `MEMBER`
- Permission: `ADMIN_READ`, `ADMIN_WRITE`, `ADMIN_DELETE`, `ADMIN_MANAGE`
- 테이블:
- `roles`
- `permissions`
- `role_permissions`
- `user_roles`
- 기본 정책:
- 관리자 경로가 아니면 DENY
- 명시 허용 외 기본 DENY

## 4. 판정 규칙
- 경로:
- `/admin/**`, `/v1/admin/**`만 관리자 경로로 판단
- 권한 매핑:
- `GET|HEAD|OPTIONS -> ADMIN_READ`
- `POST|PUT|PATCH -> ADMIN_WRITE`
- `DELETE -> ADMIN_DELETE`
- `/admin/manage/**`, `/v1/admin/manage/** -> ADMIN_MANAGE`

## 5. 캐시/로깅
- L1 인메모리 TTL 캐시
- L2 Redis TTL 캐시(장애 시 무시, DB fallback)
- `X-Request-Id`, `X-Correlation-Id` 누락 시 서버가 생성하여 응답 헤더에 반영
- 판정 요청마다 감사 로그 기록:
- `requestId`, `correlationId`, `userId`, `method`, `path`, `decision`, `reason`, `latencyMs`

## 6. 운영 API
- `GET /health`: liveness
- `GET /ready`: DB/Redis 연결 상태

## 7. 참고 연동 대상
- Gateway: `/Users/jhons/Downloads/BE/Api-gateway-server`
- Editor(Block) server: `/Users/jhons/Downloads/BE/Block-server`
