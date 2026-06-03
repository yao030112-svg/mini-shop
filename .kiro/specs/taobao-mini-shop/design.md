# 设计文档

## 架构概述

本系统采用前后端分离架构，分为三层：

1. **前端层**：uni-app + Vue 开发的微信小程序，负责用户交互和页面渲染
2. **后端层**：Spring Boot + MyBatis 提供 RESTful API 服务
3. **数据层**：MySQL 数据库存储业务数据

```
┌─────────────────────────────────────────────────┐
│                  微信小程序 (uni-app)              │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │ 首页 │ │ 分类 │ │ 购物车│ │ 订单 │ │ 我的 │  │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘  │
└─────────────────────┬───────────────────────────┘
                      │ HTTP/HTTPS (RESTful API)
┌─────────────────────┴───────────────────────────┐
│              Spring Boot 后端服务                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │
│  │Controller│ │ Service  │ │   Security       │ │
│  │   层     │ │   层     │ │ (JWT Filter)     │ │
│  └──────────┘ └──────────┘ └──────────────────┘ │
│  ┌──────────────────────────────────────────┐   │
│  │           MyBatis Mapper 层               │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────┐
│                   MySQL 数据库                     │
└─────────────────────────────────────────────────┘
```

## 目录结构

```
d:\小程序开发练习\
├── mini-shop-frontend/          # 前端 uni-app 项目
│   ├── pages/
│   │   ├── index/               # 首页
│   │   ├── category/            # 分类页
│   │   ├── product/             # 商品详情页
│   │   ├── cart/                # 购物车页
│   │   ├── order/               # 订单相关页面
│   │   └── user/                # 个人中心
│   ├── components/              # 公共组件
│   ├── api/                     # API 请求封装
│   ├── store/                   # Vuex 状态管理
│   ├── utils/                   # 工具函数
│   └── static/                  # 静态资源
│
└── mini-shop-backend/           # 后端 Spring Boot 项目
    └── src/main/java/com/minishop/
        ├── controller/          # 控制器层
        ├── service/             # 业务逻辑层
        ├── mapper/              # MyBatis Mapper
        ├── entity/              # 实体类
        ├── dto/                 # 数据传输对象
        ├── vo/                  # 视图对象
        ├── config/              # 配置类
        ├── security/            # 安全认证
        ├── exception/           # 异常处理
        └── util/                # 工具类
```

## 数据模型设计

### E-R 关系

```
User 1──N Address
User 1──N Cart
User 1──N Order
Order 1──N OrderItem
Product N──1 Category
Product 1──N ProductSpec (规格维度)
ProductSpec 1──N SpecValue (规格值)
Product 1──N Sku
Sku N──N SpecValue (通过 sku_spec_value 关联)
Cart N──1 Sku
OrderItem N──1 Sku
```

### 数据库表结构

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) UNIQUE NOT NULL COMMENT '微信openid',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar_url VARCHAR(256) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    role TINYINT DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
    password VARCHAR(128) COMMENT '管理员密码(加密)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 商品分类表
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon VARCHAR(256) COMMENT '分类图标URL',
    sort_order INT DEFAULT 0 COMMENT '排序权重，越大越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 商品表
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    main_image VARCHAR(256) COMMENT '主图URL',
    images TEXT COMMENT '商品图片列表(JSON数组)',
    min_price DECIMAL(10,2) COMMENT '最低价格(冗余，取所有SKU最低价)',
    status TINYINT DEFAULT 1 COMMENT '状态：1上架 0下架',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门推荐',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 商品规格维度表
CREATE TABLE product_spec (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(32) NOT NULL COMMENT '规格名称，如颜色、尺码',
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- 规格值表
CREATE TABLE spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spec_id BIGINT NOT NULL COMMENT '规格维度ID',
    value VARCHAR(64) NOT NULL COMMENT '规格值，如红色、XL',
    sort_order INT DEFAULT 0,
    FOREIGN KEY (spec_id) REFERENCES product_spec(id)
);

-- SKU表
CREATE TABLE sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    price DECIMAL(10,2) NOT NULL COMMENT 'SKU价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    spec_desc VARCHAR(256) COMMENT '规格描述，如"红色,XL"',
    image VARCHAR(256) COMMENT 'SKU图片',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- SKU与规格值关联表
CREATE TABLE sku_spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_id BIGINT NOT NULL,
    spec_value_id BIGINT NOT NULL,
    FOREIGN KEY (sku_id) REFERENCES sku(id),
    FOREIGN KEY (spec_value_id) REFERENCES spec_value(id),
    UNIQUE KEY uk_sku_spec (sku_id, spec_value_id)
);

-- 购物车表
CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (sku_id) REFERENCES sku(id),
    UNIQUE KEY uk_user_sku (user_id, sku_id)
);

-- 收货地址表
CREATE TABLE address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(32) NOT NULL COMMENT '省',
    city VARCHAR(32) NOT NULL COMMENT '市',
    district VARCHAR(32) NOT NULL COMMENT '区',
    detail VARCHAR(256) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 订单表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已发货 3已完成 4已取消 5已退款',
    address_snapshot TEXT NOT NULL COMMENT '收货地址快照(JSON)',
    payment_time DATETIME COMMENT '支付时间',
    shipping_time DATETIME COMMENT '发货时间',
    complete_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 订单详情表
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    sku_spec_desc VARCHAR(256) COMMENT '规格描述快照',
    price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    quantity INT NOT NULL COMMENT '购买数量',
    image VARCHAR(256) COMMENT '商品图片快照',
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 轮播图表
CREATE TABLE banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_url VARCHAR(256) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(256) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 接口设计

### 认证相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/wx-login | 微信登录 | 否 |
| POST | /api/auth/admin-login | 管理员登录 | 否 |

### 首页与商品

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/banner/list | 获取轮播图列表 | 否 |
| GET | /api/product/hot | 获取热门商品 | 否 |
| GET | /api/product/list | 商品列表(分页) | 否 |
| GET | /api/product/{id} | 商品详情 | 否 |
| GET | /api/category/list | 获取分类列表 | 否 |
| GET | /api/product/category/{categoryId} | 按分类查询商品 | 否 |

### 购物车

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/cart/list | 获取购物车列表 | 是 |
| POST | /api/cart/add | 添加到购物车 | 是 |
| PUT | /api/cart/update | 更新购物车数量 | 是 |
| DELETE | /api/cart/delete/{id} | 删除购物车条目 | 是 |
| PUT | /api/cart/select-all | 全选/取消全选 | 是 |

### 订单

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/order/create | 创建订单 | 是 |
| GET | /api/order/list | 订单列表 | 是 |
| GET | /api/order/{id} | 订单详情 | 是 |
| POST | /api/order/pay/{id} | 模拟支付 | 是 |
| POST | /api/order/cancel/{id} | 取消订单 | 是 |

### 用户与地址

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/user/info | 获取用户信息 | 是 |
| GET | /api/address/list | 地址列表 | 是 |
| POST | /api/address/add | 新增地址 | 是 |
| PUT | /api/address/update | 更新地址 | 是 |
| DELETE | /api/address/delete/{id} | 删除地址 | 是 |
| PUT | /api/address/default/{id} | 设为默认地址 | 是 |

### 管理员接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin/product/list | 商品列表 | 管理员 |
| POST | /api/admin/product/add | 添加商品 | 管理员 |
| PUT | /api/admin/product/update | 更新商品 | 管理员 |
| DELETE | /api/admin/product/delete/{id} | 删除商品 | 管理员 |
| PUT | /api/admin/product/status/{id} | 上下架 | 管理员 |
| GET | /api/admin/category/list | 分类列表 | 管理员 |
| POST | /api/admin/category/add | 添加分类 | 管理员 |
| PUT | /api/admin/category/update | 更新分类 | 管理员 |
| DELETE | /api/admin/category/delete/{id} | 删除分类 | 管理员 |
| GET | /api/admin/order/list | 订单列表 | 管理员 |
| PUT | /api/admin/order/ship/{id} | 发货 | 管理员 |
| PUT | /api/admin/order/refund/{id} | 退款 | 管理员 |
| GET | /api/admin/user/list | 用户列表 | 管理员 |
| PUT | /api/admin/user/disable/{id} | 禁用用户 | 管理员 |

## 核心组件设计

### 1. JWT 认证模块

```java
// JwtUtil.java
public class JwtUtil {
    private static final String SECRET = "mini-shop-secret-key";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天

    public static String generateToken(Long userId, Integer role) { ... }
    public static Claims parseToken(String token) { ... }
    public static boolean isTokenExpired(String token) { ... }
}

// JwtInterceptor.java - 拦截器验证Token
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        // 从Header获取Token，验证有效性，设置用户上下文
    }
}
```

### 2. 购物车价格计算服务

```java
// CartService.java
public class CartService {
    /**
     * 计算选中商品总价
     * 总价 = Σ(每个选中条目的 SKU单价 × 数量)
     */
    public BigDecimal calculateTotalPrice(Long userId) {
        List<Cart> selectedItems = cartMapper.getSelectedByUserId(userId);
        return selectedItems.stream()
            .map(item -> item.getSku().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### 3. 订单创建服务（含库存扣减）

```java
// OrderService.java
@Transactional
public Order createOrder(Long userId, Long addressId, List<Long> cartIds) {
    // 1. 验证收货地址存在
    // 2. 获取购物车选中条目
    // 3. 检查每个SKU库存是否充足
    // 4. 扣减库存（使用乐观锁：UPDATE sku SET stock = stock - #{qty} WHERE id = #{id} AND stock >= #{qty}）
    // 5. 创建订单和订单项
    // 6. 生成唯一订单编号
    // 7. 清除已下单的购物车条目
    // 8. 返回订单信息
}
```

### 4. 订单超时取消（定时任务）

```java
// OrderTimeoutTask.java
@Scheduled(fixedRate = 60000) // 每分钟执行一次
public void cancelTimeoutOrders() {
    // 查询超过30分钟未支付的订单
    // 更新状态为已取消
    // 恢复对应SKU库存
}
```

### 5. SKU 规格选择器逻辑（前端）

```javascript
// sku-selector.js
/**
 * 根据已选规格值，计算其他规格值的可选状态
 * 遍历所有SKU，找出包含已选规格值且库存>0的SKU集合
 * 未选维度的可选值 = 上述SKU集合中该维度出现的所有值
 */
function computeSelectableValues(specs, skuList, selectedValues) {
    // 返回每个规格维度下各值的可选状态
}
```

## 错误处理

### 统一响应格式

```java
public class Result<T> {
    private int code;       // 状态码：200成功，401未认证，403禁止，500错误
    private String message; // 提示信息
    private T data;         // 数据

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> error(int code, String message) { ... }
}
```

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorized(UnauthorizedException e) {
        return Result.error(401, "未登录或Token已过期");
    }
}
```

### 业务异常码

| 错误码 | 说明 |
|--------|------|
| 401 | 未认证/Token过期 |
| 403 | 权限不足 |
| 1001 | 库存不足 |
| 1002 | 商品已下架 |
| 1003 | 地址数量超限 |
| 1004 | 分类下存在商品，无法删除 |
| 1005 | 用户已被禁用 |

## 前端状态管理

```javascript
// store/index.js (Vuex)
const store = {
    modules: {
        user: {
            state: { token, userInfo },
            actions: { wxLogin, getUserInfo, logout }
        },
        cart: {
            state: { cartList, totalPrice, selectedCount },
            actions: { getCartList, addToCart, updateQuantity, deleteItem, toggleSelectAll }
        }
    }
}
```

## 前端请求封装

```javascript
// api/request.js
const request = (options) => {
    const token = uni.getStorageSync('token');
    return uni.request({
        url: BASE_URL + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
        }
    }).then(res => {
        if (res.data.code === 401) {
            // Token过期，跳转登录
            uni.navigateTo({ url: '/pages/login/login' });
        }
        return res.data;
    });
};
```

## 正确性属性

*属性是系统在所有有效执行中应保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：购物车总价计算一致性

*对于任意*购物车中已选中的商品集合，计算得到的总价格应等于每个选中条目的（SKU单价 × 数量）之和。

**验证需求：5.6**

### 属性 2：购物车添加相同SKU的幂等合并

*对于任意*用户和任意SKU，如果该SKU已存在于购物车中，再次添加该SKU后，购物车中该SKU的条目数量仍为1，且数量字段增加1。

**验证需求：5.2**

### 属性 3：库存扣减一致性

*对于任意*成功创建的订单，订单中每个SKU的库存减少量应等于该订单项的购买数量。

**验证需求：6.3**

### 属性 4：库存不足阻止下单

*对于任意*订单提交请求，如果其中任一SKU的请求数量大于当前库存，则订单创建应失败且所有SKU库存保持不变。

**验证需求：6.4**

### 属性 5：订单编号唯一性

*对于任意*两个成功创建的订单，它们的订单编号应互不相同。

**验证需求：6.5**

### 属性 6：订单超时取消恢复库存

*对于任意*因超时被取消的订单，取消后对应SKU的库存应恢复为下单前的数量（即库存增加量等于订单项购买数量）。

**验证需求：7.4**

### 属性 7：支付状态转换正确性

*对于任意*待支付状态的订单，模拟支付成功后状态应变为已支付；模拟支付失败后状态应保持为待支付。

**验证需求：7.2, 7.3**

### 属性 8：分类商品归属正确性

*对于任意*分类查询请求，返回的所有商品的 category_id 应等于请求的分类ID。

**验证需求：3.2**

### 属性 9：分页数据量约束

*对于任意*分页查询请求，返回的数据条数应不超过指定的 pageSize（默认20）。

**验证需求：2.2, 2.4, 3.3**

### 属性 10：分类删除保护

*对于任意*包含商品的分类，执行删除操作应失败，且该分类数据保持不变。

**验证需求：10.3**

### 属性 11：用户禁用后请求拒绝

*对于任意*被禁用的用户，使用该用户Token发起的任何需要认证的API请求应返回禁用提示并拒绝执行。

**验证需求：12.2**

### 属性 12：Token有效性验证

*对于任意*格式错误或已过期的Token，系统应拒绝请求并返回401状态码；*对于任意*有效Token，系统应能正确解析出用户ID和角色信息。

**验证需求：1.3, 13.2**

### 属性 13：地址数量上限约束

*对于任意*用户，其收货地址数量应不超过10个。当已有10个地址时，新增地址操作应失败。

**验证需求：8.4**

### 属性 14：退款恢复库存

*对于任意*执行退款操作的订单，退款后对应SKU的库存应增加该订单项的购买数量。

**验证需求：11.3**

### 属性 15：商品软删除不可见性

*对于任意*被软删除的商品，用户端的所有商品查询接口应不返回该商品。

**验证需求：9.3**

### 属性 16：购物车数量范围约束

*对于任意*购物车数量修改操作，修改后的数量应在 [1, 该SKU当前库存] 范围内。

**验证需求：5.3**

### 属性 17：输入校验拒绝非法数据

*对于任意*包含SQL注入特征字符或超出长度限制的用户输入，系统应拒绝该请求并返回校验错误。

**验证需求：13.5**
