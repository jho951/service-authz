# Authz-server Docs

이 디렉터리는 Authz-server의 현재 운영 계약, 구현 구조, 배포/디버깅 절차를 역할별로 나눈 문서 세트입니다.

## 문서 지도
- [architecture.md](./architecture.md): 서비스 책임, 모듈 구조, 코드 경계, 채택 결정 요약
- [api.md](./api.md): 인가 API, 헤더 계약, 응답 규칙, 감사 로그 계약
- [openapi/authz-service.upstream.v1.yaml](./openapi/authz-service.upstream.v1.yaml): authz-service upstream OpenAPI 문서
- [ci-and-implementation.md](./ci-and-implementation.md): 개발/검증 명령, CI, PR 규칙, 에이전트 작업 규칙
- [database.md](./database.md): RBAC 테이블, seed 데이터, 캐시 모델
- [docker.md](./docker.md): Dockerfile, dev/prod compose, 실행 스크립트, 환경변수
- [platform.md](./platform.md): platform-security, platform-governance 소비 방식과 운영 정책
- [troubleshooting.md](./troubleshooting.md): 로컬/도커 디버깅, curl 재현, 장애 점검

## 정책 문서
- 현재 유효한 제품 요구사항은 주제별 문서에 둡니다. 서비스 경계는 [architecture.md](./architecture.md), API 계약은 [auth-api.md](./auth-api.md), 저장 모델은 [database.md](./database.md), 플랫폼 운영 정책은 [platform.md](./platform.md)를 기준으로 봅니다.
- [../prompts/](../prompts/): AI 작업 로그

## 운영 기준
- 요구사항과 정책이 바뀌면 영향받는 주제별 문서를 함께 갱신합니다.
- 되돌리기 어렵거나 팀 합의가 필요한 기술 결정은 주제별 문서의 채택 결정 요약에 반영합니다.
- 전략 검토, 비교, 회의 메모가 필요하면 별도 discussion 문서를 새로 만들고 주제별 문서에서 연결합니다.
- 재현 가능한 장애 대응 절차는 [troubleshooting.md](./troubleshooting.md)에 반영합니다.
- 모든 AI 작업은 [../prompts/](../prompts/)에 작업 목표 단위로 로그를 남깁니다.
- [contract-change-workflow.md](./contract-change-workflow.md): 구현 변경 후 service-contract와 contract.lock.yml 갱신 순서
