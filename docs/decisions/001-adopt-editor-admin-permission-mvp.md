# ADR 001: Editor 관리자 경로용 Authz Service MVP 채택

## 상태
- 채택됨

## 배경
- Gateway는 `/v1/admin/**` 요청에 대해 별도 인가 판정을 위임할 내부 서비스가 필요하다.
- Block-server 문서(ADR 015/016)는 Gateway 주입 신뢰 헤더 사용과 감사 추적을 강제한다.

## 결정
- 내부 판정 API를 `POST /permissions/internal/admin/verify`로 고정한다.
- 입력 헤더 계약은 `X-User-Id`, `X-Original-Method`, `X-Original-Path`, `X-User-Role`로 고정한다.
- RBAC MVP(`ADMIN`, `MANAGER`, `MEMBER` + `ADMIN_*`)를 DB 테이블(`roles`, `permissions`, `role_permissions`, `user_roles`)로 관리한다.
- 캐시는 L1(in-memory) + L2(Redis, optional fallback)를 사용한다.
- 감사 로그는 모든 판정 요청(성공/거부/입력오류)에 대해 `requestId`, `correlationId`, `userId`, `method`, `path`, `decision`, `reason`, `latencyMs`를 기록한다.
- 내부 시크릿(`X-Internal-Request-Secret`) 검증은 설정 시 활성화한다.

## 영향
- 장점:
- Gateway 및 Editor 서버와의 연동 기준이 명확해진다.
- 권한 모델 확장(ABAC/스코프) 전 최소 운영 가능한 기반을 확보한다.
- 단점:
- 기존 수강신청 예제 코드가 일부 남아 있어 후속 정리가 필요하다.
