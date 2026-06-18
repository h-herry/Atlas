# atlas-user — 用户认证与权限管理 / User Authentication & Permission Management

## 功能概述 / Overview

`atlas-user` 模块负责系统的用户认证（JWT 双 Token）、RBAC 权限管理和部门组织架构。采用无状态 JWT + Redis 权限缓存方案，支持 Token 续期、登录锁定、Token 黑名单登出失效、4 级数据权限隔离。

---

The `atlas-user` module handles user authentication (JWT dual-token), RBAC permission management, and departmental organizational structure. It employs a stateless JWT + Redis permission caching scheme, supporting token renewal, login lockout, token blacklist logout invalidation, and 4-level data permission isolation.

**端口 / Port**：8081 | **数据库 / Database**：atlas_user | **文件数 / File count**：14 Java classes

---

## 数据库表列表 / Database Tables（6 张 / 6 tables）

| 表名 / Table | 说明 / Description | 迁移脚本 / Migration |
|------|------|----------|
| department | 部门表（树形结构，dept_path 字段实现层级） / Department table (tree structure via dept_path) | V1 |
| user | 用户表（BCrypt 密码、登录失败次数、锁定到期时间） / User table (BCrypt password, login fail count, lock expiration) | V1 |
| role | 角色表（含 data_scope 数据权限级别） / Role table (with data_scope permission level) | V1 |
| permission | 权限表（菜单/按钮/接口三级粒度，含 parent_id） / Permission table (menu/button/API three-tier granularity) | V1 |
| user_role | 用户-角色关联表 / User-role association table | V1 |
| role_permission | 角色-权限关联表 / Role-permission association table | V1 |

---

## Controller API 列表 / Controller API List

### AuthController（/api/auth）

| 端点 / Endpoint | 方法 / Method | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/auth/login` | POST | 无 / None | 用户登录，返回 Access Token + Refresh Token / Login, return tokens |
| `/api/auth/refresh` | POST | 无 / None | Token 续期，使用 Refresh Token 换取新 Access Token / Token renewal |
| `/api/auth/logout` | POST | 需要登录 / Login required | 登出，Access Token 加入 Redis 黑名单 / Logout, token added to blacklist |

### UserController（/api/user）

| 端点 / Endpoint | 方法 / Method | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/user/page` | GET | — | 分页查询用户 / Paginated user query |
| `/api/user/{id}` | GET | — | 根据 ID 查询用户 / Query user by ID |

---

## 核心 Service / Core Services

### LoginService

- `login(username, password)`：密码 BCrypt 校验 → 失败次数累加 → 5 次锁定 15 分钟 → 成功则清零并生成 Token / BCrypt password check → increment fail count → lock for 15 min after 5 failures → reset and generate tokens on success
- `refresh(refreshToken)`：验证 Refresh Token 有效性 → 生成新 Access Token / Validate refresh token → generate new access token
- `logout(accessToken)`：将 Token 加入 Redis 黑名单（TTL = Token 剩余有效期）/ Add token to Redis blacklist (TTL = remaining validity)

### TokenService

- `generateAccessToken(userId, permissions)`：生成 Access Token（HMAC-SHA256，2h 过期）/ Generate access token (HMAC-SHA256, 2h expiry)
- `generateRefreshToken(userId)`：生成 Refresh Token（更长有效期）/ Generate refresh token (longer validity)
- `validateAccessToken(token)`：验证签名 + 黑名单 + 过期检查 / Validate signature + blacklist + expiry check
- `getPermissions(userId)`：Redis 缓存权限列表，缓存未命中时从 DB 查询 / Redis cached permissions, query DB on cache miss

---

## 技术要点 / Technical Details

### 登录锁定机制 / Login Lockout Mechanism

- `user.login_fail_count`：失败次数累加 / Incremental failure count
- `user.lock_until`：锁定到期时间（5 次失败后设置 = 当前时间 + 15 分钟）/ Lock expiration (set after 5 failures = now + 15 min)
- 登录成功时自动重置 `login_fail_count = 0`、`lock_until = null` / Auto-reset on successful login

### Token 续期 / Token Renewal（RenewTokenHolder）

- `RenewTokenHolder`（ThreadLocal）在 JWT 过滤器中设置 / Set in JWT filter
- Controller 响应前通过 `AuditLogAspect` / `RateLimitAspect` 等切面自动续期 / Auto-renewed via aspects before controller response
- 续期条件：Token 剩余有效期 < 阈值 / Renewal condition: remaining validity < threshold

### Token 黑名单登出 / Token Blacklist Logout

- 登出时 Access Token 写入 Redis `blacklist:token:{token}`，TTL = 剩余有效期 / Write token to Redis blacklist on logout
- `JwtAuthenticationFilter` 验证阶段检查黑名单 / Checked during JWT filter validation

### 数据权限 / Data Permission（DataScopeInterceptor）

- `@DataScope` 注解配合 `DataScopeInterceptor` 切面 / Annotation + aspect
- 4 级数据隔离：仅本人 / 本部门 / 本部门及子部门 / 全部 / 4-level: self, own dept, dept & sub-depts, all
- 通过 `dept_path` 字段实现部门树形权限 / Dept tree via dept_path

### 密码安全 / Password Security

- BCrypt 加密存储（`BCryptPasswordEncoder`）/ BCrypt hashed storage
- Spring Security `DaoAuthenticationProvider` 整合 / Spring Security integration
