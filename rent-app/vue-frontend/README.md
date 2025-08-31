# 租房系统Vue.js前端

## 项目介绍

这是一个使用Vue 3 + Vite构建的租房系统前端应用，包含用户注册登录、房源搜索、房东功能和管理员功能等核心模块。

## 技术栈

- Vue 3
- Vite
- Vue Router
- Vuex
- Axios
- Element Plus

## 项目结构

```
src/
├── components/
│   ├── auth/
│   │   ├── Register.vue (注册组件)
│   │   └── Login.vue (登录组件)
│   ├── user/
│   │   ├── HouseSearch.vue (房源搜索组件)
│   │   └── ApplyLandlord.vue (申请房东组件)
│   ├── landlord/
│   │   ├── HouseUpload.vue (房源上传组件)
│   │   └── MyHouses.vue (我的房源组件)
│   └── admin/
│       ├── UserApproval.vue (用户审核组件)
│       ├── HouseApproval.vue (房源审核组件)
│       └── AllHouses.vue (所有房源组件)
├── views/
│   ├── Home.vue (首页)
│   ├── UserDashboard.vue (用户仪表板)
│   ├── LandlordDashboard.vue (房东仪表板)
│   └── AdminDashboard.vue (管理员仪表板)
├── router/
│   └── index.js (路由配置)
├── store/
│   └── index.js (状态管理)
├── utils/
│   └── api.js (API封装)
└── App.vue (根组件)
```

## 安装依赖

```bash
npm install
```

## 开发环境运行

```bash
npm run dev
```

默认访问地址: http://localhost:3000

## 构建生产版本

```bash
npm run build
```

## 功能说明

### 用户功能
- 用户注册/登录
- 申请成为房东
- 搜索房源

### 房东功能
- 上传房源
- 管理房源

### 管理员功能
- 审核房东申请
- 审核房源
- 查看所有房源

## API接口

后端服务通过API网关(端口8080)提供RESTful API接口，具体接口如下：

### 用户服务
- POST /api/user/register - 用户注册
- POST /api/user/login - 用户登录
- POST /api/user/apply-landlord - 申请成为房东
- POST /api/user/admin/approve-landlord - 管理员审核房东申请
- GET /api/user/admin/users - 管理员获取所有用户

### 房源服务
- GET /api/house/search - 搜索房源
- POST /api/house/upload - 上传房源
- GET /api/house/my-houses - 获取我的房源
- GET /api/house/admin/houses - 管理员获取所有房源
- POST /api/house/admin/approve-house - 管理员审核房源