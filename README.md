<<<<<<< HEAD
# mini-shop
=======
# 迷你商城 - 启动文档

## 项目简介

淘宝类型购物小程序，包含用户端和管理员端。

- **后端**：Spring Boot + MyBatis + MySQL，端口 8090
- **前端**：uni-app + Vue 2，支持微信小程序和 H5

---

## 一、环境准备

| 工具 | 版本要求 | 用途 |
|------|---------|------|
| Java | 8+ (推荐 11 或 17) | 运行后端 |
| MySQL | 5.7+ 或 8.0 | 数据库 |
| HBuilderX | 最新版 | 运行前端 |
| 微信开发者工具 | 最新版 | 预览小程序（可选） |

---

## 二、启动后端

### 2.1 创建数据库

打开 MySQL 客户端（Navicat、DBeaver 或命令行），执行：

```sql
CREATE DATABASE mini_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mini_shop;
```

### 2.2 导入表结构和初始数据

依次执行以下两个 SQL 文件：

```
mini-shop-backend/src/main/resources/sql/schema.sql
mini-shop-backend/src/main/resources/sql/data.sql
```

**方式一：命令行**
```sql
SOURCE D:/小程序开发练习/mini-shop-backend/src/main/resources/sql/schema.sql;
SOURCE D:/小程序开发练习/mini-shop-backend/src/main/resources/sql/data.sql;
```

**方式二：图形工具**
用 Navicat/DBeaver 打开这两个文件，在 mini_shop 数据库中执行。

### 2.3 修改数据库配置（如需要）

编辑 `mini-shop-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mini_shop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root      # 改成你的 MySQL 用户名
    password: root      # 改成你的 MySQL 密码
```

### 2.4 启动后端服务

打开终端（CMD 或 PowerShell），执行：

```bash
cd D:\小程序开发练习\mini-shop-backend
.\mvnw spring-boot:run
```

看到以下日志表示启动成功：

```
Started MiniShopApplication in x.xxx seconds
```

后端运行地址：**http://localhost:8090**

### 2.5 验证后端

浏览器访问：http://localhost:8090/api/banner/list

返回 JSON 数据即表示后端正常。

---

## 三、启动前端

### 方式一：HBuilderX 运行（推荐）

1. 打开 **HBuilderX**
2. 菜单 → **文件** → **导入** → **从本地目录导入**
3. 选择目录：`D:\小程序开发练习\mini-shop-frontend`
4. 在左侧项目管理器中点击项目下的任意文件（如 `App.vue`）
5. 菜单 → **运行**（或按 `Ctrl+R`）→ 选择运行目标：
   - **运行到浏览器 → Chrome**（H5 模式，最方便调试）
   - **运行到小程序模拟器 → 微信开发者工具**（需要先安装微信开发者工具）

> 首次运行可能提示安装 uni-app 编译插件，按提示安装即可。

### 方式二：命令行运行 H5 模式

```bash
cd D:\小程序开发练习\mini-shop-frontend
npm install
npm run dev:h5
```

浏览器访问：**http://localhost:8081**

### 方式三：编译为微信小程序

```bash
cd D:\小程序开发练习\mini-shop-frontend
npm install
npm run dev:mp-weixin
```

然后用微信开发者工具导入 `dist/dev/mp-weixin` 目录。

---

## 四、账号信息

### 管理员账号

| 字段 | 值 |
|------|-----|
| 用户名 | 系统管理员 |
| 密码 | admin123 |

### 测试用户

微信登录为模拟实现，输入任意 code 即可自动创建用户。

---

## 五、项目结构

```
D:\小程序开发练习\
├── mini-shop-backend/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/minishop/
│   │   ├── controller/         # 控制器（API 接口）
│   │   ├── service/            # 业务逻辑
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 请求参数
│   │   ├── vo/                 # 响应对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # JWT 认证
│   │   ├── exception/          # 异常处理
│   │   ├── util/               # 工具类
│   │   └── task/               # 定时任务
│   └── src/main/resources/
│       ├── application.yml     # 应用配置
│       ├── mapper/             # MyBatis XML
│       └── sql/                # 数据库脚本
│
└── mini-shop-frontend/         # 前端 uni-app 项目
    ├── pages/                  # 页面文件
    ├── components/             # 公共组件
    ├── api/                    # API 请求封装
    ├── store/                  # Vuex 状态管理
    ├── static/                 # 静态资源
    ├── App.vue                 # 根组件
    ├── main.js                 # 入口文件
    ├── pages.json              # 页面路由配置
    ├── manifest.json           # 应用配置
    └── uni.scss                # 全局样式变量
```

---

## 六、API 接口一览

### 公开接口（无需登录）

| 接口 | 说明 |
|------|------|
| POST /api/auth/wx-login | 微信登录 |
| POST /api/auth/admin-login | 管理员登录 |
| GET /api/banner/list | 轮播图列表 |
| GET /api/product/hot | 热门商品 |
| GET /api/product/list | 商品列表 |
| GET /api/product/{id} | 商品详情 |
| GET /api/category/list | 分类列表 |
| GET /api/product/category/{id} | 分类商品 |

### 用户接口（需登录）

| 接口 | 说明 |
|------|------|
| GET /api/cart/list | 购物车列表 |
| POST /api/cart/add | 加入购物车 |
| PUT /api/cart/update | 修改数量 |
| DELETE /api/cart/delete/{id} | 删除条目 |
| POST /api/order/create | 创建订单 |
| POST /api/order/pay/{id} | 模拟支付 |
| GET /api/order/list | 订单列表 |
| GET /api/address/list | 地址列表 |

### 管理员接口（需管理员登录）

| 接口 | 说明 |
|------|------|
| GET /api/admin/product/list | 商品管理 |
| POST /api/admin/product/add | 添加商品 |
| PUT /api/admin/order/ship/{id} | 发货 |
| PUT /api/admin/order/refund/{id} | 退款 |
| PUT /api/admin/user/disable/{id} | 禁用用户 |

---

## 七、常见问题

### Q: 后端启动报 "Port 8080 is already in use"
A: 8080 端口被占用。当前已改为 8090 端口。如果 8090 也被占用，修改 `application.yml` 中的 `server.port`，同时修改前端 `api/request.js` 中的 `BASE_URL`。

### Q: 后端启动报数据库连接失败
A: 检查 MySQL 是否启动，数据库 `mini_shop` 是否已创建，`application.yml` 中的用户名密码是否正确。

### Q: HBuilderX 找不到"运行"菜单
A: 确保左侧项目管理器中已选中项目文件，然后按 `Ctrl+R` 或右键项目文件夹选择运行。

### Q: 前端页面空白或接口报错
A: 确保后端已启动且端口与前端 `api/request.js` 中的 `BASE_URL` 一致。

---

## 八、技术栈

- **后端**：Spring Boot 2.7 + MyBatis + MySQL + JWT + jqwik（属性测试）
- **前端**：uni-app + Vue 2 + Vuex
- **数据库**：MySQL 8.0
- **认证**：JWT Token（7天有效期）
>>>>>>> 68e766f (首次上传 mini-shop)
