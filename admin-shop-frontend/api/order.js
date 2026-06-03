/**
 * 订单相关 API
 */
import { get, post } from './request'

/**
 * 创建订单
 * @param {number} addressId - 收货地址 ID
 */
export function createOrder(addressId) {
  return post('/api/order/create', { addressId })
}

/**
 * 模拟支付
 * @param {number} id - 订单 ID
 */
export function payOrder(id) {
  return post(`/api/order/pay/${id}`)
}

/**
 * 获取订单列表
 * @param {number|null} status - 订单状态筛选（可选）
 */
export function getOrderList(status) {
  const params = {}
  if (status !== undefined && status !== null) {
    params.status = status
  }
  return get('/api/order/list', params)
}

/**
 * 获取订单详情
 * @param {number} id - 订单 ID
 */
export function getOrderDetail(id) {
  return get(`/api/order/${id}`)
}

/**
 * 取消订单
 * @param {number} id - 订单 ID
 */
export function cancelOrder(id) {
  return post(`/api/order/cancel/${id}`)
}
