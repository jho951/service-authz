# 07 - RegistrationService를 구현
- Date: 2026-02-08
- Tool: Codex
- Goal: RegistrationService

## Prompt (원문)
```text
목표는 동시성 상황에서도 정원 초과가 절대 발생하지 않도록 “강좌별 Lock(Per-course ReentrantLock)”으로 임계영역을 만들고,
Stable Spec에 맞춰 수강신청/취소 규칙을 정확히 지키는 것이다.

# 작업 범위(반드시 지킬 것)
- 수정 파일: src/main/java/**/registration/RegistrationService.java (이 파일만 변경)
- 기존 Controller/Repository/Entity 구조는 유지
- CourseLockManager(강좌별 Lock 제공)가 이미 존재하므로 반드시 사용
- “삭제”가 아니라 Registration.status를 ENROLLED/CANCELED로 변경하여 취소 처리
- 성공 응답:
  - register: HTTP 201
  - cancel: HTTP 200
- 실패 응답은 전역 예외처리(@ControllerAdvice)에서 처리한다고 가정하고,
  여기서는 아래 커스텀 예외(또는 RuntimeException)들을 던지는 형태로 구현해라.
  (이미 프로젝트에 예외 타입이 있다면 그걸 사용하고, 없다면 간단한 예외 클래스를 RegistrationService 내부 static class로 만들어도 됨)

# 도메인 규칙(락 안에서 원자적으로 처리)
## register(studentId, courseId)
1) student/course 존재 확인. 없으면 404 (REG_NOT_FOUND)
2) courseId로 Lock 획득 (try/finally로 반드시 unlock)
3) lock 안에서 아래 순서로 검증 + 처리 (순서 고정):
   A. 정원 체크: course.enrolledCount < course.capacity 아니면 409 REG_CAPACITY_FULL
   B. 중복 체크: registrationRepository.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, ENROLLED)면 409 REG_DUPLICATE_COURSE
   C. 학점 체크: studentId의 ENROLLED registrations를 조회해 총 credits 합을 계산하고,
      (현재 합 + 신규 course.credits) > 18 이면 409 REG_MAX_CREDITS
   D. 시간충돌 체크:
      - studentId의 ENROLLED 강좌들의 schedule과 신규 course.schedule을 비교
      - 충돌 조건: 같은 요일 && (start < otherEnd && otherStart < end)면 409 REG_TIME_CONFLICT
      - schedule은 문자열이 아니라 내부 모델(요일+startMinute+endMinute)로 변환해서 비교 (필요 시 private helper로 파싱)
   E. Registration을 ENROLLED로 저장
   F. course.enrolledCount++ 후 저장
4) 201 반환용 DTO/엔티티를 리턴

## cancel(registrationId)
1) registration 조회. 없으면 404 REG_NOT_FOUND
2) 이미 CANCELED면 409 REG_ALREADY_CANCELED
3) 해당 registration의 courseId로 Lock 획득
4) lock 안에서:
   - status=CANCELED로 변경 후 저장
   - course.enrolledCount-- (0 밑으로 내려가면 0으로 clamp) 후 저장
5) 200 반환용 DTO/엔티티 리턴

# 스케줄 파싱 규격(Stable Spec)
- schedule 문자열 예: "월 09:00-10:30"
- 요일: 월/화/수/목/금/토/일
- 시간은 HH:mm, 분 단위는 00 또는 30만 나온다고 가정
- 비교는 분(minute) 정수로 변환해서 처리

# 출력 형식
- 수정된 RegistrationService.java의 전체 코드만 출력해라.
- 다른 파일은 수정하지 마라.
```