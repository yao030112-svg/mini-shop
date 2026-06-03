-- 更新第一行数据为管理员账号
UPDATE user 
SET nickname = 'admin'
WHERE phone = '13800000000' AND role = 1;

-- 或者如果上面那行不存在，创建新的管理员账号
-- INSERT INTO user (openid, nickname, phone, password, role, status, deleted, create_time, update_time)
-- VALUES (
--     'admin_openid_001', 
--     'admin', 
--     '13800000000',
--     '$2a$10$lBdmQ8hoq3hPnghczqW.4.kqPYm7UH4sVohYGTxXdBFi8SOnKq/66', 
--     1, 
--     1, 
--     0, 
--     NOW(), 
--     NOW()
-- );
