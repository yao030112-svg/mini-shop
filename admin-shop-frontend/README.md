# 商城管理后台

这是一个基于 uni-app 开发的商城管理后台小程序，用于管理商品、分类、订单和用户。

## 项目结构

```
admin-shop-frontend/
├── api/                    # API 接口
│   ├── admin.js           # 管理端 API
│   └── request.js         # 请求封装
├── pages/                  # 页面
│   └── admin/             # 管理端页面
│       ├── index.vue      # 管理端首页（数据统计）
│       ├── login.vue      # 管理员登录
│       ├── product/       # 商品管理
│       ├── category/      # 分类管理
│       ├── order/         # 订单管理
│       └── user/          # 用户管理
├── components/             # 公共组件
├── static/                 # 静态资源
├── store/                  # Vuex 状态管理
├── pages.json             # 页面配置
├── manifest.json          # 应用配置
└── package.json           # 项目依赖
```

## 功能模块

### 1. 管理端首页 (pages/admin/index.vue)
- **数据统计卡片**：总订单数、商品总数、用户总数、总收入
- **今日数据**：今日订单、今日收入、新增用户
- **快捷操作**：快速跳转到各个管理模块
- **最近订单**：展示最近的5条订单

### 2. 商品管理 (pages/admin/product/list.vue)
- 商品列表展示
- 商品搜索
- 商品上下架
- 商品删除

### 3. 分类管理 (pages/admin/category/list.vue)
- 分类列表展示
- 添加分类
- 编辑分类
- 删除分类
- 分类排序

### 4. 订单管理 (pages/admin/order/list.vue)
- 订单列表展示
- 按状态筛选（待支付、已支付、已发货、已完成、已退款）
- 订单发货
- 订单退款

### 5. 用户管理 (pages/admin/user/list.vue)
- 用户列表展示
- 用户搜索
- 禁用/启用用户

## 技术栈

- **框架**: uni-app (Vue 2)
- **状态管理**: Vuex
- **UI**: 原生 uni-app 组件
- **网络请求**: 自定义 request 封装

## 开发指南

### 安装依赖

```bash
npm install
```

### 运行项目

#### H5 开发
```bash
npm run dev:h5
```

#### 微信小程序开发
```bash
npm run dev:mp-weixin
```

### 构建发布

#### H5 构建
```bash
npm run build:h5
```

#### 微信小程序构建
```bash
npm run build:mp-weixin
```

## 后端 API 要求

管理端需要以下后端 API 支持：

### 统计接口
- `GET /api/admin/stats` - 获取管理端统计数据

### 商品管理
- `GET /api/admin/product/list` - 获取商品列表
- `POST /api/admin/product/add` - 添加商品
- `PUT /api/admin/product/update` - 更新商品
- `DELETE /api/admin/product/delete/:id` - 删除商品
- `PUT /api/admin/product/status/:id` - 上下架商品

### 分类管理
- `GET /api/admin/category/list` - 获取分类列表
- `POST /api/admin/category/add` - 添加分类
- `PUT /api/admin/category/update` - 更新分类
- `DELETE /api/admin/category/delete/:id` - 删除分类

### 订单管理
- `GET /api/admin/order/list` - 获取订单列表
- `PUT /api/admin/order/ship/:id` - 发货
- `PUT /api/admin/order/refund/:id` - 退款

### 用户管理
- `GET /api/admin/user/list` - 获取用户列表
- `PUT /api/admin/user/disable/:id` - 禁用用户
- `PUT /api/admin/user/enable/:id` - 启用用户

### 认证接口
- `POST /api/auth/admin/login` - 管理员登录

## 注意事项

1. **独立项目**: 这是一个独立的管理端项目，与客户端 (mini-shop-frontend) 分开部署
2. **权限控制**: 所有管理端接口都需要管理员权限（role = 1）
3. **API 地址**: 需要在 `api/request.js` 中配置后端 API 基础地址
4. **底部导航**: 管理端使用自定义底部导航，不使用 uni-app 的 tabBar

## 部署说明

1. 修改 `api/request.js` 中的 `BASE_URL` 为实际的后端地址
2. 根据目标平台执行相应的构建命令
3. 将构建产物部署到对应平台

## 许可证

MIT
