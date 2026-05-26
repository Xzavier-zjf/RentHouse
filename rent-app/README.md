# RentHouse 租房平台

RentHouse 是一个面向课程设计、毕业设计原型和答辩演示的租房业务系统。项目包含普通用户、房东、管理员三类角色，覆盖房源发布、审核、搜索、收藏、预约看房、租赁申请、通知、头像、房源图片和定位等核心流程。

## 项目结构

```text
user-service/      # 用户服务，端口 8081
house-service/     # 房源与业务服务，端口 8082
gateway/           # Spring Cloud Gateway，端口 8080
vue-frontend/      # Vue 3 + Vite + Element Plus 前端，端口 3000
db-init/           # MySQL 初始化与升级脚本
docs/              # 需求分析说明书、开发文档
```

## 技术栈

- JDK 17
- Spring Boot 3.2.5
- Spring Cloud Gateway
- MySQL 8 + JdbcTemplate
- MongoDB GridFS
- JWT
- Vue 3 + Vite
- Element Plus
- Axios

## 核心功能

### 普通用户

- 注册、登录、查看个人信息
- 修改密码、上传/替换头像
- 浏览已审核通过的房源
- 按位置、月租金、户型、面积等条件搜索房源
- 查看房源详情、房源图片轮播和地图定位
- 收藏和取消收藏房源
- 预约看房、提交租赁申请
- 查看我的收藏、我的预约、我的租赁申请
- 申请成为房东，查看申请状态与拒绝原因
- 查看站内通知

### 房东

- 发布房源，房源默认进入待审核状态
- 上传多张房源图片，支持封面设置、排序和删除
- 使用常用选项快速填写位置、户型、楼层、朝向，也可手动输入
- 使用浏览器定位保存房源经纬度
- 查看、编辑、下架自己的房源
- 查看和处理预约看房记录
- 查看和处理租赁申请
- 接收预约、申请、审核相关通知

### 管理员

- 分页查看用户列表，用户列表隐藏密码字段
- 审核房东申请，支持通过和拒绝，拒绝时填写原因
- 审核房源，支持状态筛选和拒绝原因
- 查看房源封面、房源状态和统计概览
- 查看用户数、房东数、房源数、待审核数、预约数、申请数等数据

## 数据库与图片存储

项目采用双数据库模式：

- MySQL `rent_db`：存储用户、房源、收藏、预约、租赁申请、通知等业务数据。
- MongoDB `rent_image_db`：通过 GridFS 存储头像和房源图片。

默认连接配置：

```text
MySQL:   localhost:3306/rent_db
MongoDB: mongodb://localhost:27017/rent_image_db
```

默认 MySQL 账号密码：

```text
username: root
password: 123456
```

如本机配置不同，请修改：

```text
user-service/src/main/resources/application.yml
house-service/src/main/resources/application.yml
```

### 新库初始化

在 `rent-app` 目录执行：

```bash
mysql -uroot -p123456 < db-init/init.sql
```

### 旧库升级

如果已有旧版 `rent_db`，在 MySQL 客户端中执行：

```sql
USE rent_db;
source D:/IdeaProjects/RentHouse/rent-app/db-init/upgrade-enhanced.sql;
```

## 启动方式

### 一键启动

在当前目录运行：

```bash
start.bat
```

脚本会依次启动：

- `user-service`: `http://localhost:8081`
- `house-service`: `http://localhost:8082`
- `gateway`: `http://localhost:8080`
- `vue-frontend`: `http://localhost:3000`

### 手动启动

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

```bash
cd vue-frontend
npm install
npm run dev
```

前端访问地址：

```text
http://localhost:3000
```

后端统一网关：

```text
http://localhost:8080
```

## 默认账号

管理员账号：

```text
username: admin
password: admin123
```

测试房东账号以本地数据库为准。可以用管理员审核普通用户成为房东，或查看当前数据库：

```bash
mysql -uroot -p123456 rent_db -e "SELECT id, username, role, landlord_apply_status FROM user;"
```

## 主要接口

### 用户服务

- `POST /api/user/register`：用户注册
- `POST /api/user/login`：用户登录
- `GET /api/user/profile`：获取个人资料
- `PUT /api/user/profile`：更新个人资料
- `PUT /api/user/password`：修改密码
- `POST /api/user/avatar`：上传/替换头像
- `GET /api/user/avatar/{fileId}`：读取头像图片
- `POST /api/user/apply-landlord`：申请成为房东
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
- `POST /api/house/{id}/images`：上传房源图片
- `GET /api/house/{id}/images`：查看房源图片列表
- `PUT /api/house/{id}/images/{fileId}`：设置封面或排序
- `DELETE /api/house/{id}/images/{fileId}`：删除房源图片
- `GET /api/house/images/{fileId}`：读取房源图片
- `POST /api/house/{id}/favorite`：收藏房源
- `DELETE /api/house/{id}/favorite`：取消收藏
- `GET /api/house/favorites`：我的收藏
- `POST /api/house/appointments`：预约看房
- `GET /api/house/appointments/my`：我的预约
- `GET /api/house/appointments/landlord`：房东预约管理
- `POST /api/house/appointments/{id}/status`：房东处理预约
- `POST /api/house/appointments/{id}/cancel`：用户取消预约
- `POST /api/house/rental-applications`：提交租赁申请
- `GET /api/house/rental-applications/my`：我的申请
- `GET /api/house/rental-applications/landlord`：房东申请管理
- `POST /api/house/rental-applications/{id}/status`：房东处理租赁申请
- `POST /api/house/rental-applications/{id}/cancel`：用户取消租赁申请
- `GET /api/house/admin/statistics`：管理员统计概览
- `GET /api/house/admin/houses`：管理员分页查看房源
- `POST /api/house/admin/approve-house`：管理员审核房源

## 构建验证

后端服务：

```bash
cd user-service
mvn -q -DskipTests compile
```

```bash
cd house-service
mvn -q -DskipTests compile
```

```bash
cd gateway
mvn -q -DskipTests compile
```

前端：

```bash
cd vue-frontend
npm run build
```

## 注意事项

- 请使用 `http://localhost:3000` 访问 Vue 前端，不要打开 `index.html`，旧静态页面不会展示最新功能。
- 浏览器定位功能需要授权；如果拒绝授权，只会影响自动定位，不影响手动填写位置。
- MongoDB 不需要手动创建 `rent_image_db`，首次上传头像或房源图片时会自动创建 GridFS 集合。
- 前端通过网关 `http://localhost:8080` 调用接口，图片也通过后端接口读取，不直接访问 MongoDB。

## 文档

- [需求分析说明书](docs/需求分析说明书.md)
- [开发文档](docs/开发文档.md)
