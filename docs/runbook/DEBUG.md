# Permission Service 디버깅 절차

## 1. 기본 확인
1. 애플리케이션 실행: `./gradlew bootRun`
2. liveness 확인: `curl -i http://localhost:8084/health`
3. readiness 확인: `curl -i http://localhost:8084/ready`

## 2. 인가 API 정상 케이스
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-User-Id: admin-seed' \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /v1/admin/blocks' \
  -H 'X-Request-Id: debug-req-1' \
  -H 'X-Correlation-Id: debug-corr-1'
```
- 기대 결과: `200 OK`

## 3. 거부 케이스
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-User-Id: member-user' \
  -H 'X-User-Role: MEMBER' \
  -H 'X-Original-Method: DELETE' \
  -H 'X-Original-Path: /v1/admin/blocks/1'
```
- 기대 결과: `403 FORBIDDEN`

## 4. 입력 오류 케이스
```bash
curl -i -X POST http://localhost:8084/permissions/internal/admin/verify \
  -H 'X-Original-Method: GET' \
  -H 'X-Original-Path: /v1/admin/blocks'
```
- 기대 결과: `400 BAD_REQUEST`

## 5. 로그 확인 포인트
- 앱 로그에서 아래 필드를 포함한 감사 로그 라인을 확인:
- `requestId`, `correlationId`, `userId`, `method`, `path`, `decision`, `reason`, `latencyMs`

## 6. Redis 장애 점검
1. Redis 중지 상태에서 판정 API 호출
2. 기대 결과:
- 판정 API는 DB 기반으로 계속 동작
- `/ready`의 `redis` 컴포넌트는 `DOWN`
