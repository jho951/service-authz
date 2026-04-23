# Platform

## 소비 버전
- `platform-governance`: `3.0.0`
- `platform-security`: `3.0.0`
- `platform-integrations`: `3.0.0`

Gradle은 GitHub Packages에서 아래 BOM과 starter를 소비합니다.
- `io.github.jho951.platform:platform-runtime-bom:3.0.0`
- `io.github.jho951.platform:platform-governance-bom:3.0.0`
- `io.github.jho951.platform:platform-governance-starter`
- `io.github.jho951.platform:platform-security-bom:3.0.0`
- `io.github.jho951.platform:platform-security-starter`
- `io.github.jho951.platform:platform-security-legacy-compat`
- `io.github.jho951.platform:platform-security-web-api`
- `io.github.jho951.platform:platform-security-governance-bridge:3.0.0`
- `io.github.jho951:audit-log-api`

## GitHub Packages
root `settings.gradle`의 `dependencyResolutionManagement`는 `platform-governance`, `platform-security`, `platform-integrations` GitHub Packages repository를 등록합니다. Project-level repository 선언은 `RepositoriesMode.FAIL_ON_PROJECT_REPOS`로 막습니다.

인증 우선순위:
1. Gradle property `githubPackagesUsername`, `githubPackagesToken`
2. legacy property `githubUsername`, `githubToken`, `ghToken`, `gh_token`
3. 환경변수 `GITHUB_ACTOR`, `GH_TOKEN`, `GITHUB_TOKEN`

## platform-security
Authz-server는 platform-security를 ingress boundary, IP guard, rate limit, security audit 기본 흐름에 사용합니다.

주요 설정:
- `platform.security.service-role-preset=INTERNAL_SERVICE`
- `platform.security.boundary.internal-paths=/permissions/internal/**`
- `platform.security.auth.internal-token-enabled=false`
- `platform.security.ip-guard.enabled`
- `platform.security.rate-limit.enabled`
- `permission.internal-auth.mode`

`POST /permissions/internal/admin/verify`는 `INTERNAL` boundary로 분류합니다.

현재 내부 caller proof:
- 운영 profile은 `permission.internal-auth.mode=JWT`로 internal service JWT를 검증합니다.
- local/test 호환을 위해 `LEGACY_SECRET`, `HYBRID` 모드에서 `platform-security-legacy-compat`가 `X-Internal-Request-Secret`을 platform-owned compat seam으로 수용합니다.

운영 필수값:
- `PLATFORM_SECURITY_JWT_SECRET`
- `AUTHZ_INTERNAL_JWT_SECRET`
- `PLATFORM_SECURITY_INTERNAL_ALLOW_CIDRS`
- `PLATFORM_SECURITY_ADMIN_ALLOW_CIDRS`
- `REDIS_HOST`
- `REDIS_PORT`

## platform-governance
Authz-server는 platform-governance로 control-plane 정책 검사를 등록합니다.

운영 profile 기준:
- governance audit enabled
- strict engine enabled
- violation action `DENY`
- handler failure fatal enabled
- policy config required in enforcing mode
- audit delivery uses `AuditSink` through `platform-governance` mainline starter

현재 등록 정책:
- `authz.permission` 리소스의 `grant` 액션에서 `*` 또는 `:*`로 끝나는 wildcard permission grant 차단

## 검증 명령
```bash
./gradlew :app:dependencyInsight --dependency platform-security-starter --configuration runtimeClasspath
./gradlew :app:dependencyInsight --dependency platform-governance-starter --configuration runtimeClasspath
./gradlew test
```

기대 버전:
- `platform-security-starter:3.0.0`
- `platform-governance-starter:3.0.0`
