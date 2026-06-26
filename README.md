# mini-shop

一个小型商城项目，包含用户端、小程序端和管理端。

## 项目结构

- `mini-shop-backend`：后端，Spring Boot + MyBatis + MySQL
- `mini-shop-frontend`：用户端前端，uni-app + Vue 2
- `admin-shop-frontend`：管理端前端，uni-app + Vue 2

## 功能概览

- 商品展示、分类、详情
- 购物车、下单、订单列表
- 地址管理、用户登录
- 管理员登录
- 商品、分类、轮播图、订单、用户管理

## 运行环境

- Java 11+
- MySQL 5.7 / 8.0
- HBuilderX 或 Node.js 环境

## 后端启动

1. 创建数据库：

```sql
CREATE DATABASE mini_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 导入表结构和初始化数据：

- `mini-shop-backend/src/main/resources/sql/schema.sql`
- `mini-shop-backend/src/main/resources/sql/data.sql`

3. 修改数据库配置：

`mini-shop-backend/src/main/resources/application.yml`

4. 启动后端：

```bash
cd mini-shop-backend
.\mvnw spring-boot:run
```

默认地址：`http://localhost:8090`

## 用户端启动

### H5 模式

```bash
cd mini-shop-frontend
npm install
npm run dev:h5
```

默认地址：`http://localhost:8081`

### 微信小程序

```bash
cd mini-shop-frontend
npm install
npm run dev:mp-weixin
```

然后用微信开发者工具导入 `dist/dev/mp-weixin`

## 管理端启动

```bash
cd admin-shop-frontend
npm install
npm run dev:mp-weixin
```

## 目录说明

```text
mini-shop
├── mini-shop-backend
│   ├── src/main/java/com/minishop
│   └── src/main/resources
├── mini-shop-frontend
└── admin-shop-frontend
```

## 说明

当前仓库里有一些额外的调试文件、IDE 配置和构建产物，后续可以再进一步清理。
