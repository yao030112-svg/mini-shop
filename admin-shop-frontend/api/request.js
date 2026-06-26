/**
 * 统一请求封装
 * 基于 uni.request，统一处理 Token、错误码、loading
 */

const BASE_URL = 'http://127.0.0.1:8090'

// 当前正在进行的请求数量（用于控制 loading 显示）
let requestCount = 0

/**
 * 显示 loading
 */
function showLoading() {
  if (requestCount === 0) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
  requestCount++
}

/**
 * 隐藏 loading
 */
function hideLoading() {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    uni.hideLoading()
  }
}

/**
 * 核心请求方法
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径（不含 BASE_URL）
 * @param {string} options.method - 请求方法
 * @param {Object} options.data - 请求数据
 * @param {boolean} options.loading - 是否显示 loading，默认 true
 * @returns {Promise}
 */
function request(options) {
  const { loading = true } = options

  if (loading) {
    showLoading()
  }

  const token = uni.getStorageSync('token')

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        const data = res.data

        // 401 未认证，清除 token 并跳转登录页
        if (data.code === 401) {
          uni.removeStorageSync('token')
          uni.showToast({ title: '请重新登录', icon: 'none' })
          uni.navigateTo({ url: '/pages/login/login' })
          reject(data)
          return
        }

        // 业务成功
        if (data.code === 200) {
          resolve(data)
        } else {
          // 业务错误，提示错误信息
          uni.showToast({ title: data.message || '请求失败', icon: 'none' })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      },
      complete: () => {
        if (loading) {
          hideLoading()
        }
      }
    })
  })
}

/**
 * GET 请求
 */
export function get(url, data, options = {}) {
  return request({ url, method: 'GET', data, ...options })
}

/**
 * POST 请求
 */
export function post(url, data, options = {}) {
  return request({ url, method: 'POST', data, ...options })
}

/**
 * PUT 请求
 */
export function put(url, data, options = {}) {
  return request({ url, method: 'PUT', data, ...options })
}

/**
 * DELETE 请求
 */
export function del(url, data, options = {}) {
  return request({ url, method: 'DELETE', data, ...options })
}

export default request
