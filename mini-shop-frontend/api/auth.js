/**
 * 认证相关 API
 */
import { post, get } from './request'

/**
 * 微信登录
 * @param {string} code - 微信登录 code
 */
export function wxLogin(code) {
  return post('/api/auth/wx-login', { code })
}

/**
 * 管理员登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 */
export function adminLogin(username, password) {
  return post('/api/auth/admin-login', { username, password })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/api/user/info')
}
