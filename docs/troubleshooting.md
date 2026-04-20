# Troubleshooting

## 기본 확인
1. local 실행: `bash scripts/run.local.sh`
2. docker dev 실행: `bash scripts/run.docker.sh up dev`
3. docker prod 실행: `bash scripts/run.docker.sh up prod`
4. liveness 확인: `curl -i http://localhost:8084/health`
5. readiness 확인: `curl -i http://localhost:8084/ready`

## 인가 API 정상 케이스
local/test에서 `PERMISSION_INTERNAL_AUTH_MODE=LEGACY_SECRET` 또는 `HYBRID`를 사용할 때는 동일한 값을 `X-Internal-Request-Secret`로 포함합니다. 운영 profile은 `Authorization: Bearer <internal-service-jwt>`를 사용합니다.

```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-User-Id: admin-seed' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /v1/admin/blocks' \
  -H 'X-Request-Id: debug-req-1' \
  -H 'X-Correlation-Id: debug-corr-1'
```

기대 결과: `200 OK`

## 관리자 manage 경로 확인
`/admin/manage/**`와 `/v1/admin/manage/**`는 `ADMIN_MANAGE`로 매핑되어야 합니다.

```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-User-Id: admin-seed' \
  -H 'X-Original-Method: POST' \
  -H 'X-Original-Path: /admin/manage/settings'
```

기대 결과: `200 OK`

## 내부 caller proof 불일치
JWT가 유효하지 않거나 legacy secret이 일치하지 않으면 권한 계산 전에 `403`을 반환합니다.

```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: wrong-secret' \
  -H 'X-User-Id: admin-seed' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /v1/admin/blocks'
```

기대 결과: `403 FORBIDDEN`

## 거부 케이스
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-User-Id: member-user' \
  -H 'X-User-Role: ADMIN' \
  -H 'X-Original-Method: DELETE' \
  -H 'X-Original-Path: /admin/blocks/1'
```

기대 결과: `403 FORBIDDEN`

`X-User-Role`은 권한 판단에 사용하지 않으므로 DB의 `user_roles` 기준으로 거부되어야 합니다.

## 입력 오류 케이스
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /admin/blocks'
```

기대 결과: `400 BAD_REQUEST`

## 잘못된 role 헤더 무시
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Internal-Request-Secret: debug-secret' \
  -H 'X-User-Id: admin-seed' \
  -H 'X-User-Role: BAD_ROLE' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /admin/blocks'
```

기대 결과: `200 OK`

## 로그 확인 포인트
앱 로그에서 아래 필드를 포함한 감사 로그 라인을 확인합니다.
- `requestId`
- `correlationId`
- `userId`
- `method`
- `path`
- `decision`
- `reason`
- `latencyMs`

## Redis 장애 점검
1. docker dev/prod 실행에서는 compose 또는 외부 Redis가 `service-shared` 네트워크에서 접근 가능해야 합니다.
2. Redis 중지 상태에서 판정 API를 호출합니다.
3. 기대 결과:
- 판정 API는 DB 기반으로 계속 동작
- `admin-seed`와 같은 허용 케이스는 여전히 `200 OK`
- `/ready`의 `redis` 컴포넌트는 `DOWN`

## Governance 운영 fail-fast 점검
1. GitHub Packages 인증을 확인합니다.
- 로컬은 `GITHUB_ACTOR=jho951`, `GITHUB_TOKEN=<read:packages PAT>`를 export 하거나 Gradle property `githubPackagesUsername`, `githubPackagesToken`을 설정합니다.
2. 운영 profile을 실행합니다.
```bash
bash scripts/run.docker.sh up prod
```
3. 기대 설정:
- `platform.governance.audit.enabled=true`
- `platform.governance.engine.strict=true`
- `platform.governance.violation.action=DENY`
- `platform.governance.violation.handler-failure-fatal=true`
- `platform.governance.policy-config.values.authz.policy.publish.requires-approval=true`
- `platform.governance.operational.require-audit-sink-in-production=false`
4. 자주 실패하는 조건:
- `AuditLogRecorder` bean 없음
- `engine.strict=false`
- `handler-failure-fatal=false`
- enforcing mode에서 policy config 비어 있음

## Platform Security 운영 fail-fast 점검
1. 최신 배포본 해석 확인:
```bash
./gradlew :app:dependencyInsight --dependency platform-security-starter --configuration runtimeClasspath
./gradlew :app:dependencyInsight --dependency platform-governance-starter --configuration runtimeClasspath
```
2. 운영 profile 필수 환경변수 확인:
- `PLATFORM_SECURITY_JWT_SECRET`
- `PLATFORM_SECURITY_INTERNAL_ALLOW_CIDRS`
- `PLATFORM_SECURITY_ADMIN_ALLOW_CIDRS`
- `REDIS_HOST`
- `REDIS_PORT`
3. 기대 설정:
- `platform.security.service-role-preset=INTERNAL_SERVICE`
- `platform.security.boundary.internal-paths=/permissions/internal/**`
- `platform.security.auth.internal-token-enabled=false`
- `platform.security.ip-guard.enabled=true`
- `platform.security.rate-limit.enabled=true`
4. 자주 실패하는 조건:
- GitHub Packages token 없음
- `PLATFORM_SECURITY_JWT_SECRET` 없음
- internal/admin CIDR 누락
- Redis 연결 실패

## Gateway 내부 호출 검증 확인
1. 운영에서는 Authz와 Gateway에 같은 internal JWT secret을 설정합니다.
- Authz: `PERMISSION_INTERNAL_JWT_SECRET=<jwt-secret>`
- Gateway: 같은 secret으로 Authz 호출용 internal service JWT를 서명합니다.
2. Gateway를 통해 관리자 경로를 호출합니다.
3. 기대 결과:
- Gateway가 Authz verify 호출에 `Authorization: Bearer <internal-service-jwt>`를 포함합니다.
- JWT가 유효하면 Authz는 DB 권한 기준으로 `200 OK` 또는 `403 FORBIDDEN`을 반환합니다.
- JWT가 유효하지 않으면 Authz는 권한 계산 전에 `403 FORBIDDEN`을 반환합니다.
