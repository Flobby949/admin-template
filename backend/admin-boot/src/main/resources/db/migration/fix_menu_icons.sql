-- 修复菜单图标名称，将小写改为首字母大写
-- 以符合 Element Plus Icons 的命名规范

UPDATE sys_menu SET icon = 'Setting' WHERE icon = 'setting';
UPDATE sys_menu SET icon = 'User' WHERE icon = 'user';
UPDATE sys_menu SET icon = 'UserFilled' WHERE icon = 'peoples';
UPDATE sys_menu SET icon = 'Menu' WHERE icon = 'tree-table';
UPDATE sys_menu SET icon = 'OfficeBuilding' WHERE icon = 'tree';
UPDATE sys_menu SET icon = 'Notebook' WHERE icon = 'dict';
UPDATE sys_menu SET icon = 'Monitor' WHERE icon = 'monitor';
UPDATE sys_menu SET icon = 'Document' WHERE icon = 'form';
