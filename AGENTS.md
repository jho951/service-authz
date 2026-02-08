# CLAUDE.md

이 저장소는 **대학교 수강신청 시스템(REST API 서버)** 과제 구현 레포입니다.  
AI 에이전트(Codex/ChatGPT 등)는 아래 규칙을 반드시 지킵니다.

---

## 0) 최우선 목표(평가 우선순위 반영)

1. **항상 실행 가능한 상태 유지**
    - 서버가 정상 구동되고, `GET /health`가 **항상 200 OK**를 반환해야 한다.
2. **핵심 기능 우선 구현**
    - 수강신청/취소, 정원 초과 방지(동시성), 학점 제한(18), 시간 충돌 방지, 중복 신청 방지.
3. **사고의 깊이는 문서로 증명**
    - 요구사항 해석과 결정 사항은 `docs/REQUIREMENTS.md`에 기록한다.
    - API 명세는 `docs/API.md`에 “평가자가 문서만으로 테스트 작성 가능” 수준으로 작성한다.
4. **검증 가능한 결과물**
    - 동시성 테스트(정원 1, 동시 100 요청 → 성공 1)를 반드시 포함한다.
5. **AI 활용 흔적을 누락하지 않는다**
    - `prompts/`에 실제 사용 프롬프트를 기록/내보내기하여 제출한다(누락 금지).

---

## 1) 기술/구현 제약

- Language: **Java**
- Framework: **Spring Boot**
- Build: **Gradle**
- API: **REST**
- Storage: **H2 in-memory + JPA** (권장)
    - 단, 동시성 핵심 요구(정원 초과 방지)는 **애플리케이션 레벨 Per-course Lock**으로 보장한다.
- 데이터 생성(Seeder): 서버 시작 시 **1분 이내**
    - 최소 수량: 학과 10+, 강좌 500+, 학생 10,000+, 교수 100+

---

## 2) 필수 산출물 및 위치

- `README.md` : 빌드/실행/테스트 방법, 포트, `/health` 안내
- `docs/REQUIREMENTS.md` : Stable Spec(요구사항 분석/결정/동시성 전략/테스트 시나리오)
- `docs/API.md` : 엔드포인트별 요청/응답/에러(상태코드+에러코드+예시)
- `prompts/` : AI 입력 프롬프트 이력(누락 금지)
- `src/` : 소스 코드 + 테스트 코드

---

## 3) 작업 방식(필수 프로세스)

AI는 아래 순서를 지켜 작업한다.

### Step A. 먼저 “설계”를 정리한다
- 요구사항 요약 및 Stable Spec 준수 여부 체크
- 불명확한 부분 목록 + 확정된 결정(Stable Decisions)
- 데이터 모델(학생/강좌/교수/수강신청)
- 동시성 제어 전략(Per-course Lock) 및 임계영역 범위
- 규칙 우선순위(정원/학점/시간충돌/중복/취소)
- 핵심 로직의 시간/공간 복잡도

### Step B. 테스트 계획을 먼저 세우고 구현한다
- 동시성 테스트: capacity=1 강좌에 100 동시 요청 → 성공 1, 실패 99, 최종 enrolled=1
- 학점/시간충돌/중복/취소 테스트 최소 1개씩

### Step C. 구현은 “최소 변경(diff)” 원칙
- 한번에 큰 리팩터링 금지
- 작은 단위로 안전하게 변경
- 변경 후 항상 빌드/테스트가 깨지지 않게 유지

---

## 4) Stable Spec: 불변 규칙(Invariants)

아래 규칙은 어떤 상황에서도 항상 참이어야 한다.

1) **정원 초과 금지**
- 어떤 동시 요청 상황에서도 `enrolled <= capacity`
- 정원이 1 남은 강좌에 100명이 동시에 신청해도 **성공은 정확히 1건**

2) **학점 제한**
- 학생의 이번 학기 신청 학점 총합은 `<= 18`

3) **시간표 충돌 금지**
- 동일 학생이 신청한 두 강좌의 시간이 겹치면 신청 불가
- 충돌 판정: 같은 요일이고 `start < otherEnd && otherStart < end`

4) **중복 신청 금지**
- 동일 학생이 동일 강좌(courseId)를 중복 신청 불가

---

## 5) 동시성 제어(핵심 요구)

### 5.1 최종 선택: Per-course Lock 기반 임계영역
- 강좌별로 Lock(ReentrantLock)을 보유한다.
- 수강 신청 처리 시 해당 강좌 Lock을 획득하고 임계영역에서 규칙 검증 + 등록을 처리한다.
- 본 과제 구현은 단일 애플리케이션 인스턴스를 가정한다.
- 멀티 인스턴스 확장은 문서로만 남긴다(DB 비관적 락/분산 락).

### 5.2 임계영역(원자성) 범위
수강 신청은 아래 단계를 하나의 임계영역으로 처리한다.

- 정원 확인
- 중복/학점/시간충돌 검사
- 등록 생성
- enrolled 증가
- 응답 반환

---

## 6) API 문서/스키마 준수 규칙

- API는 `docs/API.md`의 Stable Spec을 “정답”으로 간주한다.
- 응답 규칙:
    - 성공: `{ "data": ... }`
    - 실패: `{ "error": ... }`
- 목록 조회는 `page`, `size`를 지원하며 기본 정렬은 `id ASC`

---

## 7) 에러 응답 표준(Stable Spec과 완전 일치)

### 7.1 공통 에러 스키마
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
### 7.2 HTTP 상태 코드 규칙

400: 입력 값 오류

404: 리소스 없음(학생/강좌/교수/등록)

409: 비즈니스 규칙 충돌(정원/학점/시간충돌/중복/이미 취소)

500: 서버 오류

### 7.3 대표 에러 코드

REG_CAPACITY_FULL

REG_MAX_CREDITS

REG_TIME_CONFLICT

REG_DUPLICATE_COURSE

REG_NOT_FOUND

REG_ALREADY_CANCELED

참고: 과목코드(subjectCode) 기반 분반 중복 금지는 이번 MVP에서는 미적용(옵션)이며,
구현하지 않는다면 관련 에러 코드는 문서/코드에서 제외하거나 “미구현”으로만 남긴다.

## 8) Seeder 및 /health 규칙

서버 시작 시 데이터는 동적으로 생성한다(1분 이내).

GET /health는 항상 200 OK를 반환한다.

/health 응답에는 seeding 상태와 count를 포함한다(테스트 편의).

예시:
```
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

## 9) 커밋/프롬프트 기록 규칙(평가 대비)

커밋은 “의미 있는 단위”로 쪼갠다.

예) docs: add stable requirements and api spec (AI-assisted)

예) feat: implement registration with per-course lock

예) test: add concurrency test for capacity=1

AI 활용은 숨기지 않는다.

문서에 “AI-assisted” 표기 가능

prompts/에 실제 사용 프롬프트를 반드시 남긴다.
# Prompts Log (AI 활용 이력)

> 목적: 평가자가 “어떻게 AI와 협업했는지”를 재현 가능하게 확인할 수 있도록, 실제 입력 프롬프트와 의사결정을 기록한다.  
> 규칙: 프롬프트는 가능한 원문 그대로 기록하고, 결과물(문서/코드) 반영 여부를 함께 적는다.

---

## 0. 프로젝트 컨텍스트

- 과제: 대학교 수강신청 시스템(REST API 서버)
- 핵심 요구: 정원 1 남은 강좌에 100 동시 신청 → 정확히 1명만 성공, 정원 초과 불가
- 구현 스택: Spring Boot + Gradle + (H2 in-memory + JPA 권장)
- Stable Spec: `docs/REQUIREMENTS.md`, `docs/API.md` 기준

---

## 1. 요구사항 안정화(Stable Spec) 생성

### Prompt (원문)
```text
[여기에 너가 Codex/ChatGPT에 실제로 입력한 요구사항 원문/지시를 붙여넣기]
```