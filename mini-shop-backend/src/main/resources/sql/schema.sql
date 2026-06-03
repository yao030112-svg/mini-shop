-- ============================================
-- Mini Shop 数据库初始化脚本
-- 建表顺序按照外键依赖关系排列
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS user (
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
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon VARCHAR(256) COMMENT '分类图标URL',
    sort_order INT DEFAULT 0 COMMENT '排序权重，越大越靠前',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 商品表
CREATE TABLE IF NOT EXISTS product (
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
CREATE TABLE IF NOT EXISTS product_spec (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(32) NOT NULL COMMENT '规格名称，如颜色、尺码',
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- 规格值表
CREATE TABLE IF NOT EXISTS spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spec_id BIGINT NOT NULL COMMENT '规格维度ID',
    value VARCHAR(64) NOT NULL COMMENT '规格值，如红色、XL',
    sort_order INT DEFAULT 0,
    FOREIGN KEY (spec_id) REFERENCES product_spec(id)
);

-- SKU表
CREATE TABLE IF NOT EXISTS sku (
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
CREATE TABLE IF NOT EXISTS sku_spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_id BIGINT NOT NULL,
    spec_value_id BIGINT NOT NULL,
    FOREIGN KEY (sku_id) REFERENCES sku(id),
    FOREIGN KEY (spec_value_id) REFERENCES spec_value(id),
    UNIQUE KEY uk_sku_spec (sku_id, spec_value_id)
);

-- 购物车表
CREATE TABLE IF NOT EXISTS cart (
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
CREATE TABLE IF NOT EXISTS address (
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
CREATE TABLE IF NOT EXISTS orders (
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
CREATE TABLE IF NOT EXISTS order_item (
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
CREATE TABLE IF NOT EXISTS banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_url VARCHAR(256) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(256) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
