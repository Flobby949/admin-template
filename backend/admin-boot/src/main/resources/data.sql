-- 初始管理员账号
-- 用户名: admin
-- 密码: admin123
-- BCrypt(12) 加密

INSERT INTO sys_user (username, password, real_name, status, login_fail_count, deleted)
VALUES ('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0JGMTb6ne', 'Administrator', 1, 0, 0)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    status = VALUES(status),
    login_fail_count = VALUES(login_fail_count),
    deleted = VALUES(deleted);
