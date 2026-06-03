<template>
  <view class="login-page">
    <!-- Logo 区域 -->
    <view class="logo-section">
      <image class="logo-section__image" src="/static/images/logo.png" mode="aspectFit" />
      <text class="logo-section__title">迷你商城</text>
      <text class="logo-section__desc">登录后享受更多服务</text>
    </view>

    <!-- 登录按钮 -->
    <view class="login-section">
      <view class="login-btn" @click="handleWxLogin">
        <text class="login-btn__icon">🔐</text>
        <text class="login-btn__text">微信一键登录</text>
      </view>
    </view>

    <!-- 提示 -->
    <view class="tips-section">
      <text class="tips-section__text">登录即表示同意《用户协议》和《隐私政策》</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loading: false
    }
  },

  methods: {
    // 微信一键登录
    async handleWxLogin() {
      if (this.loading) return
      this.loading = true

      try {
        uni.showLoading({ title: '登录中' })

        // 调用 store 中的 wxLogin action
        await this.$store.dispatch('user/wxLogin')

        // 获取用户信息
        try {
          await this.$store.dispatch('user/getUserInfo')
        } catch (e) {
          // 获取用户信息失败不影响登录，可以后续再获取
          console.error('获取用户信息失败', e)
        }

        uni.hideLoading()
        uni.showToast({ title: '登录成功', icon: 'success' })

        // 登录成功后返回上一页
        setTimeout(() => {
          uni.navigateBack({
            fail: () => {
              // 如果没有上一页，跳转到首页
              uni.switchTab({ url: '/pages/index/index' })
            }
          })
        }, 1000)
      } catch (e) {
        uni.hideLoading()
        console.error('登录失败', e)
        uni.showToast({ title: e.message || '登录失败，请重试', icon: 'none' })
      } finally {
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
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $primary-color;
  border-radius: $border-radius-round;
  padding: $spacing-md;

  &__icon {
    font-size: 36rpx;
    margin-right: $spacing-sm;
  }

  &__text {
    font-size: $font-size-lg;
    color: #ffffff;
    font-weight: bold;
  }
}

/* 提示 */
.tips-section {
  margin-top: $spacing-xl;

  &__text {
    font-size: $font-size-xs;
    color: $text-color-placeholder;
  }
}
</style>
