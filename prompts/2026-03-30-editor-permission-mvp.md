# 2026-03-30 Editor Permission MVP

- 작업 목표: 수강신청 예제 서버를 Gateway/Editor 연동용 permission-service MVP로 전환
- Step 1: `docs/requirement.md` 기반으로 admin verify API, RBAC 엔티티/리포지토리, L1/L2 캐시, 감사 로그 구현
- Step 2: 참고 레포(`Api-gateway-server`, `Block-server`) 문서 확인 후 `/v1/admin/**` + `/admin/**` 경로 호환, trace header 생성/반영, 내부 시크릿 옵션 추가
- Step 3: `GET /health`, `GET /ready` 운영 API 및 Redis readiness 반영
- Step 4: `./gradlew test`로 테스트 통과 확인
