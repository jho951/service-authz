# 대학교 수강신청 시스템 (REST API)

이 프로젝트는 **Spring Boot + H2(JPA)** 기반의 수강신청 서버입니다.  
동시성 상황에서도 **정원 초과가 절대 발생하지 않도록** 강좌별 `ReentrantLock` 기반 임계영역을 사용합니다.

## 실행 환경
- Java 17
- Spring Boot 3.3.x
- Gradle
- H2 In-Memory Database

## 빌드/실행
```bash
./gradlew clean build
./gradlew bootRun
```

## 테스트
```bash
./gradlew test
```

## 헬스 체크
```bash
GET /health
```
항상 200 OK를 반환하며, seeding 상태/카운트를 포함합니다.

## 기본 포트 / 콘솔
- 서버 포트: `8080`
- H2 콘솔: `GET /h2-console`

## 문서
- 요구사항/설계: `docs/REQUIREMENTS.md`
- API 명세: `docs/API.md`
- Swagger UI: `GET /swagger-ui.html`

## 데이터 초기 생성(Seeder)
- 서버 시작 시 자동 생성
- 학생 10,000 / 교수 100 / 강좌 500
- 시간표 슬롯: 월~금 09:00~21:00 (90분 단위)
- 이름 생성: 성씨 30 x 이름 50 조합(1,500개) 풀에서 랜덤 배정
- 최초 실행 시 위 데이터가 DB(H2 In-Memory)에 자동 삽입됩니다.

H2 콘솔에서 간단 확인:
```sql
SELECT COUNT(*) FROM STUDENT;
SELECT COUNT(*) FROM PROFESSOR;
SELECT COUNT(*) FROM COURSE;
```

## 동시성 보장
강좌별 `ReentrantLock`을 사용하여 아래 임계영역을 원자적으로 처리합니다.
- 정원 확인 → 중복/학점/시간충돌 검사 → 등록 생성 → enrolled 증가

## 프로젝트 파일 안내
- `build.gradle`: Gradle 빌드 스크립트 (의존성/플러그인 정의)
- `package.json`: **해당 없음 (Node.js 사용하지 않음)**
- `requirements.txt`: **해당 없음 (Python 사용하지 않음)**

## 참고
AI 프롬프트 기록은 `prompts/`에 유지됩니다.
