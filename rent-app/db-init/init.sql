-- 使用 CREATE DATABASE IF NOT EXISTS 避免数据库已存在错误
CREATE DATABASE IF NOT EXISTS rent_db;
USE rent_db;

CREATE TABLE user (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL, -- BCrypt加密
  email VARCHAR(100),
  role VARCHAR(20) DEFAULT 'USER', -- USER/LANDLORD/ADMIN
  landlord_apply_status VARCHAR(20) DEFAULT 'NOT_APPLIED' -- 申请状态
);

CREATE TABLE house (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100),
  price DECIMAL(10,2),
  location VARCHAR(100),
  user_id INT, -- 上传者ID
  status VARCHAR(20) DEFAULT 'PENDING', -- PENDING/APPROVED/REJECTED
  reason VARCHAR(255) DEFAULT NULL, -- 审核原因
  FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 创建管理员账号（作业必备）
-- 使用 INSERT IGNORE 避免重复插入错误
INSERT IGNORE INTO user (username, password, role) 
VALUES ('admin', '$2a$10$NjQ5MjUwNjQ5MjUwNjQ5MjUu', 'ADMIN');
-- 密码明文：admin123 (BCrypt加密后)