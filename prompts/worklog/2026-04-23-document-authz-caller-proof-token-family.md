# 2026-04-23

- 목적: authz-service의 별도 caller proof JWT 계약과 현재 관리자 경로 403 troubleshooting을 문서화한다.
- 배경: 일반 보호 서비스용 Gateway 내부 JWT와 authz-service 내부 caller proof JWT가 같은 것으로 오해될 수 있었다.
- 핵심 변경:
  - service-contract의 authz security/api에 토큰 패밀리 구분 추가
  - `docs/troubleshooting.md`에 관리자 경로 403 원인과 해결 절차 추가
  - `aud=authz-service` 계약을 운영 확인 포인트로 명시
