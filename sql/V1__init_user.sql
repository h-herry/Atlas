-- ============================================
-- Atlas V1: 用户/部门/角色/权限初始化
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_user;

-- 部门表
CREATE TABLE IF NOT EXISTS department (
    id              BIGINT PRIMARY KEY COMMENT '部门ID',
    parent_id       BIGINT NOT NULL DEFAULT 0 COMMENT '上级部门ID(0=根部门)',
    dept_path       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '部门路径(如 /1/3/15)',
    dept_name       VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_level      TINYINT NOT NULL DEFAULT 1 COMMENT '层级',
    manager_id      BIGINT COMMENT '部门负责人',
    sort_order      INT NOT NULL DEFAULT 0 COMMENT '排序',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent_id (parent_id),
    KEY idx_dept_path (dept_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id              BIGINT PRIMARY KEY COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL COMMENT '登录账号',
    real_name       VARCHAR(50) NOT NULL COMMENT '真实姓名',
    password_hash   VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    dept_id         BIGINT NOT NULL COMMENT '所属部门',
    position        VARCHAR(50) COMMENT '岗位',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    last_login_at   DATETIME COMMENT '最后登录时间',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id              BIGINT PRIMARY KEY COMMENT '角色ID',
    role_code       VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name       VARCHAR(50) NOT NULL COMMENT '角色名称',
    data_scope      TINYINT NOT NULL DEFAULT 1 COMMENT '数据权限:1仅本人 2本部门 3本部门及子部门 4全部',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表（功能权限，粒度到按钮）
CREATE TABLE IF NOT EXISTS permission (
    id              BIGINT PRIMARY KEY COMMENT '权限ID',
    parent_id       BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID(0=菜单)',
    perm_code       VARCHAR(100) NOT NULL COMMENT '权限标识',
    perm_name       VARCHAR(50) NOT NULL COMMENT '权限名称',
    perm_type       TINYINT NOT NULL COMMENT '1菜单 2按钮 3接口',
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户-角色关联
CREATE TABLE IF NOT EXISTS user_role (
    user_id         BIGINT NOT NULL,
    role_id         BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-权限关联
CREATE TABLE IF NOT EXISTS role_permission (
    role_id         BIGINT NOT NULL,
    perm_id         BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 初始数据
-- ============================================

-- 根部门
INSERT INTO department (id, parent_id, dept_path, dept_name, dept_level, sort_order) VALUES
(1, 0, '/1', '总公司', 1, 1);

-- 角色
INSERT INTO role (id, role_code, role_name, data_scope) VALUES
(1, 'ADMIN',        '系统管理员', 4),
(2, 'DEPT_MANAGER', '部门经理',   3),
(3, 'BUYER',        '采购员',     1),
(4, 'LEGAL',        '法务',       2),
(5, 'FINANCE',      '财务',       4),
(6, 'WAREHOUSE',    '仓库管理员', 2);

-- 权限
INSERT INTO permission (id, parent_id, perm_code, perm_name, perm_type) VALUES
(1,  0, 'purchase',           '采购管理',     1),
(2,  1, 'purchase:order',     '采购订单',     1),
(3,  2, 'purchase:order:create',  '创建订单', 2),
(4,  2, 'purchase:order:approve', '审批订单', 2),
(5,  0, 'contract',           '合同管理',     1),
(6,  5, 'contract:create',    '创建合同',     2),
(7,  5, 'contract:approve',   '审批合同',     2),
(8,  0, 'inventory',          '库存管理',     1),
(9,  8, 'inventory:in',       '入库操作',     2),
(10, 0, 'supplier',           '供应商管理',   1),
(11, 0, 'system',             '系统管理',     1),
(12, 11,'system:user',        '用户管理',     2),
(13, 11,'system:role',        '角色管理',     2);

-- 管理员用户（密码: admin123）
INSERT INTO `user` (id, username, real_name, password_hash, dept_id, position) VALUES
(1, 'admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 1, '管理员');

-- 管理员角色绑定
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);

-- 管理员权限
INSERT INTO role_permission (role_id, perm_id)
SELECT 1, id FROM permission;
