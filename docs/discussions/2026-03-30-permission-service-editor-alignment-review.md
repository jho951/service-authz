# Permission Service 전략 검토 (Editor 연동)

## 배경
- 기존 코드베이스는 수강신청 예제 구조였고, 현재 목표는 에디터 서버 관리자 경로 인가 서버다.
- Gateway는 `POST /permissions/internal/admin/verify`로 헤더 기반 판정을 위임한다.
- Block-server ADR 015/016 기준으로 `X-User-Id` 신뢰 헤더 정책과 내부 경로(`/admin/**`) 규약을 함께 고려해야 한다.

## 선택지
1. 기존 수강신청 구조 유지 + 일부 인가 API만 추가
- 장점: 구현 속도 빠름
- 단점: 도메인 오염이 커서 운영 시 혼선

2. 인가 핵심만 신규 도메인으로 추가하고 기존 코드는 점진 정리
- 장점: 현재 배포 가능한 최소 변경으로 gateway/editor 연동 가능
- 단점: 과거 예제 코드가 일부 잔존

## 시나리오 비교
- 시나리오 A: `X-User-Id=admin-seed`, `GET /admin/blocks`
- 선택지 1/2 모두 200 가능
- 시나리오 B: `X-User-Role=MEMBER`, `DELETE /admin/blocks/1`
- 선택지 2는 RBAC 매핑으로 안정적으로 403 처리 가능

## 결론
- 선택지 2 채택.
- MVP 범위에서 RBAC + 캐시 + 감사로그 + health/ready까지 포함해 gateway/editor 연동 기준 충족.
- 단기 호환을 위해 `/v1/admin/**`와 `/admin/**`를 모두 허용한다.
