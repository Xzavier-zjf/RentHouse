# 极简版租房网站

专为学生作业设计的租房网站，实现用户、房东、管理员三角色体系。

## 项目结构

```
rent-app/
├── user-service/     # 用户服务（端口8081）
├── house-service/    # 房源服务（端口8082）
├── gateway/          # API网关（端口8080）
├── db-init/          # 数据库初始化脚本
├── index.html        # 原生HTML+JS前端测试页面
└── vue-frontend/     # Vue.js前端项目
```

## 技术栈

- JDK 17
- Spring Boot 3.2.5
- MySQL 8.0.33
- MyBatis
- JWT 0.12.5
- 原生HTML+JS (现有前端)
- Vue 3 + Vite + Element Plus (新前端)

## 运行步骤

### 1. 数据库初始化

1. 确保MySQL服务已启动
2. 执行 [db-init/init.sql](db-init/init.sql) 脚本创建数据库和表
3. 确保MySQL用户名和密码为 `root/root`，或修改各服务的配置文件

### 2. 启动后端服务

分别进入各个服务目录，执行Maven命令：

```bash
# 启动用户服务
cd user-service
mvn spring-boot:run

# 启动房源服务
cd house-service
mvn spring-boot:run

# 启动网关服务
cd gateway
mvn spring-boot:run
```

### 3. 访问应用

有两种方式访问前端应用：

#### 原生HTML+JS版本
打开浏览器访问 `index.html` 文件进行测试。

#### Vue.js版本
进入 `vue-frontend` 目录，安装依赖并启动开发服务器：

```bash
cd vue-frontend
npm install
npm run dev
```

然后在浏览器中访问 http://localhost:3000

## 角色功能

### 普通用户
- 注册/登录
- 搜索房源
- 申请成为房东

### 房东
- 上传房源
- 管理房源

### 管理员
- 审核房东申请
- 审核房源
- 查看所有用户和房源

## 默认管理员账号

- 用户名: admin
- 密码: admin123

## API接口

### 用户服务 (8081)
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `POST /api/user/apply-landlord` - 申请成为房东
- `POST /api/user/admin/approve-landlord` - 管理员审核房东申请
- `GET /api/user/admin/users` - 管理员查看所有用户

### 房源服务 (8082)
- `GET /api/house/search` - 搜索房源
- `POST /api/house/upload` - 房东上传房源
- `GET /api/house/my-houses` - 房东查看自己的房源
- `GET /api/house/admin/houses` - 管理员查看所有房源
- `POST /api/house/admin/approve-house` - 管理员审核房源