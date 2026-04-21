# Auth API

## Admin Verify
- Method: `POST`
- Path: `/permissions/internal/admin/verify`
- 목적: Gateway가 관리자 경로 요청을 처리하기 전 Authz-server에 최종 권한 판정을 위임합니다.

## 요청 헤더
필수:
- `X-User-Id`
- `X-Original-Method`
- `X-Original-Path`

선택:
- `X-Request-Id`
- `X-Correlation-Id`
- `X-Internal-Request-Secret`
- `Authorization: Bearer <internal-service-jwt>`

내부 호출자 검증은 `permission.internal-auth.mode`로 결정합니다. 운영 profile은 `JWT` 모드를 사용하며, Gateway는 `AUTHZ_INTERNAL_JWT_SECRET`로 서명한 internal service JWT를 `Authorization` 헤더에 전달해야 합니다. local/test에서는 `LEGACY_SECRET` 또는 `HYBRID` 모드로 `X-Internal-Request-Secret`을 사용할 수 있습니다.

## 판정 규칙
- `permission.route-policy.routes` 설정으로 method/path를 `PermissionCode`에 매핑합니다.
- 기본 설정은 `/admin/**`, `/v1/admin/**`를 관리자 경로로 다루며 `/admin/manage/**`, `/v1/admin/manage/**`는 `ADMIN_MANAGE`로 우선 매핑합니다.
- 최종 허용 여부는 Authz DB의 `user_roles`와 `role_permissions` 매핑으로 판단합니다.
- `X-User-Role`은 권한 상승 근거로 사용하지 않습니다.
- 잘못된 `X-User-Role` 값이 전달되어도 입력 오류로 처리하지 않습니다.

## 응답
- `200 OK`: ALLOW
- `403 FORBIDDEN`: DENY 또는 내부 caller proof 검증 실패
- `400 BAD_REQUEST`: 필수 헤더 누락, method/path 형식 오류

오류 응답은 공통 `ErrorCode` 기반입니다.

```json
{
  "code": 9001,
  "message": "잘못된 요청입니다."
}
```

현재 공통 오류 코드는 `common/src/main/java/com/example/permission/common/base/constant/ErrorCode.java`에서 관리합니다.

## 운영 API
- `GET /health`: liveness
- `GET /ready`: DB/Redis 연결 상태
- `GET /swagger-ui.html`: Swagger UI
- `GET /v3/api-docs`: OpenAPI document

Redis 장애 시 `/ready`는 `DOWN`을 노출할 수 있지만, 판정 API는 DB fallback으로 계속 동작해야 합니다.

## 감사 로그
판정 요청마다 아래 정보를 기록합니다.
- `requestId`
- `correlationId`
- `userId`
- `method`
- `path`
- `decision`
- `reason`
- `latencyMs`

`X-Request-Id`, `X-Correlation-Id`가 누락되면 서버가 생성해 응답 헤더에 반영합니다.

## curl 예시
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-User-Id: admin-seed' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /v1/admin/blocks' \
  -H 'X-Request-Id: debug-req-1' \
  -H 'X-Correlation-Id: debug-corr-1'
```
