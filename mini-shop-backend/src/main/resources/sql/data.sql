-- ============================================
-- Mini Shop 初始数据
-- 包含管理员账号和测试数据
-- ============================================

-- 管理员账号（密码为 admin123 的 BCrypt 加密值）
INSERT INTO user (openid, nickname, avatar_url, phone, status, role, password) VALUES
('admin_openid_001', '系统管理员', NULL, '13800000000', 1, 1, '$2a$10$lBdmQ8hoq3hPnghczqW.4.kqPYm7UH4sVohYGTxXdBFi8SOnKq/66');

-- 测试普通用户
INSERT INTO user (openid, nickname, avatar_url, phone, status, role) VALUES
('test_openid_001', '测试用户A', 'https://picsum.photos/seed/avatar1/200/200', '13800000001', 1, 0),
('test_openid_002', '测试用户B', 'https://picsum.photos/seed/avatar2/200/200', '13800000002', 1, 0);

-- 商品分类
INSERT INTO category (name, icon, sort_order) VALUES
('手机数码', 'https://picsum.photos/seed/cat1/100/100', 100),
('服装鞋帽', 'https://picsum.photos/seed/cat2/100/100', 90),
('食品饮料', 'https://picsum.photos/seed/cat3/100/100', 80),
('家居生活', 'https://picsum.photos/seed/cat4/100/100', 70),
('美妆护肤', 'https://picsum.photos/seed/cat5/100/100', 60);

-- 商品数据
INSERT INTO product (name, description, category_id, main_image, images, min_price, status, is_hot) VALUES
('iPhone 15 Pro', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计', 1, 'https://picsum.photos/seed/iphone/400/400', '["https://picsum.photos/seed/iphone1/400/400","https://picsum.photos/seed/iphone2/400/400"]', 7999.00, 1, 1),
('华为 Mate 60 Pro', '华为旗舰手机，麒麟芯片回归，卫星通信', 1, 'https://picsum.photos/seed/huawei/400/400', '["https://picsum.photos/seed/huawei1/400/400","https://picsum.photos/seed/huawei2/400/400"]', 6999.00, 1, 1),
('经典白色T恤', '纯棉舒适面料，百搭经典款', 2, 'https://picsum.photos/seed/tshirt/400/400', '["https://picsum.photos/seed/tshirt1/400/400"]', 59.00, 1, 0),
('运动休闲鞋', '轻便透气，适合日常运动', 2, 'https://picsum.photos/seed/sneaker/400/400', '["https://picsum.photos/seed/sneaker1/400/400","https://picsum.photos/seed/sneaker2/400/400"]', 299.00, 1, 1),
('有机坚果礼盒', '精选多种坚果，健康美味', 3, 'https://picsum.photos/seed/nuts/400/400', '["https://picsum.photos/seed/nuts1/400/400"]', 128.00, 1, 0);

-- 商品规格维度（iPhone 15 Pro）
INSERT INTO product_spec (product_id, name, sort_order) VALUES
(1, '颜色', 1),
(1, '存储容量', 2);

-- 商品规格维度（经典白色T恤）
INSERT INTO product_spec (product_id, name, sort_order) VALUES
(3, '颜色', 1),
(3, '尺码', 2);

-- 商品规格维度（运动休闲鞋）
INSERT INTO product_spec (product_id, name, sort_order) VALUES
(4, '颜色', 1),
(4, '尺码', 2);

-- 规格值（iPhone 15 Pro - 颜色，spec_id=1）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(1, '原色钛金属', 1),
(1, '蓝色钛金属', 2),
(1, '白色钛金属', 3);

-- 规格值（iPhone 15 Pro - 存储容量，spec_id=2）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(2, '256GB', 1),
(2, '512GB', 2),
(2, '1TB', 3);

-- 规格值（经典白色T恤 - 颜色，spec_id=3）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(3, '白色', 1),
(3, '黑色', 2);

-- 规格值（经典白色T恤 - 尺码，spec_id=4）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(4, 'S', 1),
(4, 'M', 2),
(4, 'L', 3),
(4, 'XL', 4);

-- 规格值（运动休闲鞋 - 颜色，spec_id=5）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(5, '黑色', 1),
(5, '白色', 2);

-- 规格值（运动休闲鞋 - 尺码，spec_id=6）
INSERT INTO spec_value (spec_id, value, sort_order) VALUES
(6, '39', 1),
(6, '40', 2),
(6, '41', 3),
(6, '42', 4),
(6, '43', 5);

-- SKU（iPhone 15 Pro）
INSERT INTO sku (product_id, price, stock, spec_desc, image) VALUES
(1, 7999.00, 100, '原色钛金属,256GB', 'https://picsum.photos/seed/sku1/400/400'),
(1, 9999.00, 50, '原色钛金属,512GB', 'https://picsum.photos/seed/sku2/400/400'),
(1, 12999.00, 30, '原色钛金属,1TB', 'https://picsum.photos/seed/sku3/400/400'),
(1, 7999.00, 80, '蓝色钛金属,256GB', 'https://picsum.photos/seed/sku4/400/400'),
(1, 9999.00, 40, '蓝色钛金属,512GB', 'https://picsum.photos/seed/sku5/400/400'),
(1, 12999.00, 20, '蓝色钛金属,1TB', 'https://picsum.photos/seed/sku6/400/400');

-- SKU（华为 Mate 60 Pro - 无规格维度，单SKU）
INSERT INTO sku (product_id, price, stock, spec_desc, image) VALUES
(2, 6999.00, 200, '默认', 'https://picsum.photos/seed/sku7/400/400');

-- SKU（经典白色T恤）
INSERT INTO sku (product_id, price, stock, spec_desc, image) VALUES
(3, 59.00, 500, '白色,S', NULL),
(3, 59.00, 500, '白色,M', NULL),
(3, 59.00, 500, '白色,L', NULL),
(3, 59.00, 300, '白色,XL', NULL),
(3, 69.00, 400, '黑色,S', NULL),
(3, 69.00, 400, '黑色,M', NULL),
(3, 69.00, 400, '黑色,L', NULL),
(3, 69.00, 200, '黑色,XL', NULL);

-- SKU（运动休闲鞋）
INSERT INTO sku (product_id, price, stock, spec_desc, image) VALUES
(4, 299.00, 100, '黑色,39', NULL),
(4, 299.00, 150, '黑色,40', NULL),
(4, 299.00, 200, '黑色,41', NULL),
(4, 299.00, 200, '黑色,42', NULL),
(4, 299.00, 100, '黑色,43', NULL),
(4, 319.00, 80, '白色,39', NULL),
(4, 319.00, 120, '白色,40', NULL),
(4, 319.00, 150, '白色,41', NULL),
(4, 319.00, 150, '白色,42', NULL),
(4, 319.00, 80, '白色,43', NULL);

-- SKU（有机坚果礼盒 - 单SKU）
INSERT INTO sku (product_id, price, stock, spec_desc, image) VALUES
(5, 128.00, 1000, '默认', NULL);

-- SKU与规格值关联（iPhone 15 Pro）
-- SKU 1: 原色钛金属(spec_value_id=1), 256GB(spec_value_id=4)
INSERT INTO sku_spec_value (sku_id, spec_value_id) VALUES
(1, 1), (1, 4),
(2, 1), (2, 5),
(3, 1), (3, 6),
(4, 2), (4, 4),
(5, 2), (5, 5),
(6, 2), (6, 6);

-- SKU与规格值关联（经典白色T恤）
-- 白色(spec_value_id=7), 黑色(spec_value_id=8)
-- S(spec_value_id=9), M(spec_value_id=10), L(spec_value_id=11), XL(spec_value_id=12)
INSERT INTO sku_spec_value (sku_id, spec_value_id) VALUES
(8, 7), (8, 9),
(9, 7), (9, 10),
(10, 7), (10, 11),
(11, 7), (11, 12),
(12, 8), (12, 9),
(13, 8), (13, 10),
(14, 8), (14, 11),
(15, 8), (15, 12);

-- SKU与规格值关联（运动休闲鞋）
-- 黑色(spec_value_id=13), 白色(spec_value_id=14)
-- 39(spec_value_id=15), 40(spec_value_id=16), 41(spec_value_id=17), 42(spec_value_id=18), 43(spec_value_id=19)
INSERT INTO sku_spec_value (sku_id, spec_value_id) VALUES
(16, 13), (16, 15),
(17, 13), (17, 16),
(18, 13), (18, 17),
(19, 13), (19, 18),
(20, 13), (20, 19),
(21, 14), (21, 15),
(22, 14), (22, 16),
(23, 14), (23, 17),
(24, 14), (24, 18),
(25, 14), (25, 19);

-- 轮播图
INSERT INTO banner (image_url, link_url, sort_order, status) VALUES
('https://picsum.photos/seed/banner1/750/360', '/pages/product/detail?id=1', 100, 1),
('https://picsum.photos/seed/banner2/750/360', '/pages/product/detail?id=2', 90, 1),
('https://picsum.photos/seed/banner3/750/360', '/pages/category/index?id=2', 80, 1);

-- 测试用户收货地址
INSERT INTO address (user_id, receiver_name, phone, province, city, district, detail, is_default) VALUES
(2, '张三', '13800000001', '广东省', '深圳市', '南山区', '科技园路1号', 1),
(2, '李四', '13800000003', '北京市', '朝阳区', '三里屯', '太古里商场旁', 0);
