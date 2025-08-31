-- 修复数据库中所有用户的角色字段
UPDATE user SET role = 'USER' WHERE role IS NULL;
UPDATE user SET landlord_apply_status = 'NOT_APPLIED' WHERE landlord_apply_status IS NULL;

-- 查看修复结果
SELECT id, username, role, landlord_apply_status FROM user;
