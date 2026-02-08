# 06 - Seeder 구현
- Date: 2026-02-08
- Tool: Codex
- Goal: Seeder

## Prompt (원문)
```text
Seeder 기능으로

DataSeeder 1개 만들어서: 학생 10,000 / 교수 100 / 강좌 500 생성

강좌 스케줄은 겹침이 너무 많지 않게 “요일+시간 슬롯 풀”에서 랜덤

/health는 이미 컨트롤러가 있다면, 응답에 seeding 상태 + counts만 넣어주면 문서와 일치.

도메인에 이미 departmentId, departmentName가 박혀있는 형태라 department는 별도 테이블 없이도 돼 
```