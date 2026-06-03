<template>
  <view class="user-page">
    <!-- 用户信息区 -->
    <view class="user-header" @click="handleUserClick">
      <image
        class="user-header__avatar"
        :src="userInfo ? userInfo.avatarUrl : '/static/images/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="user-header__info">
        <text class="user-header__name">{{ userInfo ? userInfo.nickname : '点击登录' }}</text>
        <text class="user-header__desc" v-if="!userInfo">登录后享受更多服务</text>
      </view>
    </view>

    <!-- 订单状态入口 -->
    <view class="order-section">
      <view class="order-section__header">
        <text class="order-section__title">我的订单</text>
        <view class="order-section__all" @click="goOrderList(0)">
          <text class="order-section__all-text">全部订单</text>
          <text class="order-section__arrow">></text>
        </view>
      </view>
      <view class="order-section__grid">
        <view class="order-section__item" @click="goOrderList(1)">
          <view class="order-section__icon-wrap">
            <text class="order-section__icon">💰</text>
          </view>
          <text class="order-section__label">待支付</text>
        </view>
        <view class="order-section__item" @click="goOrderList(2)">
          <view class="order-section__icon-wrap">
            <text class="order-section__icon">📦</text>
          </view>
          <text class="order-section__label">已支付</text>
        </view>
        <view class="order-section__item" @click="goOrderList(3)">
          <view class="order-section__icon-wrap">
            <text class="order-section__icon">🚚</text>
          </view>
          <text class="order-section__label">已发货</text>
        </view>
        <view class="order-section__item" @click="goOrderList(4)">
          <view class="order-section__icon-wrap">
            <text class="order-section__icon">✅</text>
          </view>
          <text class="order-section__label">已完成</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-item" @click="goAddress">
        <text class="menu-item__icon">📍</text>
        <text class="menu-item__text">收货地址管理</text>
        <text class="menu-item__arrow">></text>
      </view>
      <view class="menu-item" @click="goAbout">
        <text class="menu-item__icon">ℹ️</text>
        <text class="menu-item__text">关于我们</text>
        <text class="menu-item__arrow">></text>
      </view>
      <!-- 管理员入口 -->
      <view class="menu-item" v-if="userInfo && userInfo.role === 1" @click="goAdmin">
        <text class="menu-item__icon">⚙️</text>
        <text class="menu-item__text">管理后台</text>
        <text class="menu-item__arrow">></text>
      </view>
    </view>

    <!-- 退出登录 -->
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
    if (this.token && !this.userInfo) {
      this.$store.dispatch('user/getUserInfo')
    }
    
    // 尝试从本地存储获取微信用户信息（用于显示头像和昵称）
    const wxUserInfo = uni.getStorageSync('wxUserInfo')
    if (wxUserInfo && !this.userInfo?.avatarUrl) {
      try {
        const info = JSON.parse(wxUserInfo)
        if (info.avatarUrl || info.nickName) {
          this.$store.commit('user/SET_USER_INFO', {
            ...this.userInfo,
            avatarUrl: info.avatarUrl || this.userInfo?.avatarUrl,
            nickname: info.nickName || this.userInfo?.nickname
          })
        }
      } catch (e) {
        console.error('解析微信用户信息失败', e)
      }
    }
  },

  methods: {
    // 点击用户区域
    handleUserClick() {
      if (!this.token) {
        uni.navigateTo({ url: '/pages/login/index' })
      }
    },

    // 跳转订单列表
    goOrderList(tab) {
      if (!this.token) {
        uni.navigateTo({ url: '/pages/login/index' })
        return
      }
      uni.navigateTo({ url: `/pages/order/list?tab=${tab}` })
    },

    // 跳转收货地址
    goAddress() {
      if (!this.token) {
        uni.navigateTo({ url: '/pages/login/index' })
        return
      }
      uni.navigateTo({ url: '/pages/address/list' })
    },

    // 关于我们
    goAbout() {
      uni.showToast({ title: '迷你商城 v1.0', icon: 'none' })
    },

    // 管理后台
    goAdmin() {
      uni.navigateTo({ url: '/pages/admin/product/list' })
    },

    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('user/logout')
            uni.showToast({ title: '已退出', icon: 'success' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-page {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 用户信息区 */
.user-header {
  display: flex;
  align-items: center;
  padding: $spacing-xl $spacing-lg;
  background: linear-gradient(135deg, $primary-color, $primary-light);

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
    margin-right: $spacing-lg;
  }

  &__info {
    flex: 1;
  }

  &__name {
    font-size: $font-size-xl;
    color: #ffffff;
    font-weight: bold;
    display: block;
  }

  &__desc {
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 8rpx;
    display: block;
  }
}

/* 订单区域 */
.order-section {
  background-color: $bg-color-white;
  margin: $spacing-sm;
  border-radius: $border-radius-md;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md;
    border-bottom: 1rpx solid $border-color-light;
  }

  &__title {
    font-size: $font-size-md;
    color: $text-color;
    font-weight: bold;
  }

  &__all {
    display: flex;
    align-items: center;
  }

  &__all-text {
    font-size: $font-size-sm;
    color: $text-color-lighter;
  }

  &__arrow {
    font-size: $font-size-sm;
    color: $text-color-lighter;
    margin-left: 4rpx;
  }

  &__grid {
    display: flex;
    padding: $spacing-md 0;
  }

  &__item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__icon-wrap {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__icon {
    font-size: 48rpx;
  }

  &__label {
    font-size: $font-size-sm;
    color: $text-color-light;
    margin-top: 8rpx;
  }
}

/* 功能列表 */
.menu-section {
  background-color: $bg-color-white;
  margin: $spacing-sm;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-light;

  &:last-child {
    border-bottom: none;
  }

  &__icon {
    font-size: 40rpx;
    margin-right: $spacing-md;
  }

  &__text {
    flex: 1;
    font-size: $font-size-md;
    color: $text-color;
  }

  &__arrow {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }
}

/* 退出登录 */
.logout-section {
  margin: $spacing-xl $spacing-sm;
}

.logout-btn {
  background-color: $bg-color-white;
  border-radius: $border-radius-md;
  padding: $spacing-md;
  text-align: center;

  &__text {
    font-size: $font-size-md;
    color: $error-color;
  }
}
</style>
