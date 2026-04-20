# Database

## 저장 모델
Authz-server의 v1 권한 모델은 RBAC입니다.

Role:
- `ADMIN`
- `MANAGER`
- `MEMBER`

Permission:
- `ADMIN_READ`
- `ADMIN_WRITE`
- `ADMIN_DELETE`
- `ADMIN_MANAGE`

관리 테이블:
- `roles`
- `permissions`
- `role_permissions`
- `user_roles`

## ID 정책
- 모든 JPA entity의 공통 `id`는 Java `UUID`입니다.
- DB column은 `char(36)` 기준으로 관리합니다.
- UUID 값은 `BaseEntity`의 `@GeneratedValue(strategy = GenerationType.UUID)`로 생성합니다.
- 운영 seed SQL은 PostgreSQL `gen_random_uuid()::text`로 id를 명시 삽입합니다.

## Entity 기준
- 모든 JPA entity는 `BaseEntity`를 상속합니다.
- 공통 entity 기반 타입은 `common` 모듈에 둡니다.
- 공통 감사 컬럼은 `version`, `created_at`, `modified_at`입니다.
- `created_at`, `modified_at`은 Spring Data JPA Auditing으로 관리합니다.

## 권한 계산
- 권한 계산은 `X-User-Id`로 조회한 `user_roles`를 기준으로 합니다.
- `X-User-Role` 헤더는 사용하지 않습니다.
- role이 가진 permission은 `role_permissions`를 통해 계산합니다.
- 관리자 경로와 method는 `ADMIN_*` permission code로 매핑됩니다.
- 관리자 경로가 아니면 DENY입니다.
- 명시 허용 외 기본 DENY입니다.

## Seed
`permission-seed.sql`은 PostgreSQL 기준 idempotent seed 스크립트입니다.

포함 항목:
- 기본 role 3종
- 관리자 permission 4종
- 문서/워크스페이스 permission 확장 후보
- `ADMIN`, `MANAGER`, `MEMBER` role-permission 매핑
- sample user-role assignment 주석 예시

## Cache
- L1: in-memory TTL cache
- L2: Redis TTL cache
- Redis 장애 시 DB fallback

주요 설정:
- `PERMISSION_CACHE_PREFIX`
- `PERMISSION_CACHE_L1_TTL_SECONDS`
- `PERMISSION_CACHE_L2_TTL_SECONDS`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_TIMEOUT_MS`

## Readiness
`GET /ready`는 DB와 Redis 연결 상태를 함께 확인합니다.

Redis 장애 시:
- `/ready`는 `DOWN`을 노출할 수 있습니다.
- 판정 API는 DB fallback으로 계속 동작해야 합니다.
