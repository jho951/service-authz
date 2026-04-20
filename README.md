# authz-service

## 역할

- Gateway가 관리자 경로(`/admin/**`, `/v1/admin/**`)를 처리하기 전 최종 인가 판정을 제공합니다.
- 에디터 관리자 API 보호를 위한 capability 진실을 소유합니다.
- gateway 뒤에서 `/permissions/internal/**` 내부 API를 제공합니다.
- Authz DB의 `user_roles`, `role_permissions`를 기준으로 권한을 판단합니다.
- 권한 검증 결과와 governance 감사 이벤트를 기록합니다.

## 서비스 이름

| 항목 | 값 |
| --- | --- |
| 구현/PR/런타임 이름 | `authz-service` |
| Gradle group | `com.example.permission` |
| 서비스 포트 | `8084` |

## Contract Source

- 공통 계약 레포: `https://github.com/jho951/service-contract`
- 계약 동기화 기준 파일: [contract.lock.yml](contract.lock.yml)
- 계약 변경 절차: [contract-change-workflow.md](docs/contract-change-workflow.md)
- PR에서는 `.github/workflows/contract-check.yml`이 lock 파일과 계약 영향 변경 여부를 검사합니다.
- 인터페이스 변경 시 본 저장소 구현보다 계약 레포 변경을 먼저 반영합니다.

## 빠른 시작

GitHub Packages 의존성을 받으려면 `GH_TOKEN`이 필요합니다.

```bash
export GITHUB_ACTOR=jho951
export GH_TOKEN=<github-token-with-read-packages>
```

### Docker 개발 스택 실행:

```bash
./scripts/run.docker.sh up dev
```

### 로컬 직접 실행:

```bash
./scripts/run.local.sh
```

### 빌드와 테스트:

```bash
./gradlew build
```

### 상태 확인:

```bash
curl -i http://localhost:8084/actuator/health
curl -i http://localhost:8084/actuator/prometheus
```

### API 문서:

```bash
open http://localhost:8084/swagger-ui.html
```

## 문서

- [문서 홈](docs/README.md)
