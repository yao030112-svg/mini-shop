<template>
  <view class="user-page">
    <view class="user-header" @click="handleUserClick">
      <image
        class="user-header__avatar"
        :src="userInfo ? userInfo.avatarUrl : '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="user-header__info">
        <text class="user-header__name">{{ userInfo ? userInfo.nickname : '点击登录' }}</text>
        <text class="user-header__desc" v-if="!userInfo">登录后显示微信头像和昵称</text>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-item" @click="goAbout">
        <text class="menu-item__text">关于我们</text>
      </view>
    </view>

    <view class="logout-section" v-if="userInfo">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-btn__text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'

export default {
  computed: {
    ...mapState('user', ['userInfo', 'token'])
  },

  onShow() {
    const cached = uni.getStorageSync('userInfo')
    if (cached && !this.userInfo) {
      this.$store.commit('user/SET_USER_INFO', cached)
    }
    if (this.token && !this.userInfo) {
      this.$store.dispatch('user/getUserInfo')
    }
  },

  methods: {
    handleUserClick() {
      if (!this.token) {
        uni.navigateTo({ url: '/pages/login/index' })
      }
    },
    goAbout() {
      uni.showToast({ title: '迷你商城', icon: 'none' })
    },
    handleLogout() {
      this.$store.dispatch('user/logout')
      uni.showToast({ title: '已退出', icon: 'success' })
    }
  }
}
</script>
