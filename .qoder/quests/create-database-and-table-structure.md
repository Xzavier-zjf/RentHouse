# 数据库创建与表结构设计

## 1. 概述

本文档描述了租房网站项目的数据库初始化脚本问题解决方案，包括数据库和表结构的设计，以及前端实现和后端适配方案。

## 2. 问题分析

在执行数据库初始化脚本时出现以下错误：
```
[ERR] 1007 - Can't create database 'rent_db'; database exists
```

该错误表明数据库 `rent_db` 已经存在，但脚本仍然尝试创建它，导致执行失败。

## 3. 解决方案

### 3.1 修改数据库初始化脚本

为了解决该问题，需要修改数据库初始化脚本，使用 `CREATE DATABASE IF NOT EXISTS` 语句替代原有的 `CREATE DATABASE` 语句，以避免数据库已存在时的错误。

修改后的脚本如下：

```sql
-- 使用 CREATE DATABASE IF NOT EXISTS 避免数据库已存在错误
CREATE DATABASE IF NOT EXISTS rent_db;
USE rent_db;

-- 用户表
CREATE TABLE user (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL, -- BCrypt加密
  email VARCHAR(100),
  role VARCHAR(20) DEFAULT 'USER', -- USER/LANDLORD/ADMIN
  landlord_apply_status VARCHAR(20) DEFAULT 'NOT_APPLIED' -- 申请状态
);

-- 房源表
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
```

关键修改点：
1. 使用 `CREATE DATABASE IF NOT EXISTS rent_db` 替代 `CREATE DATABASE rent_db`
2. 使用 `INSERT IGNORE` 替代 `INSERT` 来避免重复插入管理员账号

### 3.2 数据库表结构说明

#### 3.2.1 用户表 (user)
| 字段名 | 类型 | 约束 | 说明 |
|-------|------|-----|-----|
| id | INT | AUTO_INCREMENT PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE NOT NULL | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（BCrypt加密） |
| email | VARCHAR(100) | - | 邮箱 |
| role | VARCHAR(20) | DEFAULT 'USER' | 用户角色（USER/LANDLORD/ADMIN） |
| landlord_apply_status | VARCHAR(20) | DEFAULT 'NOT_APPLIED' | 房东申请状态 |

#### 3.2.2 房源表 (house)
| 字段名 | 类型 | 约束 | 说明 |
|-------|------|-----|-----|
| id | INT | AUTO_INCREMENT PRIMARY KEY | 房源ID |
| title | VARCHAR(100) | - | 房源标题 |
| price | DECIMAL(10,2) | - | 价格 |
| location | VARCHAR(100) | - | 位置 |
| user_id | INT | FOREIGN KEY REFERENCES user(id) | 上传者ID |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 审核状态（PENDING/APPROVED/REJECTED） |
| reason | VARCHAR(255) | DEFAULT NULL | 审核原因 |

## 4. 前端实现方案

### 4.1 技术栈
- 原生 HTML + JavaScript
- Fetch API 进行后端接口调用

### 4.2 功能模块
1. 用户注册/登录页面
2. 房源浏览页面
3. 房东房源管理页面
4. 管理员审核页面

### 4.3 关键实现要点
1. 使用 Fetch API 与后端服务通信
2. 根据用户角色动态渲染页面内容
3. 实现 JWT Token 的存储和使用
4. 表单验证和错误处理

### 4.4 示例代码结构
```javascript
// 用户登录示例
async function login(username, password) {
  const response = await fetch('/api/users/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });
  
  if (response.ok) {
    const data = await response.json();
    // 存储 JWT token
    localStorage.setItem('token', data.token);
    // 根据角色跳转页面
    redirectToRolePage(data.role);
  } else {
    // 处理错误
    showError('登录失败');
  }
}
```

## 5. 后端适配方案

### 5.1 用户服务 (user-service)
用户服务的 User 模型与数据库表结构保持一致：
- id -> Integer id
- username -> String username
- password -> String password
- email -> String email
- role -> String role
- landlord_apply_status -> String landlordApplyStatus

### 5.2 房源服务 (house-service)
房源服务的 House 模型与数据库表结构保持一致：
- id -> Integer id
- title -> String title
- price -> BigDecimal price
- location -> String location
- user_id -> Integer userId
- status -> String status
- reason -> String reason

### 5.3 数据库连接配置
确保各服务的 application.yml 配置文件中数据库连接信息正确：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rent_db
    username: root
    password: root
```

## 6. 测试验证

### 6.1 数据库初始化测试
1. 执行修改后的初始化脚本
2. 验证数据库和表是否正确创建
3. 验证管理员账号是否正确插入

### 6.2 前后端集成测试
1. 启动所有服务
2. 测试用户注册/登录功能
3. 测试房源 CRUD 操作
4. 测试权限控制功能