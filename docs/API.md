# API 문서 (Stable Spec)

## 공통 사항
- Base URL: `/`
- Content-Type: `application/json`
- 성공 응답은 `data` 필드 사용
- 실패 응답은 `error` 필드 사용
- 목록 조회는 `page`, `size`를 지원하며 기본 정렬은 `id ASC`

### 공통 에러 응답
```json
{
  "error": {
    "code": "REG_CAPACITY_FULL",
    "message": "정원이 가득 찼습니다.",
    "detail": {
      "courseId": 1
    }
  }
}
```

대표 에러 코드:
- REG_CAPACITY_FULL
- REG_MAX_CREDITS
- REG_TIME_CONFLICT
- REG_DUPLICATE_COURSE
- REG_NOT_FOUND
- REG_ALREADY_CANCELED

## 1) Health
### GET /health
- 설명: 서버 상태 확인. 항상 200 반환.
- 응답 200:
```json
{
  "status": "UP",
  "seeding": "DONE",
  "counts": {
    "students": 10000,
    "courses": 500,
    "professors": 100
  }
}
```

## 2) 학생
### GET /students
- 설명: 학생 목록 조회
- 쿼리 파라미터(선택):
  - `page` (기본 0)
  - `size` (기본 20)
- 응답 200:
```json
{
  "data": [
    {
      "id": 1001,
      "name": "Student-1001",
      "departmentId": 3,
      "totalCredits": 12
    }
  ]
}
```

### GET /students/{studentId}
- 설명: 학생 단건 조회
- 응답 200:
```json
{
  "data": {
    "id": 1001,
    "name": "Student-1001",
    "departmentId": 3,
    "totalCredits": 12
  }
}
```
- 응답 404: `REG_NOT_FOUND`

### GET /students/{studentId}/schedule
- 설명: 내 시간표(이번 학기) 조회
- 응답 200:
```json
{
  "data": [
    {
      "courseId": 120,
      "courseName": "Intro to CS",
      "credits": 3,
      "departmentName": "Computer Science",
      "professorName": "Prof-55",
      "schedule": [
        "월 09:00-10:30",
        "수 09:00-10:30"
      ]
    }
  ]
}
```
- 응답 404: `REG_NOT_FOUND`

## 3) 교수
### GET /professors
- 설명: 교수 목록 조회
- 쿼리 파라미터(선택):
  - `page` (기본 0)
  - `size` (기본 20)
- 응답 200:
```json
{
  "data": [
    { "id": 55, "name": "Prof-55", "departmentId": 3 }
  ]
}
```

### GET /professors/{professorId}
- 설명: 교수 단건 조회
- 응답 200:
```json
{
  "data": { "id": 55, "name": "Prof-55", "departmentId": 3 }
}
```
- 응답 404: `REG_NOT_FOUND`

## 4) 강좌
### GET /courses
- 설명: 강좌 목록 조회 (전체/학과별)
- 쿼리 파라미터(선택):
  - `departmentId`
  - `page` (기본 0)
  - `size` (기본 20)
- 응답 200:
```json
{
  "data": [
    {
      "id": 120,
      "name": "Intro to CS",
      "departmentId": 3,
      "professorId": 55,
      "credits": 3,
      "capacity": 40,
      "enrolled": 38,
      "schedule": [
        "월 09:00-10:30",
        "수 09:00-10:30"
      ]
    }
  ]
}
```

### GET /courses/{courseId}
- 설명: 강좌 단건 조회
- 응답 200: 위 강좌 단건 형태
- 응답 404: `REG_NOT_FOUND`

## 5) 수강 신청/취소
### POST /registrations
- 설명: 수강 신청
- 요청:
```json
{
  "studentId": 1001,
  "courseId": 120
}
```
- 응답 201:
```json
{
  "data": {
    "registrationId": 9001,
    "studentId": 1001,
    "courseId": 120,
    "status": "ENROLLED"
  }
}
```
- 에러:
  - 409 `REG_CAPACITY_FULL`
  - 409 `REG_DUPLICATE_COURSE`
  - 409 `REG_MAX_CREDITS`
  - 409 `REG_TIME_CONFLICT`
  - 404 `REG_NOT_FOUND`

### DELETE /registrations/{registrationId}
- 설명: 수강 취소
- 응답 200:
```json
{
  "data": {
    "registrationId": 9001,
    "status": "CANCELED"
  }
}
```
- 에러:
  - 404 `REG_NOT_FOUND`
  - 409 `REG_ALREADY_CANCELED`

