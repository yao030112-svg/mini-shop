-- ============================================
-- 创建/更新管理员账号
-- 用户名: admin
-- 密码: admin123
-- ============================================

-- 方案1: 如果已存在nickname为'admin'的管理员，更新密码
UPDATE user 
SET password = '$2a$10$lBdmQ8hoq3hPnghczqW.4.kqPYm7UH4sVohYGTxXdBFi8SOnKq/66',
    role = 1,
    status = 1,
    deleted = 0
WHERE nickname = 'admin' AND role = 1;

-- 方案2: 如果不存在，插入新的管理员账号
INSERT INTO user (openid, nickname, phone, password, role, status, deleted, create_time, update_time)
SELECT 
    'admin_openid_' || UNIX_TIMESTAMP(),
    'admin',
    '13800000000',
    '$2a$10$lBdmQ8hoq3hPnghczqW.4.kqPYm7UH4sVohYGTxXdBFi8SOnKq/66',
    1,
    1,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM user WHERE nickname = 'admin' AND role = 1
);

-- 验证管理员账号是否创建成功
SELECT id, nickname, phone, role, status, deleted, create_time 
FROM user 
WHERE nickname = 'admin' AND role = 1;
