# Contract Sync (Authz-server)

- Contract Source: https://github.com/jho951/contract
- Service SoT Branch: `main`
- Contract Role: Authorization/policy owner
- Responsibility Split: authz-service owns capability truth, user-service owns visibility/privacy, editor consumes final enforcement

## Required Links
- Common README: https://github.com/jho951/contract/blob/main/contracts/common/README.md
- Routing: https://github.com/jho951/contract/blob/main/contracts/common/routing.md
- Headers: https://github.com/jho951/contract/blob/main/contracts/common/headers.md
- Security: https://github.com/jho951/contract/blob/main/contracts/common/security.md
- Env: https://github.com/jho951/contract/blob/main/contracts/common/env.md
- Service Ownership: https://github.com/jho951/contract/blob/main/contracts/common/service-ownership.md
- Adoption Matrix: https://github.com/jho951/contract/blob/main/contracts/common/adoption-matrix.md
- Authz README: https://github.com/jho951/contract/blob/main/contracts/authz/README.md
- Authz API: https://github.com/jho951/contract/blob/main/contracts/authz/api.md
- Authz v2: https://github.com/jho951/contract/blob/main/contracts/authz/v2.md
- Authz RBAC: https://github.com/jho951/contract/blob/main/contracts/authz/rbac.md
- Authz Audit: https://github.com/jho951/contract/blob/main/contracts/authz/audit.md
- Authz Security: https://github.com/jho951/contract/blob/main/contracts/authz/security.md
- Authz Ops: https://github.com/jho951/contract/blob/main/contracts/authz/ops.md
- Authz Errors: https://github.com/jho951/contract/blob/main/contracts/authz/errors.md
- Authz OpenAPI v1: https://github.com/jho951/contract/blob/main/contracts/openapi/authz-service.v1.yaml
- Authz OpenAPI v2: https://github.com/jho951/contract/blob/main/contracts/openapi/authz-service.v2.yaml
- Gateway Auth Proxy: https://github.com/jho951/contract/blob/main/contracts/gateway/auth-proxy.md
- Gateway Security: https://github.com/jho951/contract/blob/main/contracts/gateway/security.md
- Gateway Cache: https://github.com/jho951/contract/blob/main/contracts/gateway/cache.md
- Gateway Env: https://github.com/jho951/contract/blob/main/contracts/gateway/env.md
- User Visibility: https://github.com/jho951/contract/blob/main/contracts/user/visibility.md

## Sync Checklist
- [ ] `/permissions/internal/admin/verify` matches gateway expectations
- [ ] `GET /health` and `GET /ready` match operational contract
- [ ] v2 `POST /v2/permissions/authorize` and query/introspect endpoints are documented
- [ ] RBAC, audit, and versioning semantics match `contracts/authz/v2.md`
- [ ] Gateway headers (`X-User-Id`, `X-User-Role`, `X-Session-Id`, `X-Original-Method`, `X-Original-Path`) match contract
- [ ] Redis prefix ownership matches `contracts/redis/keys.md`
- [ ] capability truth is not conflated with user visibility/privacy or editor enforcement
