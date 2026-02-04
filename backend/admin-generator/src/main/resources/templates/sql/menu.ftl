-- ============================================
-- 菜单权限SQL - ${entity.className}
-- 模块: ${moduleName}
-- 实体: ${entity.comment!''}
-- 生成时间: ${date}
-- ============================================

<#if menu.createDirectory!false>
-- 1. 一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES (${menu.dirId}, 0, '${menu.dirName}', 1, '/${moduleName}', 'Layout', NULL, '${menu.dirIcon!"Folder"}', ${menu.dirSort!10}, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), route_path = VALUES(route_path), icon = VALUES(icon);

</#if>
-- 2. 二级菜单（不带权限，权限由三级按钮控制）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES (${menuId}, ${menu.dirId}, '${entity.comment!""}管理', 2, '${entity.classNameLower}', '${moduleName}/${entity.classNameLower}/index', NULL, '${menu.menuIcon!"List"}', ${menuSort}, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), component = VALUES(component);

-- 3. 三级按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES
    (${btnBaseId}, ${menuId}, '${entity.comment!""}查询', 3, NULL, NULL, '${entity.permissionPrefix}:list', NULL, 1, 1, 1, 0),
    (${btnBaseId} + 1, ${menuId}, '${entity.comment!""}新增', 3, NULL, NULL, '${entity.permissionPrefix}:add', NULL, 2, 1, 1, 0),
    (${btnBaseId} + 2, ${menuId}, '${entity.comment!""}编辑', 3, NULL, NULL, '${entity.permissionPrefix}:edit', NULL, 3, 1, 1, 0),
    (${btnBaseId} + 3, ${menuId}, '${entity.comment!""}删除', 3, NULL, NULL, '${entity.permissionPrefix}:delete', NULL, 4, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), permission = VALUES(permission);

