# CI and Implementation

## 로컬 명령
```bash
./gradlew test
./gradlew :app:bootJar
bash scripts/run.local.sh
```

`scripts/run.local.sh`는 `.env.local`을 읽고 `./gradlew :app:bootRun`을 실행합니다.

## Docker 명령
```bash
bash scripts/run.docker.sh up dev
bash scripts/run.docker.sh ps dev
bash scripts/run.docker.sh logs dev
bash scripts/run.docker.sh down dev

bash scripts/run.docker.sh up prod
```

Docker build는 루트에서 `:app:bootJar`를 실행하고 `app/build/libs` 산출물을 이미지에 복사합니다.

## CI
GitHub Actions는 아래 workflow를 사용합니다.
- `.github/workflows/ci.yml`: JDK 17 설정, `./gradlew --no-daemon clean build`, dev/prod compose config 검증
- `.github/workflows/contract-check.yml`: PR에서 contract impact check 실행
- `.github/workflows/cd.yml`: main push 또는 `v*` tag에서 Amazon ECR image build/push

CI와 Docker build는 `settings.gradle`의 GitHub Packages repository에서 platform private package를 해석하기 위해 credential이 필요합니다.
- `GITHUB_ACTOR`
- `GITHUB_TOKEN` 또는 Gradle property `githubPackagesToken`

## 문서 갱신 규칙
- 제품 요구사항과 채택 정책은 영향받는 주제별 문서에 반영합니다.
- 서비스 경계는 [architecture.md](./architecture.md), API 계약은 [api.md](./api.md), 저장 모델은 [database.md](./database.md), 플랫폼 운영 정책은 [platform.md](./platform.md)를 기준으로 갱신합니다.
- 채택 전 전략 검토, 회의 메모, 비교 문서가 필요하면 별도 discussion 문서를 만들고 관련 주제 문서에서 연결합니다.
- 채택된 기술 결정은 관련 주제 문서의 채택 결정 요약에 반영합니다.
- 재현 가능한 디버깅 절차는 [troubleshooting.md](./troubleshooting.md)에 유지합니다.
- 기능별 후속 Todo와 확장 검토는 관련 주제 문서나 별도 roadmap 문서에 정리합니다.
- 모든 AI 작업은 [../prompts/](../prompts/)에 최소 1개 이상 로그를 남깁니다.

## Prompt Log
- 프롬프트 로그는 가능한 한 작업 목표 단위의 단일 파일로 유지합니다.
- 같은 목표 아래 이어지는 후속 작업은 기존 로그에 `Step`을 추가합니다.
- 커밋을 여러 개로 나누더라도 하나의 궁극적 목표를 향한 연속 작업이면 한 파일에 단계별로 누적합니다.
- 모든 대화나 자잘한 정리까지 남기지는 않고, 설계 결정, 구현 단계, 검증 결과 중심으로 기록합니다.
- 작은 작업은 날짜, 작업 목적, 핵심 변경만 적은 3-5줄 요약으로 충분합니다.

## ADR 기준
ADR은 아래처럼 되돌리기 어렵거나 팀 합의가 필요한 경우에 작성합니다.
- 아키텍처 변경
- 인증/보안 정책 변경
- 상태 관리 방식 변경
- 배포/운영 정책 변경

구현 세부 조정, 국소 버그 수정, UI 문구 변경, 단순 리팩터링은 ADR 없이 진행할 수 있습니다.

## 코드 작성 기준
- 코드 주석은 꼭 필요한 경우에만 추가합니다.
- 에러 메시지와 예외 메시지는 특별한 사유가 없으면 한글로 작성합니다.
- 가독성과 성능 차이가 크지 않으면 가독성을 우선합니다.
- Service, UseCase, Controller, Validator, Mapper 계층은 처음 읽는 사람도 흐름을 파악할 수 있게 명시적으로 작성합니다.
- 메서드 분리는 역할 경계가 실제로 더 선명해질 때만 적용합니다.
- 단순 값 정리, 한 줄 위임, 컬렉션 생성, 한 번만 호출되는 얇은 포장 메서드는 만들지 않습니다.
- 호출부만 봐도 무엇을 검증, 조회, 수집, 변경하는지 읽히도록 메서드명을 작성합니다.
- 의도를 바로 알기 어려운 조건문은 `validate...`, `ensure...`, `is...`, `has...`, `can...` 계열 메서드로 감쌉니다.
- 작은 처리 단위가 바뀌는 지점에서는 빈 줄과 들여쓰기로 읽기 단위를 드러냅니다.
- 추상화는 "나중에 재사용할 수도 있음"이 아니라 "지금 의미가 늘어나는가"를 기준으로 판단합니다.

## PR 기대사항
- 제품 코드 동작 변경은 목적, 근거, 영향 범위를 PR에 명시합니다.
- 사용한 prompt log 경로를 PR 본문에 포함합니다.
- 결정 사항이 있으면 갱신한 주제 문서 경로를 함께 링크합니다.
- 주제별 문서 변경 여부와 사유를 명시합니다.
- 기존 요구사항 범위 안의 버그 수정은 문서 갱신 없이 진행할 수 있으나, PR에 기존 요구사항 범위 안의 수정임을 적습니다.
