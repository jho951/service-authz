# API 문서 (초안)

## 공통 사항
- Base URL: `/`
- Content-Type: `application/json`
- 성공 응답은 `data` 필드 사용
- 실패 응답은 `error` 필드 사용

### 공통 에러 응답

{
"error": {
"code": "REG_CAPACITY_FULL",
"message": "정원이 가득 찼습니다.",
"detail": {
"courseId": 123
}
}
}


## 1) Health
### GET /health
- 설명: 서버 상태 확인. 항상 200 반환.
- 응답 200:

{ "status": "ok" }


## 2) 학생
### GET /students/{studentId}
- 설명: 학생 단건 조회
- 응답 200:

{
"data": {
"id": 1001,
"name": "Student-1001",
"departmentId": 3,
"totalCredits": 12
}
}

- 응답 404: `REG_NOT_FOUND`

### GET /students/{studentId}/registrations
- 설명: 학생 수강내역 조회
- 응답 200:

{
"data": [
{
"registrationId": 9001,
"courseId": 120,
"subjectCode": "CS101",
"credits": 3,
"timeSlots": [
{ "day": "MON", "start": "09:00", "end": "10:30" }
]
}
]
}


## 3) 강좌
### GET /courses
- 설명: 강좌 목록 조회 (필터 선택)
- 쿼리 파라미터 (선택):
    - `departmentId`
    - `professorId`
    - `subjectCode`
    - `page`, `size`
- 응답 200:

{
"data": [
{
"id": 120,
"subjectCode": "CS101",
"title": "Intro to CS",
"departmentId": 3,
"professorId": 55,
"credits": 3,
"capacity": 40,
"enrolled": 38,
"timeSlots": [
{ "day": "MON", "start": "09:00", "end": "10:30" },
{ "day": "WED", "start": "09:00", "end": "10:30" }
]
}
]
}


### GET /courses/{courseId}
- 설명: 강좌 단건 조회
- 응답 200: 위 강좌 단건 형태
- 응답 404: `REG_NOT_FOUND`

### GET /courses/{courseId}/registrations
- 설명: 강좌 수강자 목록
- 응답 200:

{
"data": [
{ "studentId": 1001, "registrationId": 9001 },
{ "studentId": 1002, "registrationId": 9002 }
]
}


## 4) 수강 신청/취소
### POST /registrations
- 설명: 수강 신청
- 요청:

{
"studentId": 1001,
"courseId": 120
}

- 응답 201:

{
"data": {
"registrationId": 9001,
"studentId": 1001,
"courseId": 120,
"status": "ENROLLED"
}
}

- 에러:
    - 400 `REG_MAX_CREDITS`
    - 400 `REG_TIME_CONFLICT`
    - 409 `REG_CAPACITY_FULL`
    - 409 `REG_DUPLICATE_COURSE`
    - 409 `REG_DUPLICATE_SUBJECT`
    - 404 `REG_NOT_FOUND`

### DELETE /registrations/{registrationId}
- 설명: 수강 취소
- 응답 200:

{
"data": {
"registrationId": 9001,
"status": "CANCELED"
}
}

- 에러:
    - 404 `REG_NOT_FOUND`
    - 409 `REG_ALREADY_CANCELED`

## 5) 초기 데이터 상태
### GET /seed/status
- 설명: 초기 데이터 생성 상태 확인
- 응답 200:

{
"data": {
"status": "READY",
"generated": {
"departments": 12,
"professors": 120,
"students": 10000,
"courses": 600
}
}
}

- 상태값: `READY`, `SEEDING`
