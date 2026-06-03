/**
 * 商品相关 API
 */
import { get } from './request'

/**
 * 获取热门商品
 */
export function getHotProducts() {
  return get('/api/product/hot')
}

/**
 * 获取商品列表（分页）
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 */
export function getProductList(page = 1, pageSize = 20) {
  return get('/api/product/list', { page, pageSize })
}

/**
 * 获取商品详情
 * @param {number} id - 商品 ID
 */
export function getProductDetail(id) {
  return get(`/api/product/${id}`)
}

/**
 * 按分类获取商品列表（分页）
 * @param {number} categoryId - 分类 ID
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 */
export function getProductsByCategory(categoryId, page = 1, pageSize = 20) {
  return get(`/api/product/category/${categoryId}`, { page, pageSize })
}

/**
 * 获取轮播图列表
 */
export function getBannerList() {
  return get('/api/banner/list')
}

/**
 * 获取分类列表
 */
export function getCategoryList() {
  return get('/api/category/list')
}
