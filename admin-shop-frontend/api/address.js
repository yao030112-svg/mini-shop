/**
 * 收货地址相关 API
 */
import { get, post, put, del } from './request'

/**
 * 获取地址列表
 */
export function getAddressList() {
  return get('/api/address/list')
}

/**
 * 新增地址
 * @param {Object} data - 地址信息
 */
export function addAddress(data) {
  return post('/api/address/add', data)
}

/**
 * 更新地址
 * @param {Object} data - 地址信息（含 id）
 */
export function updateAddress(data) {
  return put('/api/address/update', data)
}

/**
 * 删除地址
 * @param {number} id - 地址 ID
 */
export function deleteAddress(id) {
  return del(`/api/address/delete/${id}`)
}

/**
 * 设为默认地址
 * @param {number} id - 地址 ID
 */
export function setDefault(id) {
  return put(`/api/address/default/${id}`)
}
