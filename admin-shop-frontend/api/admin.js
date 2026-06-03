/**
 * 管理员相关 API
 */
import { get, post, put, del } from './request'

// ==================== 商品管理 ====================

/**
 * 获取商品列表（管理端，分页）
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 */
export function getProductList(page = 1, pageSize = 20) {
  return get('/api/admin/product/list', { page, pageSize })
}

/**
 * 添加商品
 * @param {Object} data - 商品信息
 */
export function addProduct(data) {
  return post('/api/admin/product/add', data)
}

/**
 * 更新商品
 * @param {Object} data - 商品信息（含 id）
 */
export function updateProduct(data) {
  return put('/api/admin/product/update', data)
}

/**
 * 删除商品（软删除）
 * @param {number} id - 商品 ID
 */
export function deleteProduct(id) {
  return del(`/api/admin/product/delete/${id}`)
}

/**
 * 上下架商品
 * @param {number} id - 商品 ID
 * @param {number} status - 状态：1上架 0下架
 */
export function updateProductStatus(id, status) {
  return put(`/api/admin/product/status/${id}`, { status })
}

// ==================== 轮播图管理 ====================

/**
 * 获取轮播图列表
 */
export function getBannerList() {
  return get('/api/admin/banner/list')
}

/**
 * 添加轮播图
 * @param {Object} data - 轮播图信息
 */
export function addBanner(data) {
  return post('/api/admin/banner/add', data)
}

/**
 * 更新轮播图
 * @param {Object} data - 轮播图信息（含 id）
 */
export function updateBanner(data) {
  return put('/api/admin/banner/update', data)
}

/**
 * 删除轮播图
 * @param {number} id - 轮播图 ID
 */
export function deleteBanner(id) {
  return del(`/api/admin/banner/delete/${id}`)
}

/**
 * 更新轮播图状态
 * @param {number} id - 轮播图 ID
 * @param {number} status - 状态：1启用 0禁用
 */
export function updateBannerStatus(id, status) {
  return put(`/api/admin/banner/status/${id}`, { status })
}

// ==================== 分类管理 ====================

/**
 * 获取分类列表（管理端）
 */
export function getCategoryList() {
  return get('/api/admin/category/list')
}

/**
 * 添加分类
 * @param {Object} data - 分类信息
 */
export function addCategory(data) {
  return post('/api/admin/category/add', data)
}

/**
 * 更新分类
 * @param {Object} data - 分类信息（含 id）
 */
export function updateCategory(data) {
  return put('/api/admin/category/update', data)
}

/**
 * 删除分类
 * @param {number} id - 分类 ID
 */
export function deleteCategory(id) {
  return del(`/api/admin/category/delete/${id}`)
}

/**
 * 获取分类下的商品列表
 * @param {number} categoryId - 分类 ID
 */
export function getCategoryProducts(categoryId) {
  return get(`/api/admin/category/${categoryId}/products`)
}

// ==================== 订单管理 ====================

/**
 * 获取订单列表（管理端）
 * @param {number|null} status - 订单状态筛选（可选）
 * @param {number|null} userId - 用户 ID 筛选（可选）
 */
export function getOrderList(status, userId) {
  const params = {}
  if (status !== undefined && status !== null) {
    params.status = status
  }
  if (userId !== undefined && userId !== null) {
    params.userId = userId
  }
  return get('/api/admin/order/list', params)
}

/**
 * 发货
 * @param {number} id - 订单 ID
 */
export function shipOrder(id) {
  return put(`/api/admin/order/ship/${id}`)
}

/**
 * 退款
 * @param {number} id - 订单 ID
 */
export function refundOrder(id) {
  return put(`/api/admin/order/refund/${id}`)
}

/**
 * 获取订单详情
 * @param {number} id - 订单 ID
 */
export function getOrderDetail(id) {
  return get(`/api/admin/order/detail/${id}`)
}

// ==================== 用户管理 ====================

/**
 * 获取用户列表
 */
export function getUserList() {
  return get('/api/admin/user/list')
}

/**
 * 禁用用户
 * @param {number} id - 用户 ID
 */
export function disableUser(id) {
  return put(`/api/admin/user/disable/${id}`)
}

/**
 * 启用用户
 * @param {number} id - 用户 ID
 */
export function enableUser(id) {
  return put(`/api/admin/user/enable/${id}`)
}

// ==================== 管理端统计 ====================

/**
 * 获取管理端统计数据
 */
export function getAdminStats() {
  return get('/api/admin/stats')
}
