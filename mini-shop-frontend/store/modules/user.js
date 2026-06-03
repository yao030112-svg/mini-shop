/**
 * 用户状态管理模块
 */
import { wxLogin, getUserInfo } from '@/api/auth'

export default {
  namespaced: true,

  state: {
    token: uni.getStorageSync('token') || '',
    userInfo: null
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = null
      uni.removeStorageSync('token')
    }
  },

  actions: {
    /**
     * 微信登录
     * 调用 wx.login 获取 code，发送到后端换取 token
     */
    async wxLogin({ commit }, userInfo = null) {
      let loginRes
      
      // #ifdef H5
      // 在浏览器环境中，使用模拟登录（仅用于开发测试）
      console.warn('当前为浏览器环境，使用模拟登录')
      loginRes = { code: 'mock-code-for-h5-testing' }
      // #endif
      
      // #ifndef H5
      // 在小程序环境中，调用真实的微信登录
      loginRes = await new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: res => resolve(res),
          fail: err => reject(err)
        })
      })
      // #endif
      
      const res = await wxLogin(loginRes.code)
      commit('SET_TOKEN', res.data.token)
      
      // 如果有用户信息，保存到 store
      if (userInfo) {
        commit('SET_USER_INFO', {
          ...res.data.userInfo,
          nickName: userInfo.nickName,
          avatarUrl: userInfo.avatarUrl
        })
      } else if (res.data.userInfo) {
        commit('SET_USER_INFO', res.data.userInfo)
      }
      
      return res
    },

    /**
     * 获取用户信息
     */
    async getUserInfo({ commit }) {
      const res = await getUserInfo()
      commit('SET_USER_INFO', res.data)
      return res
    },

    /**
     * 退出登录
     */
    logout({ commit }) {
      commit('CLEAR_USER')
    }
  }
}
