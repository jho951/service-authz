# v1 Permission 후속 작업 (Editor Admin)

## 1. 정책 고도화
- `scope_type/scope_id` 기반 리소스 스코프 인가 적용
- 문서/워크스페이스 단위 ABAC 규칙 확장

## 2. 운영 안정화
- Redis key invalidate 이벤트 연동
- 캐시 히트율/판정 지연 메트릭 대시보드 추가

## 3. 연동 정리
- gateway permission check 활성화 경로 점검
- gateway가 `X-Internal-Request-Secret` 전달하도록 클라이언트 연동

## 4. 테스트 보강
- gateway -> permission 통합 테스트
- 캐시 hit/miss 부하 테스트
- 헤더 위변조/우회 보안 테스트

## 5. 코드 정리
- 수강신청 예제 도메인 제거 또는 별도 브랜치 분리
