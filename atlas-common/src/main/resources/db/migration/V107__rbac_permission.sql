-- ============================================================
-- V107: RBAC 权限管理系统 / RBAC Permission Management System
-- 基于集团多级组织架构的数据访问控制 / Data access control based on multi-level org structure
-- ============================================================

-- 权限表 / Permission Table
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '权限标识(如 message:view) / Permission Code',
    name VARCHAR(128) NOT NULL COMMENT '权限名称 / Permission Name',
    module VARCHAR(64) COMMENT '所属模块 / Module',
    type VARCHAR(32) NOT NULL DEFAULT 'FUNCTION' COMMENT '类型(FUNCTION功能/DATA数据/MENU菜单) / Type',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID / Parent Permission ID',
    sort_order INT DEFAULT 0 COMMENT '排序 / Sort Order',
    status TINYINT DEFAULT 1 COMMENT '状态(1启用0禁用) / Status (1-enabled 0-disabled)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Update Time',
    INDEX idx_module (module),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表 / System Permission';

-- 角色表 / Role Table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码 / Role Code',
    name VARCHAR(128) NOT NULL COMMENT '角色名称 / Role Name',
    data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据权限范围(ALL全部/GROUP集团级/DIVISION事业部级/PLANT工厂级/DEPT部门级/SELF仅本人) / Data Scope(ALL/GROUP/DIVISION/PLANT/DEPT/SELF)',
    description VARCHAR(256) COMMENT '角色描述 / Description',
    status TINYINT DEFAULT 1 COMMENT '状态(1启用0禁用) / Status (1-enabled 0-disabled)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Update Time',
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表 / System Role';

-- 角色权限关联表 / Role-Permission Relation
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID / Role ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID / Permission ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Create Time',
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联 / Role-Permission Mapping';

-- 用户角色关联表 / User-Role Relation
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID / User ID',
    role_id BIGINT NOT NULL COMMENT '角色ID / Role ID',
    org_node_id BIGINT COMMENT '数据隔离节点ID(关联org_structure, NULL表示全局) / Org node ID for data isolation (references org_structure, NULL = global)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Create Time',
    UNIQUE KEY uk_user_role_org (user_id, role_id, org_node_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    INDEX idx_org_node_id (org_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联 / User-Role Mapping';

-- ============================================================
-- 预置权限 / Predefined Permissions
-- ============================================================
INSERT INTO sys_permission (code, name, module, type, sort_order) VALUES
-- 消息模块 / Message Module
('message:view',      '消息查看 / Message View',            'message',    'FUNCTION', 1),
('message:update',    '消息更新 / Message Update',          'message',    'FUNCTION', 2),
('message:manage',    '消息管理 / Message Manage',          'message',    'FUNCTION', 3),

-- 供应商模块 / Supplier Module
('supplier:view',     '供应商查看 / Supplier View',         'supplier',   'FUNCTION', 10),
('supplier:create',   '供应商创建 / Supplier Create',       'supplier',   'FUNCTION', 11),
('supplier:update',   '供应商更新 / Supplier Update',       'supplier',   'FUNCTION', 12),
('supplier:delete',   '供应商删除 / Supplier Delete',       'supplier',   'FUNCTION', 13),

-- 询价模块 / Inquiry Module
('inquiry:view',      '询价查看 / Inquiry View',            'inquiry',    'FUNCTION', 20),
('inquiry:create',    '询价创建 / Inquiry Create',          'inquiry',    'FUNCTION', 21),
('inquiry:manage',    '询价管理 / Inquiry Manage',          'inquiry',    'FUNCTION', 22),

-- 订单模块 / Order Module
('order:view',        '订单查看 / Order View',              'order',      'FUNCTION', 30),
('order:create',      '订单创建 / Order Create',            'order',      'FUNCTION', 31),
('order:update',      '订单更新 / Order Update',            'order',      'FUNCTION', 32),
('order:change',      '订单变更 / Order Change',            'order',      'FUNCTION', 33),

-- 合同模块 / Contract Module
('contract:view',     '合同查看 / Contract View',           'contract',   'FUNCTION', 40),
('contract:create',   '合同创建 / Contract Create',         'contract',   'FUNCTION', 41),
('contract:manage',   '合同管理 / Contract Manage',         'contract',   'FUNCTION', 42),

-- 质量模块 / Quality Module
('quality:view',      '质量查看 / Quality View',            'quality',    'FUNCTION', 50),
('quality:inspect',   '质量检验 / Quality Inspect',         'quality',    'FUNCTION', 51),
('quality:manage',    '质量管理 / Quality Manage',          'quality',    'FUNCTION', 52),

-- 交付模块 / Delivery Module
('delivery:view',     '交付查看 / Delivery View',           'delivery',   'FUNCTION', 60),
('delivery:manage',   '交付管理 / Delivery Manage',         'delivery',   'FUNCTION', 61),

-- 结算模块 / Settlement Module
('settlement:view',   '结算查看 / Settlement View',         'settlement', 'FUNCTION', 70),
('settlement:manage', '结算管理 / Settlement Manage',       'settlement', 'FUNCTION', 71),

-- 系统管理模块 / System Admin Module
('system:perm:view',  '权限查看 / Permission View',         'system',     'FUNCTION', 100),
('system:perm:manage','权限管理 / Permission Manage',       'system',     'FUNCTION', 101),
('system:role:manage','角色管理 / Role Manage',             'system',     'FUNCTION', 102),
('system:user:manage','用户管理 / User Manage',             'system',     'FUNCTION', 103),

-- 数据操作 / Data Operations
('data:export',       '数据导出 / Data Export',             'data',       'DATA',     200),
('data:import',       '数据导入 / Data Import',             'data',       'DATA',     201);

-- ============================================================
-- 预置角色 / Predefined Roles
-- ============================================================
INSERT INTO sys_role (code, name, data_scope, description) VALUES
('SUPER_ADMIN',       '超级管理员 / Super Admin',             'ALL',      '全部数据可见、系统全权限 / Full data visibility, all system permissions'),
('GROUP_MGR',         '集团采购总监 / Group Procurement Director', 'GROUP',  '集团范围数据可见 / Group-level data visibility'),
('DIVISION_MGR',      '事业部经理 / Division Manager',        'DIVISION', '事业部范围数据可见 / Division-level data visibility'),
('PLANT_MGR',         '工厂采购经理 / Plant Procurement Manager', 'PLANT',  '工厂范围数据可见 / Plant-level data visibility'),
('BUYER',             '采购员 / Buyer',                      'DEPT',     '本部门数据可见 / Department-level data visibility'),
('QUALITY_ENGINEER',  '质量工程师 / Quality Engineer',        'PLANT',    '工厂范围数据、质量模块 / Plant-level data, quality module'),
('SUPPLIER',          '供应商 / Supplier',                   'SELF',     '仅本人数据 / Self data only');

-- ============================================================
-- 预置角色-权限关联 / Predefined Role-Permission Mappings
-- ============================================================

-- 超级管理员: 所有权限 / Super Admin: all permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'SUPER_ADMIN'), id FROM sys_permission;

-- 集团采购总监: 所有业务模块查看+数据导出 / Group Procurement Director: all biz view + data export
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'GROUP_MGR'), id FROM sys_permission
WHERE code IN ('message:view', 'message:update', 'message:manage',
               'supplier:view', 'inquiry:view', 'inquiry:create', 'inquiry:manage',
               'order:view', 'order:create', 'order:update', 'order:change',
               'contract:view', 'contract:create', 'contract:manage',
               'quality:view', 'delivery:view', 'delivery:manage',
               'settlement:view', 'settlement:manage', 'data:export');

-- 事业部经理: 事业部级业务权限 / Division Manager: division-level biz permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'DIVISION_MGR'), id FROM sys_permission
WHERE code IN ('message:view', 'message:update',
               'supplier:view', 'inquiry:view', 'inquiry:create', 'inquiry:manage',
               'order:view', 'order:create', 'order:update',
               'contract:view', 'contract:create',
               'quality:view', 'delivery:view', 'settlement:view', 'data:export');

-- 工厂采购经理: 工厂级业务权限 / Plant Procurement Manager: plant-level biz permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'PLANT_MGR'), id FROM sys_permission
WHERE code IN ('message:view', 'message:update',
               'supplier:view', 'inquiry:view', 'inquiry:create', 'inquiry:manage',
               'order:view', 'order:create', 'order:update',
               'contract:view', 'quality:view', 'delivery:view', 'settlement:view', 'data:export');

-- 采购员: 部门级日常操作权限 / Buyer: dept-level daily operation permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'BUYER'), id FROM sys_permission
WHERE code IN ('message:view', 'message:update',
               'supplier:view', 'inquiry:view', 'inquiry:create',
               'order:view', 'order:create',
               'contract:view', 'quality:view', 'delivery:view', 'settlement:view');

-- 质量工程师: 工厂级质量模块权限 / Quality Engineer: plant-level quality permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'QUALITY_ENGINEER'), id FROM sys_permission
WHERE code IN ('message:view', 'message:update',
               'supplier:view', 'order:view', 'contract:view',
               'quality:view', 'quality:inspect', 'quality:manage',
               'delivery:view', 'settlement:view');

-- 供应商: 本人数据、供应商相关权限 / Supplier: self data, supplier-related permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT (SELECT id FROM sys_role WHERE code = 'SUPPLIER'), id FROM sys_permission
WHERE code IN ('message:view', 'supplier:view', 'inquiry:view',
               'order:view', 'contract:view', 'delivery:view', 'settlement:view');
