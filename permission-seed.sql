-- Authz Service seed data (PostgreSQL)
-- Assumed tables:
--   roles(id varchar(36), name, description, created_at)
--   permissions(id varchar(36), code, description, created_at)
--   role_permissions(id varchar(36), role_id varchar(36), permission_id varchar(36), created_at)
--   user_roles(id varchar(36), user_id, role_id varchar(36), scope_type, scope_id, created_at)
--
-- This script is idempotent with NOT EXISTS checks.

BEGIN;

-- 1) Roles
INSERT INTO roles (id, name, description, created_at)
SELECT gen_random_uuid()::text, 'ADMIN', 'System administrator', NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (id, name, description, created_at)
SELECT gen_random_uuid()::text, 'MANAGER', 'Workspace manager', NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MANAGER');

INSERT INTO roles (id, name, description, created_at)
SELECT gen_random_uuid()::text, 'MEMBER', 'General member', NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MEMBER');

-- 2) Permissions (Admin)
INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'ADMIN_READ', 'Read admin resources', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_READ');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'ADMIN_WRITE', 'Write admin resources', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_WRITE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'ADMIN_DELETE', 'Delete admin resources', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_DELETE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'ADMIN_MANAGE', 'Super admin manage permission', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_MANAGE');

-- 3) Permissions (Member/Workspace/Document)
INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'DOC_READ', 'Read documents', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DOC_READ');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'DOC_CREATE', 'Create documents', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DOC_CREATE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'DOC_UPDATE', 'Update documents', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DOC_UPDATE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'DOC_DELETE', 'Delete documents', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DOC_DELETE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'WORKSPACE_READ', 'Read workspace', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'WORKSPACE_READ');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'WORKSPACE_INVITE', 'Invite workspace members', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'WORKSPACE_INVITE');

INSERT INTO permissions (id, code, description, created_at)
SELECT gen_random_uuid()::text, 'WORKSPACE_MANAGE', 'Manage workspace', NOW()
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'WORKSPACE_MANAGE');

-- 4) Role -> Permission mapping
-- ADMIN: all permissions
INSERT INTO role_permissions (id, role_id, permission_id, created_at)
SELECT gen_random_uuid()::text, r.id, p.id, NOW()
FROM roles r
JOIN permissions p ON 1 = 1
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- MANAGER: workspace + document manage scope (without admin delete/manage)
INSERT INTO role_permissions (id, role_id, permission_id, created_at)
SELECT gen_random_uuid()::text, r.id, p.id, NOW()
FROM roles r
JOIN permissions p
  ON p.code IN (
    'DOC_READ', 'DOC_CREATE', 'DOC_UPDATE', 'DOC_DELETE',
    'WORKSPACE_READ', 'WORKSPACE_INVITE', 'WORKSPACE_MANAGE',
    'ADMIN_READ'
  )
WHERE r.name = 'MANAGER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- MEMBER: basic workspace/document usage
INSERT INTO role_permissions (id, role_id, permission_id, created_at)
SELECT gen_random_uuid()::text, r.id, p.id, NOW()
FROM roles r
JOIN permissions p
  ON p.code IN (
    'DOC_READ', 'DOC_CREATE', 'DOC_UPDATE',
    'WORKSPACE_READ'
  )
WHERE r.name = 'MEMBER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- 5) Optional sample user-role assignments (edit before use)
-- Example:
-- INSERT INTO user_roles (id, user_id, role_id, scope_type, scope_id, created_at)
-- SELECT gen_random_uuid()::text, 'user-123', r.id, 'workspace', 'workspace-001', NOW()
-- FROM roles r
-- WHERE r.name = 'MEMBER'
--   AND NOT EXISTS (
--     SELECT 1
--     FROM user_roles ur
--     WHERE ur.user_id = 'user-123'
--       AND ur.role_id = r.id
--       AND ur.scope_type = 'workspace'
--       AND ur.scope_id = 'workspace-001'
--   );

COMMIT;
