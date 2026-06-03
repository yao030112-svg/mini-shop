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
      <!-- 普通按钮，直接登录 -->
      <button class="login-btn" @click="handleLogin">
        <text class="login-btn__icon">🔐</text>
        <text class="login-btn__text">微信一键登录</text>
      </button>
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
    // 微信登录（静默登录，不需要授权弹窗）
    async handleLogin() {
      if (this.loading) return
      
      this.loading = true
      
      try {
        uni.showLoading({ title: '登录中...' })
        
        console.log('开始微信登录...')
        
        // 调用 store 中的 wxLogin action（获取 code 并发送到后端）
        const loginRes = await this.$store.dispatch('user/wxLogin')
        
        console.log('登录成功，token:', loginRes.data?.token)
        console.log('用户信息:', loginRes.data)
        
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
      } catch (err) {
        uni.hideLoading()
        console.error('登录失败详情:', err)
        uni.showToast({ 
          title: err.errMsg || err.message || '登录失败，请重试', 
          icon: 'none',
          duration: 3000
        })
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
