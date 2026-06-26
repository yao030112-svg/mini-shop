import { wxLogin, getUserInfo } from '@/api/auth'

export default {
  namespaced: true,

  state: {
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      uni.setStorageSync('userInfo', userInfo)
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  },

  actions: {
    async wxLogin({ commit }, profile = null) {
      const loginRes = await new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: resolve,
          fail: reject
        })
      })

      const res = await wxLogin(loginRes.code, profile)
      commit('SET_TOKEN', res.data.token)
      commit('SET_USER_INFO', {
        userId: res.data.userId,
        nickname: res.data.nickname,
        avatarUrl: res.data.avatarUrl,
        role: res.data.role
      })
      return res
    },

    async getUserInfo({ commit }) {
      const res = await getUserInfo()
      commit('SET_USER_INFO', res.data)
      return res
    },

    logout({ commit }) {
      commit('CLEAR_USER')
    }
  }
}
