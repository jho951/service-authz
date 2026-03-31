# Permission Server Requirements (MVP)

## 1. 목적
- 인증(AuthN)과 인가(AuthZ)를 분리하기 위해 `authz-service`를 독립 서비스로 구축한다.
- `api-gateway`는 요청 라우팅/공통 정책을 담당하고, 관리자/민감 경로 인가 판정은 `authz-service`가 담당한다.

## 2. 범위
- 포함:
- 관리자 경로(`admin`) 접근 허용/거부 판정 API
- 역할 기반 권한(RBAC) 최소 모델
- 게이트웨이 내부 연동용 인터페이스
- 캐시/감사로그/헬스체크
- 제외:
- 사용자 인증(로그인, 토큰 발급/재발급)
- 사용자 프로필 도메인 관리

## 3. 용어
- Subject: 권한 판정을 받는 주체(사용자)
- Resource: 접근 대상(예: workspace, document, admin endpoint)
- Action: 수행 행위(예: read, write, delete, manage)
- Decision: `ALLOW` 또는 `DENY`

## 4. 아키텍처 원칙
- `authz-service`는 인가 기준 시스템(Source of Truth)이다.
- 게이트웨이는 판정 결과를 단기 캐시할 수 있으나, 최종 기준은 `authz-service`다.
- 내부 통신은 사설 네트워크에서만 허용하고, 내부 시크릿 또는 서비스 인증으로 보호한다.

## 5. 기능 요구사항
## 5.1 권한 판정 API
- 게이트웨이로부터 관리자 경로 접근 권한 판정을 요청받는다.
- 입력 컨텍스트:
- `X-User-Id` (필수)
- `X-User-Role` (선택)
- `X-Original-Method` (필수)
- `X-Original-Path` (필수)
- `X-Request-Id`, `X-Correlation-Id` (추적용)
- 출력:
- 허용 시 `200 OK`
- 거부 시 `403 FORBIDDEN`
- 입력 불충분/유효성 오류 시 `400 BAD_REQUEST`

## 5.2 정책 모델 (MVP)
- RBAC 최소 구현:
- Role: `ADMIN`, `MANAGER`, `MEMBER`
- Permission: `ADMIN_READ`, `ADMIN_WRITE`, `ADMIN_DELETE`, `ADMIN_MANAGE`
- Role-Permission 매핑 테이블 지원
- User-Role 매핑 테이블 지원

## 5.3 관리자 경로 규칙
- `/v1/admin/**` 경로는 반드시 `authz-service` 판정 후 통과한다.
- 기본 정책은 `DENY` 우선(명시 허용만 통과).

## 5.4 감사 로그
- 모든 판정 요청을 구조화 로그로 기록:
- `requestId`, `correlationId`, `userId`, `method`, `path`, `decision`, `reason`, `latencyMs`

## 5.5 헬스/운영 API
- `GET /health` (liveness)
- `GET /ready` (readiness, DB/Redis 연결 포함)

## 6. 비기능 요구사항
- 가용성: 권한 판정 API SLA 99.9% (운영 목표)
- 성능: P95 응답 50ms 이하(캐시 히트), 150ms 이하(캐시 미스)
- 확장성: 수평 확장 가능(무상태 애플리케이션)
- 보안: 내부망 통신 + 서비스 인증(시크릿 헤더 또는 mTLS)
- 관측성: 로그, 메트릭, 트레이싱(requestId/correlationId)

## 7. 데이터 모델 (초안)
- `roles(id, name, description, created_at, updated_at)`
- `permissions(id, code, description, created_at, updated_at)`
- `role_permissions(role_id, permission_id, created_at)`
- `user_roles(user_id, role_id, scope_type, scope_id, created_at)`
- 인덱스:
- `user_roles(user_id, scope_type, scope_id)`
- `role_permissions(role_id)`

## 8. 캐시 요구사항
- L1: 인메모리 캐시(짧은 TTL, 예: 3~10초)
- L2: Redis 캐시(예: 30~120초)
- 키 예시: `perm:{userId}:{method}:{path}`
- 권한 변경 이벤트 수신 시 관련 키 무효화
- Redis 장애 시:
- 판정 API는 DB fallback
- 게이트웨이 측은 fail-closed 정책 유지(권장)

## 9. 보안 요구사항
- 내부 호출 전용 엔드포인트는 외부 노출 금지
- 게이트웨이에서 trusted header 재주입 후 전달
- authz-service는 trusted header만 신뢰
- 입력 값 검증(빈값, path traversal, 비정상 method)

## 10. 게이트웨이 연동 요구사항
- gateway `RouteType.ADMIN` 활성화
- gateway에서 `PermissionServiceClient.verifyAdminAccess(...)` 호출 연결
- 환경변수:
- `PERMISSION_SERVICE_URL`
- `PERMISSION_ADMIN_VERIFY_URL` (또는 내부 표준 path 조합)
- `GATEWAY_PERMISSION_CACHE_ENABLED`
- `GATEWAY_PERMISSION_CACHE_TTL_SECONDS`
- `GATEWAY_PERMISSION_CACHE_PREFIX`

## 11. 실패 처리 정책
- 권한 서버 타임아웃: 게이트웨이는 `403` 또는 `504` 중 운영 정책으로 고정 (권장: `403` fail-closed)
- 권한 서버 5xx: 기본 거부(`403`) + 알람 발행
- 입력 오류(헤더 누락): `400`

## 12. 배포/운영 요구사항
- 컨테이너 기반 배포(Docker/Kubernetes)
- secrets는 환경변수 또는 시크릿 매니저 사용
- 롤링 배포 가능해야 하며 무중단 목표

## 13. 테스트 요구사항
- 단위 테스트: 정책 엔진, 매핑, 예외 케이스
- 통합 테스트: gateway -> authz-service 판정 체인
- 부하 테스트: 캐시 hit/miss 시나리오
- 보안 테스트: 헤더 위변조, 우회 경로 접근

## 14. 단계별 도입 계획
1. MVP: `/permissions/internal/admin/verify` + RBAC + 로그
2. Gateway 연동: `/v1/admin/**`를 `RouteType.ADMIN`으로 전환
3. 캐시/무효화: Redis + 이벤트 기반 invalidate
4. 고도화: ABAC/리소스 스코프, mTLS, 정책 관리 UI

## 15. 완료 기준(Definition of Done)
- 게이트웨이 관리자 경로가 authz-service 판정 없이는 통과하지 않는다.
- 권한 변경 후 지정 시간 내(또는 이벤트 즉시) 판정 결과가 반영된다.
- 운영 대시보드에서 판정 성공률/지연/오류율을 확인할 수 있다.

## 참고 레포지토리 
https://github.com/jho951/Api-gateway-server.git = 게이트웨이
https://github.com/jho951/Redis-server.git = redis
https://github.com/jho951/User-server.git = 유저 서비스
https://github.com/jho951/Auth-server.git = 인증 서버
https://github.com/jho951/Editor-server.git = 에디터 서비스


# Permission Server Requirements (MVP)

## 1. 목적
- 인증(AuthN)과 인가(AuthZ)를 분리하기 위해 `authz-service`를 독립 서비스로 구축한다.
- `api-gateway`는 요청 라우팅/공통 정책을 담당하고, 관리자/민감 경로 인가 판정은 `authz-service`가 담당한다.
- 권한 서버 구현 저장소는 `https://github.com/jho951/Authz-server.git`를 기준으로 한다.

## 2. 핵심 원칙
- Gateway에서 `role`로 직접 허용/거부를 판단하지 않는다.
- 권한 판정은 `authz-service`가 수행한다.
- `role`은 `authz-service` 내부 모델(RBAC)로만 사용 가능하다.

## 3. 범위
- 포함:
- 관리자 경로(`admin`) 접근 허용/거부 판정 API
- 역할 기반 권한(RBAC) 최소 모델
- 게이트웨이 내부 연동용 인터페이스
- 캐시/감사로그/헬스체크
- 제외:
- 로그인/토큰 발급/재발급
- 사용자 프로필 기준 데이터 관리

## 4. 권한 판정 API (MVP)
- Endpoint: `POST /permissions/internal/admin/verify`
- 입력 헤더:
- `X-User-Id` (필수)
- `X-User-Role` (선택, 참고값)
- `X-Original-Method` (필수)
- `X-Original-Path` (필수)
- `X-Request-Id`, `X-Correlation-Id` (추적용)
- 출력:
- `200 OK`: 허용
- `403 FORBIDDEN`: 거부
- `400 BAD_REQUEST`: 입력 오류

## 5. 정책 모델
- RBAC 최소 구현:
- Role: `ADMIN`, `MANAGER`, `MEMBER`
- Permission:
- Admin 계열: `ADMIN_READ`, `ADMIN_WRITE`, `ADMIN_DELETE`, `ADMIN_MANAGE`
- Member 계열: `DOC_READ`, `DOC_CREATE`, `DOC_UPDATE`, `DOC_DELETE`, `WORKSPACE_READ`, `WORKSPACE_INVITE`, `WORKSPACE_MANAGE`
- Role-Permission 매핑
- User-Role 매핑
- 기본 정책: `DENY` 우선(명시 허용만 통과)

## 5.1 MEMBER 권한 요구사항
- `MEMBER`도 서비스 사용에 필요한 권한을 명시적으로 가진다.
- 관리자/멤버를 role만으로 단순 분기하지 않고, 최종 판정은 `permission` 기준으로 수행한다.
- 최소 판정 축:
- Subject: `userId`
- Action: `read/create/update/delete/invite/manage`
- Resource: `document/workspace/admin`
- Scope: `workspaceId`, `documentId` (가능한 경우)
- 예시 정책:
- 문서 조회 API: `DOC_READ` 필요
- 문서 생성 API: `DOC_CREATE` 필요
- 워크스페이스 초대 API: `WORKSPACE_INVITE` 필요
- 관리자 API: `ADMIN_*` 필요
- 역할은 편의 그룹이며, 실제 허용은 permission 매핑 결과로 결정한다.

## 6. 게이트웨이 연동 요구사항
- gateway는 `RouteType.ADMIN` 경로에 대해 authz-service를 호출한다.
- gateway는 authz-service 판정 결과(`200/403`)만 집행한다.
- gateway 내부에 role 판정 로직을 두지 않는다.
- 연동 환경변수:
- `PERMISSION_SERVICE_URL`
- `PERMISSION_ADMIN_VERIFY_URL`
- `GATEWAY_PERMISSION_CACHE_ENABLED`
- `GATEWAY_PERMISSION_CACHE_TTL_SECONDS`
- `GATEWAY_PERMISSION_CACHE_PREFIX`

## 6.1 Permission Matrix (MVP)
| API Path Pattern | Method | Required Permission | Scope Key | 비고 |
|---|---|---|---|---|
| `/v1/documents/**` | `GET` | `DOC_READ` | `documentId` or `workspaceId` | 문서 조회 |
| `/v1/documents/**` | `POST` | `DOC_CREATE` | `workspaceId` | 문서 생성 |
| `/v1/documents/**` | `PUT`,`PATCH` | `DOC_UPDATE` | `documentId` | 문서 수정 |
| `/v1/documents/**` | `DELETE` | `DOC_DELETE` | `documentId` | 문서 삭제 |
| `/v1/workspaces/**` | `GET` | `WORKSPACE_READ` | `workspaceId` | 워크스페이스 조회 |
| `/v1/workspaces/**` | `POST` | `WORKSPACE_MANAGE` | `workspaceId` | 워크스페이스 생성/설정 |
| `/v1/workspaces/**/members/**` | `POST` | `WORKSPACE_INVITE` | `workspaceId` | 멤버 초대 |
| `/v1/workspaces/**/members/**` | `DELETE` | `WORKSPACE_MANAGE` | `workspaceId` | 멤버 제거/권한 변경 |
| `/v1/admin/**` | `GET` | `ADMIN_READ` | `global` | 관리자 조회 |
| `/v1/admin/**` | `POST`,`PUT`,`PATCH` | `ADMIN_WRITE` | `global` | 관리자 변경 |
| `/v1/admin/**` | `DELETE` | `ADMIN_DELETE` | `global` | 관리자 삭제 |
| `/v1/admin/**` | `*` | `ADMIN_MANAGE` | `global` | 상위 관리자 정책(선택) |

매핑 원칙:
- 경로+메서드 기준으로 1차 permission 매핑 후, scope 컨텍스트로 2차 검증한다.
- 동일 경로라도 메서드가 다르면 다른 permission을 요구한다.
- 정책 충돌 시 `DENY` 우선 규칙을 적용한다.

## 7. 캐시/운영
- L1 인메모리 + L2 Redis 캐시 사용 가능
- 키 예시: `perm:{userId}:{method}:{path}`
- 권한 변경 시 캐시 무효화
- 장애 시 기본 거부(fail-closed) 권장

## 8. 완료 기준 (DoD)
- 관리자 경로가 authz-service 판정 없이는 통과하지 않는다.
- gateway는 role 판단을 하지 않고 판정 결과만 집행한다.
- MEMBER 경로도 permission 기반 판정이 가능하다.
- 판정 로그(requestId, userId, method, path, decision, reason, latency)가 남는다.
