# 08 - RegistrationController에 REST 엔드포인트를 연결
- Date: 2026-02-08
- Tool: Codex
- Goal: RegistrationController

## Prompt (원문)
```text
목표: RegistrationService가 실제로 호출되도록 RegistrationController에 REST 엔드포인트를 연결한다.
Stable Spec(docs/API.md)와 일치하는 요청/응답 형식 및 HTTP 상태코드를 보장한다.

# 작업 범위(반드시 지킬 것)
- 수정 파일은 아래 중 "필요한 최소"로만 한다.
    1) src/main/java/**/registration/RegistrationController.java
    2) (필요 시) src/main/java/**/registration/dto/*.java 또는 request/response DTO 파일
- RegistrationService의 로직은 절대 수정하지 마라.
- Repository/Entity 구조는 그대로 유지한다.
- 전역 예외 처리(@ControllerAdvice)가 이미 존재한다고 가정하고,
  Controller에서는 비즈니스 예외를 catch/변환하지 말고 그대로 던져라.
- 성공 응답 스키마는 docs/API.md의 “data” 필드를 반드시 사용한다.
- 실패 응답 스키마는 전역 예외 처리에서 “error”로 처리된다고 가정한다.

# API 스펙(Stable Spec)
## 1) POST /registrations (수강 신청)
- Request JSON:
  {
  "studentId": 1001,
  "courseId": 120
  }
- Response:
    - 성공: HTTP 201
    - Body:
      {
      "data": {
      "registrationId": 9001,
      "studentId": 1001,
      "courseId": 120,
      "status": "ENROLLED"
      }
      }

## 2) DELETE /registrations/{registrationId} (수강 취소)
- Response:
    - 성공: HTTP 200
    - Body:
      {
      "data": {
      "registrationId": 9001,
      "status": "CANCELED"
      }
      }

# 구현 지침
1) Controller에서 RegistrationService를 DI 받아라.
2) POST /registrations:
    - request body를 DTO로 받고 (studentId, courseId)
    - registrationService.register(studentId, courseId)를 호출
    - 결과를 Response DTO로 매핑
    - ResponseEntity.status(201)로 반환
3) DELETE /registrations/{registrationId}:
    - PathVariable로 id를 받고
    - registrationService.cancel(registrationId)를 호출
    - 결과를 Response DTO로 매핑
    - ResponseEntity.ok(...)로 반환
4) 응답은 항상 아래 공통 래퍼를 사용:
    - Success: { "data": ... }
      (이미 프로젝트에 ApiResponse/SuccessResponse 같은 공통 래퍼가 있으면 그걸 우선 사용)

# 출력 형식
- 변경/추가된 파일의 "전체 코드"를 파일 경로와 함께 출력해라.
- 불필요한 설명은 하지 말고 코드만 출력해라.
```