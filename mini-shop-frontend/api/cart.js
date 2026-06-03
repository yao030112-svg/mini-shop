/**
 * 购物车相关 API
 */
import { get, post, put, del } from './request'

/**
 * 获取购物车列表
 */
export function getCartList() {
  return get('/api/cart/list')
}

/**
 * 添加到购物车
 * @param {number} skuId - SKU ID
 */
export function addToCart(skuId) {
  return post('/api/cart/add', { skuId })
}

/**
 * 更新购物车数量
 * @param {number} id - 购物车条目 ID
 * @param {number} quantity - 新数量
 */
export function updateQuantity(id, quantity) {
  return put('/api/cart/update', { id, quantity })
}

/**
 * 删除购物车条目
 * @param {number} id - 购物车条目 ID
 */
export function deleteItem(id) {
  return del(`/api/cart/delete/${id}`)
}

/**
 * 全选/取消全选
 * @param {boolean} selected - 是否选中
 */
export function selectAll(selected) {
  return put('/api/cart/select-all', { selected })
}

/**
 * 切换单个条目选中状态
 * @param {number} id - 购物车条目 ID
 * @param {boolean} selected - 是否选中
 */
export function toggleSelect(id, selected) {
  return put(`/api/cart/select/${id}`, { selected })
}
