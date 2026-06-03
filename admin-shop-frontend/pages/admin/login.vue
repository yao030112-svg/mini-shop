<template>
  <view class="admin-login">
    <view class="login-header">
      <text class="title">管理后台</text>
      <text class="subtitle">请输入管理员账号登录</text>
    </view>

    <view class="login-form">
      <view class="form-item">
        <text class="label">账号</text>
        <input
          class="input"
          type="text"
          v-model="username"
          placeholder="请输入管理员账号"
          placeholder-class="placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input
          class="input"
          type="password"
          v-model="password"
          placeholder="请输入密码"
          placeholder-class="placeholder"
        />
      </view>
      <button class="login-btn" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </button>
    </view>
  </view>
</template>

<script>
import { adminLogin } from '../../api/auth'

export default {
  data() {
    return {
      username: '',
      password: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.username.trim()) {
        uni.showToast({ title: '请输入账号', icon: 'none' })
        return
      }
      if (!this.password.trim()) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const res = await adminLogin(this.username, this.password)
        // 存储 token
        uni.setStorageSync('token', res.data.token)
        uni.setStorageSync('adminInfo', JSON.stringify(res.data))
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/admin/index' })
        }, 500)
      } catch (e) {
        console.error('登录失败', e)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  background-color: #f5f6fa;
  padding: 200rpx 60rpx 0;
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.login-header .title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.login-header .subtitle {
  display: block;
  font-size: 28rpx;
  color: #999;
}

.login-form {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item .label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.form-item .input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.placeholder {
  color: #ccc;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #ff6600;
  color: #fff;
  font-size: 32rpx;
  border-radius: 8rpx;
  margin-top: 40rpx;
  border: none;
}

.login-btn[disabled] {
  background: #ffb380;
}
</style>
