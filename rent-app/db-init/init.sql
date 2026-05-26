-- 使用 CREATE DATABASE IF NOT EXISTS 避免数据库已存在错误
CREATE DATABASE IF NOT EXISTS rent_db;
USE rent_db;

CREATE TABLE user (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL, -- BCrypt加密
  email VARCHAR(100),
  avatar_file_id VARCHAR(64),
  role VARCHAR(20) DEFAULT 'USER', -- USER/LANDLORD/ADMIN
  landlord_apply_status VARCHAR(20) DEFAULT 'NOT_APPLIED', -- NOT_APPLIED/PENDING/APPROVED/REJECTED
  landlord_apply_reason VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE house (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  location VARCHAR(100) NOT NULL,
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  description TEXT,
  layout VARCHAR(50),
  area DECIMAL(10,2),
  floor VARCHAR(50),
  orientation VARCHAR(50),
  image_url VARCHAR(500),
  contact_name VARCHAR(50),
  contact_phone VARCHAR(30),
  user_id INT, -- 上传者ID
  status VARCHAR(20) DEFAULT 'PENDING', -- PENDING/APPROVED/REJECTED
  rent_status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE/RESERVED/RENTED
  reason VARCHAR(255) DEFAULT NULL, -- 审核原因
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE favorite (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite_user_house (user_id, house_id),
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE appointment (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  landlord_id INT NOT NULL,
  appointment_time DATETIME NOT NULL,
  contact_name VARCHAR(50),
  contact_phone VARCHAR(30),
  message VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PENDING', -- PENDING/CONFIRMED/CANCELLED/FINISHED
  reply VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE rental_application (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  landlord_id INT NOT NULL,
  applicant_name VARCHAR(50),
  applicant_phone VARCHAR(30),
  move_in_date DATE,
  lease_months INT,
  message VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PENDING', -- PENDING/APPROVED/REJECTED/CANCELLED
  reply VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE notification (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(500),
  type VARCHAR(50),
  read_status VARCHAR(20) DEFAULT 'UNREAD',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建或修正管理员账号（作业必备）
INSERT INTO user (username, password, role, landlord_apply_status)
VALUES ('admin', '$2a$10$Rv7By/2.URtxGfldzO79ve9oavndFfS2bM5Zi8A1.EPlMHwHs3ZAu', 'ADMIN', 'APPROVED')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = 'ADMIN',
  landlord_apply_status = 'APPROVED';
-- 密码明文：admin123 (BCrypt加密后)
