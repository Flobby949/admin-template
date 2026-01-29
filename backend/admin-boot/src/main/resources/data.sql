-- 初始管理员账号
-- 用户名: admin
-- 密码: admin123
-- BCrypt(12) 加密

INSERT INTO sys_user (username, password, real_name, status, login_fail_count, deleted)
VALUES ('admin', '$2a$12$sCtK6cA.yLeGtNuZJj9nWuIlOch1re04K3pc/Vc.CnNg7yEMjmP36', 'Administrator', 1, 0, 0) AS new
ON DUPLICATE KEY UPDATE
    password = new.password,
    real_name = new.real_name,
    status = new.status,
    login_fail_count = new.login_fail_count,
    deleted = new.deleted;

-- 初始角色数据
INSERT INTO sys_role (id, role_name, role_code, data_scope, status, remark, deleted)
VALUES
    (1, '超级管理员', 'admin', 1, 1, '拥有所有权限', 0),
    (2, '普通用户', 'user', 4, 1, '普通用户角色', 0) AS new
ON DUPLICATE KEY UPDATE
    role_name = new.role_name,
    data_scope = new.data_scope,
    status = new.status,
    remark = new.remark,
    deleted = new.deleted;

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id)
SELECT
    (SELECT id FROM sys_user WHERE username = 'admin'),
    1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user_role
    WHERE user_id = (SELECT id FROM sys_user WHERE username = 'admin')
    AND role_id = 1
);

-- 初始菜单数据
-- 一级菜单：系统管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES
    (1, 0, '系统管理', 1, '/system', 'Layout', NULL, 'setting', 1, 1, 1, 0),
    (2, 1, '用户管理', 2, 'user', 'system/user/index', 'system:user:list', 'user', 1, 1, 1, 0),
    (3, 2, '用户新增', 3, NULL, NULL, 'system:user:add', NULL, 1, 1, 1, 0),
    (4, 2, '用户编辑', 3, NULL, NULL, 'system:user:edit', NULL, 2, 1, 1, 0),
    (5, 2, '用户删除', 3, NULL, NULL, 'system:user:delete', NULL, 3, 1, 1, 0),
    (6, 2, '重置密码', 3, NULL, NULL, 'system:user:resetPwd', NULL, 4, 1, 1, 0),
    (7, 1, '角色管理', 2, 'role', 'system/role/index', 'system:role:list', 'peoples', 2, 1, 1, 0),
    (8, 7, '角色新增', 3, NULL, NULL, 'system:role:add', NULL, 1, 1, 1, 0),
    (9, 7, '角色编辑', 3, NULL, NULL, 'system:role:edit', NULL, 2, 1, 1, 0),
    (10, 7, '角色删除', 3, NULL, NULL, 'system:role:delete', NULL, 3, 1, 1, 0),
    (11, 7, '角色查询', 3, NULL, NULL, 'system:role:query', NULL, 4, 1, 1, 0),
    (12, 1, '菜单管理', 2, 'menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3, 1, 1, 0),
    (13, 12, '菜单新增', 3, NULL, NULL, 'system:menu:add', NULL, 1, 1, 1, 0),
    (14, 12, '菜单编辑', 3, NULL, NULL, 'system:menu:edit', NULL, 2, 1, 1, 0),
    (15, 12, '菜单删除', 3, NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1, 0),
    (16, 1, '部门管理', 2, 'dept', 'system/dept/index', 'system:dept:list', 'tree', 4, 1, 1, 0),
    (17, 16, '部门新增', 3, NULL, NULL, 'system:dept:add', NULL, 1, 1, 1, 0),
    (18, 16, '部门编辑', 3, NULL, NULL, 'system:dept:edit', NULL, 2, 1, 1, 0),
    (19, 16, '部门删除', 3, NULL, NULL, 'system:dept:delete', NULL, 3, 1, 1, 0),
    (20, 1, '字典管理', 2, 'dict', 'system/dict/index', 'system:dict:list', 'dict', 5, 1, 1, 0),
    (21, 20, '字典新增', 3, NULL, NULL, 'system:dict:add', NULL, 1, 1, 1, 0),
    (22, 20, '字典编辑', 3, NULL, NULL, 'system:dict:edit', NULL, 2, 1, 1, 0),
    (23, 20, '字典删除', 3, NULL, NULL, 'system:dict:delete', NULL, 3, 1, 1, 0),
    (100, 0, '监控管理', 1, '/monitor', 'Layout', NULL, 'monitor', 2, 1, 1, 0),
    (101, 100, '操作日志', 2, 'operlog', 'monitor/operlog/index', 'monitor:operlog:list', 'form', 1, 1, 1, 0),
    (102, 101, '日志查询', 3, NULL, NULL, 'monitor:operlog:query', NULL, 1, 1, 1, 0),
    (103, 101, '日志删除', 3, NULL, NULL, 'monitor:operlog:delete', NULL, 2, 1, 1, 0),
    (104, 101, '日志导出', 3, NULL, NULL, 'monitor:operlog:export', NULL, 3, 1, 1, 0)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    menu_type = VALUES(menu_type),
    route_path = VALUES(route_path),
    component = VALUES(component),
    permission = VALUES(permission),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    visible = VALUES(visible),
    status = VALUES(status),
    deleted = VALUES(deleted);

-- 超级管理员角色拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 普通用户角色只有查看权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE menu_type IN (1, 2) AND deleted = 0
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 初始字典类型数据
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, deleted)
VALUES
    (1, '用户状态', 'sys_user_status', 1, '用户状态列表', 0),
    (2, '用户性别', 'sys_user_sex', 1, '用户性别列表', 0),
    (3, '系统状态', 'sys_normal_disable', 1, '系统通用状态', 0),
    (4, '是否', 'sys_yes_no', 1, '系统是否列表', 0)
ON DUPLICATE KEY UPDATE
    dict_name = VALUES(dict_name),
    status = VALUES(status),
    remark = VALUES(remark),
    deleted = VALUES(deleted);

-- 初始字典数据
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, is_default, status, remark, deleted)
VALUES
    -- 用户状态
    ('sys_user_status', '正常', '1', 1, 'success', 1, 1, '用户状态正常', 0),
    ('sys_user_status', '停用', '0', 2, 'danger', 0, 1, '用户状态停用', 0),
    -- 用户性别
    ('sys_user_sex', '男', '1', 1, 'primary', 0, 1, '性别男', 0),
    ('sys_user_sex', '女', '2', 2, 'danger', 0, 1, '性别女', 0),
    ('sys_user_sex', '未知', '0', 3, 'info', 1, 1, '性别未知', 0),
    -- 系统状态
    ('sys_normal_disable', '正常', '1', 1, 'success', 1, 1, '正常状态', 0),
    ('sys_normal_disable', '停用', '0', 2, 'danger', 0, 1, '停用状态', 0),
    -- 是否
    ('sys_yes_no', '是', 'Y', 1, 'success', 0, 1, '系统默认是', 0),
    ('sys_yes_no', '否', 'N', 2, 'danger', 0, 1, '系统默认否', 0)
ON DUPLICATE KEY UPDATE
    dict_label = VALUES(dict_label),
    dict_sort = VALUES(dict_sort),
    list_class = VALUES(list_class),
    is_default = VALUES(is_default),
    status = VALUES(status),
    remark = VALUES(remark),
    deleted = VALUES(deleted);
