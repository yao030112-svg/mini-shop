<template>
  <view class="login-page">
    <view class="logo-section">
      <image class="logo-section__image" src="/static/images/logo.png" mode="aspectFit" />
      <text class="logo-section__title">迷你商城</text>
      <text class="logo-section__desc">使用微信资料登录</text>
    </view>

    <view class="login-section">
      <button class="login-btn" open-type="chooseAvatar" @chooseavatar="handleChooseAvatar">
        <text class="login-btn__text">选择头像</text>
      </button>
      <input
        class="nickname-input"
        type="nickname"
        placeholder="请输入微信昵称"
        v-model="nickname"
      />
      <image v-if="avatarUrl" class="avatar-preview" :src="avatarUrl" mode="aspectFill" />
      <button class="login-btn login-btn--primary" @click="handleLogin">
        <text class="login-btn__text">微信一键登录</text>
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      nickname: '',
      avatarUrl: ''
    }
  },

  methods: {
    handleChooseAvatar(e) {
      this.avatarUrl = e.detail.avatarUrl
    },

    async handleLogin() {
      if (this.loading) return
      if (!this.avatarUrl) {
        uni.showToast({ title: '请先选择头像', icon: 'none' })
        return
      }
      if (!this.nickname) {
        uni.showToast({ title: '请输入微信昵称', icon: 'none' })
        return
      }

      this.loading = true
      uni.showLoading({ title: '登录中...' })

      try {
        await this.$store.dispatch('user/wxLogin', {
          nickName: this.nickname,
          avatarUrl: this.avatarUrl
        })

        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack({
            fail: () => uni.switchTab({ url: '/pages/user/index' })
          })
        }, 800)
      } catch (err) {
        uni.showToast({ title: err.errMsg || err.message || '登录失败', icon: 'none' })
      } finally {
        uni.hideLoading()
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background-color: $bg-color-white;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 $spacing-lg;
}

/* Logo 区域 */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 200rpx;

  &__image {
    width: 160rpx;
    height: 160rpx;
    border-radius: $border-radius-lg;
  }

  &__title {
    font-size: $font-size-xxl;
    color: $text-color;
    font-weight: bold;
    margin-top: $spacing-lg;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $text-color-lighter;
    margin-top: $spacing-xs;
  }
}

/* 登录按钮 */
.login-section {
  width: 100%;
  margin-top: 120rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $primary-color;
  border-radius: $border-radius-round;
  padding: $spacing-md;
  margin-bottom: $spacing-md;

  &__text {
    font-size: $font-size-lg;
    color: #ffffff;
    font-weight: bold;
  }

  &--primary {
    margin-top: $spacing-lg;
  }
}

.nickname-input {
  width: 100%;
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  margin-bottom: $spacing-md;
}

.avatar-preview {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin: $spacing-md 0;
}
</style>
