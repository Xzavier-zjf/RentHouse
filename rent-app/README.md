# RentHouse 租房平台

RentHouse 是一个面向课程设计、毕业设计原型和答辩演示的租房业务系统。项目在原有用户、房东、管理员三角色基础上，补充了房源详情、条件筛选、收藏、预约看房、租赁申请、房东处理、管理员审核与统计、站内通知等功能，形成较完整的租房业务闭环。

## 项目结构

```text
rent-app/
├── user-service/      # 用户服务，端口 8081
├── house-service/     # 房源与业务服务，端口 8082
├── gateway/           # Spring Cloud Gateway，端口 8080
├── vue-frontend/      # Vue 3 + Vite + Element Plus 前端
├── db-init/           # 数据库初始化与升级脚本
└── docs/              # 需求分析说明书、开发文档
```

## 技术栈

- JDK 17
- Spring Boot 3.2.5
- Spring Cloud Gateway
- MySQL 8
- JdbcTemplate
- JWT
- Vue 3
- Vite
- Element Plus
- Axios

## 核心功能

### 普通用户

- 注册、登录、查看和维护个人资料
- 未登录也可浏览已审核通过的房源
- 按位置、价格、户型、面积、状态等条件搜索房源
- 查看房源详情
- 收藏和取消收藏房源，且防止重复收藏
- 预约看房，且防止同一用户对同一房源重复预约
- 提交租赁申请，且防止同一用户对同一房源重复提交有效申请
- 查看我的收藏、我的预约、我的租赁申请
- 申请成为房东，查看申请状态与拒绝原因
- 查看站内通知

### 房东

- 发布完整房源信息，房源默认进入待审核状态
- 查看自己的房源及审核状态
- 编辑自己的房源
- 下架自己的房源
- 查看和处理自己房源下的预约看房记录
- 查看和处理自己房源下的租赁申请
- 接收预约、申请、审核相关通知

### 管理员

- 分页查看用户列表，用户列表隐藏密码字段
- 审核房东申请，支持通过和拒绝，拒绝时填写原因
- 审核房源，支持状态筛选和拒绝原因
- 查看用户数、房东数、房源数、待审核数、预约数、申请数等统计概览
- 分页查看房源、预约和申请等后台数据

## 数据库

### 新库初始化

确保 MySQL 已启动后，执行：

```bash
mysql -uroot -p123456 < db-init/init.sql
```

### 旧库升级

如果已有旧版数据库，可执行增强版升级脚本：

```bash
mysql -uroot -p123456 rent_house < db-init/upgrade-enhanced.sql
```

默认服务配置使用的数据库账号为 `root`，密码为 `123456`。如本机配置不同，请修改各服务的 `src/main/resources/application.yml`。

## 启动方式

### 启动后端服务

分别打开终端进入对应目录：

```bash
cd user-service
mvn spring-boot:run
```

```bash
cd house-service
mvn spring-boot:run
```

```bash
cd gateway
mvn spring-boot:run
```

### 启动前端

```bash
cd vue-frontend
npm install
npm run dev
```

前端默认访问地址为 `http://localhost:3000`，统一通过网关 `http://localhost:8080` 调用后端接口。

## 默认账号

- 管理员用户名：`admin`
- 管理员密码：`admin123`

## 主要接口

### 用户服务

- `POST /api/user/register`：用户注册
- `POST /api/user/login`：用户登录
- `GET /api/user/profile`：获取个人资料
- `PUT /api/user/profile`：更新个人资料
- `POST /api/user/apply-landlord`：申请成为房东
- `GET /api/user/landlord-application-status`：查看房东申请状态
- `GET /api/user/notifications`：查看站内通知
- `POST /api/user/notifications/{id}/read`：标记通知已读
- `GET /api/user/admin/users`：管理员分页查看用户
- `POST /api/user/admin/approve-landlord`：管理员通过房东申请
- `POST /api/user/admin/reject-landlord`：管理员拒绝房东申请

### 房源服务

- `GET /api/house/search`：公开房源搜索
- `GET /api/house/{id}`：房源详情
- `POST /api/house/upload`：房东发布房源
- `PUT /api/house/{id}`：房东编辑房源
- `POST /api/house/{id}/offline`：房东下架房源
- `GET /api/house/my-houses`：房东查看自己的房源
- `POST /api/house/favorites/{houseId}`：收藏房源
- `DELETE /api/house/favorites/{houseId}`：取消收藏
- `GET /api/house/favorites`：我的收藏
- `POST /api/house/appointments`：预约看房
- `GET /api/house/appointments/my`：我的预约
- `GET /api/house/landlord/appointments`：房东预约管理
- `POST /api/house/rental-applications`：提交租赁申请
- `GET /api/house/rental-applications/my`：我的申请
- `GET /api/house/landlord/rental-applications`：房东申请管理
- `GET /api/house/admin/statistics`：管理员统计概览
- `GET /api/house/admin/houses`：管理员分页查看房源
- `POST /api/house/admin/approve-house`：管理员通过房源
- `POST /api/house/admin/reject-house`：管理员拒绝房源

## 文档

- [需求分析说明书](docs/需求分析说明书.md)
- [开发文档](docs/开发文档.md)

## 构建验证

后端服务可分别执行 Maven 编译：

```bash
mvn -q -DskipTests compile
```

前端执行：

```bash
npm run build
```

前端已通过 Vite `manualChunks` 进行依赖拆分，避免生产构建出现大 chunk 警告。
