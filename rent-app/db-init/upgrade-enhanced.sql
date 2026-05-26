-- 从旧版极简库升级到课程增强版。
-- 脚本可重复执行：已存在的列、表和默认管理员会被跳过或修正。

USE rent_db;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_column_if_missing(
  IN table_name_value VARCHAR(64),
  IN column_name_value VARCHAR(64),
  IN column_definition_value TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name_value
      AND COLUMN_NAME = column_name_value
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD COLUMN `', column_name_value, '` ', column_definition_value);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('user', 'created_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('user', 'updated_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('user', 'landlord_apply_reason', 'VARCHAR(255)');
CALL add_column_if_missing('user', 'avatar_file_id', 'VARCHAR(64)');

CALL add_column_if_missing('house', 'description', 'TEXT');
CALL add_column_if_missing('house', 'latitude', 'DECIMAL(10,7)');
CALL add_column_if_missing('house', 'longitude', 'DECIMAL(10,7)');
CALL add_column_if_missing('house', 'layout', 'VARCHAR(50)');
CALL add_column_if_missing('house', 'area', 'DECIMAL(10,2)');
CALL add_column_if_missing('house', 'floor', 'VARCHAR(50)');
CALL add_column_if_missing('house', 'orientation', 'VARCHAR(50)');
CALL add_column_if_missing('house', 'image_url', 'VARCHAR(500)');
CALL add_column_if_missing('house', 'contact_name', 'VARCHAR(50)');
CALL add_column_if_missing('house', 'contact_phone', 'VARCHAR(30)');
CALL add_column_if_missing('house', 'rent_status', 'VARCHAR(20) DEFAULT ''AVAILABLE''');
CALL add_column_if_missing('house', 'created_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('house', 'updated_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');

DROP PROCEDURE IF EXISTS add_column_if_missing;

CREATE TABLE IF NOT EXISTS favorite (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite_user_house (user_id, house_id),
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointment (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  landlord_id INT NOT NULL,
  appointment_time DATETIME NOT NULL,
  contact_name VARCHAR(50),
  contact_phone VARCHAR(30),
  message VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PENDING',
  reply VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rental_application (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  house_id INT NOT NULL,
  landlord_id INT NOT NULL,
  applicant_name VARCHAR(50),
  applicant_phone VARCHAR(30),
  move_in_date DATE,
  lease_months INT,
  message VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PENDING',
  reply VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (house_id) REFERENCES house(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(500),
  type VARCHAR(50),
  read_status VARCHAR(20) DEFAULT 'UNREAD',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

UPDATE house SET rent_status = 'AVAILABLE' WHERE rent_status IS NULL;

INSERT INTO user (username, password, role, landlord_apply_status)
VALUES ('admin', '$2a$10$Rv7By/2.URtxGfldzO79ve9oavndFfS2bM5Zi8A1.EPlMHwHs3ZAu', 'ADMIN', 'APPROVED')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = 'ADMIN',
  landlord_apply_status = 'APPROVED';
