<template>
  <view class="admin-home">
    <view class="page-header">
      <text class="page-title">管理后台</text>
      <text class="welcome-text">欢迎回来，管理员</text>
    </view>

    <!-- 数据统计卡片 -->
    <view class="stats-container">
      <view class="stat-card" @click="navigateTo('/pages/admin/order/list')">
        <view class="stat-icon order-icon">
          <text>📦</text>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.orderCount || 0 }}</text>
          <text class="stat-label">总订单数</text>
        </view>
      </view>

      <view class="stat-card" @click="navigateTo('/pages/admin/product/list')">
        <view class="stat-icon product-icon">
          <text>🛍️</text>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.productCount || 0 }}</text>
          <text class="stat-label">商品总数</text>
        </view>
      </view>

      <view class="stat-card" @click="navigateTo('/pages/admin/user/list')">
        <view class="stat-icon user-icon">
          <text>👥</text>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.userCount || 0 }}</text>
          <text class="stat-label">用户总数</text>
        </view>
      </view>

      <view class="stat-card">
        <view class="stat-icon revenue-icon">
          <text>💰</text>
        </view>
        <view class="stat-info">
          <text class="stat-value">¥{{ stats.totalRevenue || 0 }}</text>
          <text class="stat-label">总收入</text>
        </view>
      </view>
    </view>

    <!-- 今日数据 -->
    <view class="today-stats">
      <text class="section-title">今日数据</text>
      <view class="today-cards">
        <view class="today-card">
          <text class="today-value">{{ stats.todayOrders || 0 }}</text>
          <text class="today-label">今日订单</text>
        </view>
        <view class="today-card">
          <text class="today-value">¥{{ stats.todayRevenue || 0 }}</text>
          <text class="today-label">今日收入</text>
        </view>
        <view class="today-card">
          <text class="today-value">{{ stats.todayUsers || 0 }}</text>
          <text class="today-label">新增用户</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <text class="section-title">快捷操作</text>
      <view class="action-grid">
        <view class="action-item" @click="navigateTo('/pages/admin/product/list')">
          <text class="action-icon">📝</text>
          <text class="action-text">商品管理</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/admin/category/list')">
          <text class="action-icon">🏷️</text>
          <text class="action-text">分类管理</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/admin/banner/list')">
          <text class="action-icon">🖼️</text>
          <text class="action-text">轮播图管理</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/admin/order/list')">
          <text class="action-icon">📋</text>
          <text class="action-text">订单管理</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/admin/user/list')">
          <text class="action-icon">👤</text>
          <text class="action-text">用户管理</text>
        </view>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="recent-orders">
      <view class="section-header">
        <text class="section-title">最近订单</text>
        <text class="view-all" @click="navigateTo('/pages/admin/order/list')">查看全部 ></text>
      </view>
      <view class="order-list">
        <view class="order-item" v-for="item in recentOrders" :key="item.id">
          <view class="order-info">
            <text class="order-no">{{ item.orderNo }}</text>
            <text class="order-amount">¥{{ item.totalAmount }}</text>
          </view>
          <view class="order-meta">
            <text :class="['order-status', 'status-' + item.status]">{{ getStatusText(item.status) }}</text>
            <text class="order-time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
        <view class="empty" v-if="recentOrders.length === 0 && !loading">
          <text>暂无订单</text>
        </view>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="admin-nav">
      <view class="nav-item active">
        <text>首页</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/product/list')">
        <text>商品</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/category/list')">
        <text>分类</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/order/list')">
        <text>订单</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/user/list')">
        <text>用户</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getAdminStats, getOrderList } from '../../api/admin'

export default {
  data() {
    return {
      stats: {
        orderCount: 0,
        productCount: 0,
        userCount: 0,
        totalRevenue: 0,
        todayOrders: 0,
        todayRevenue: 0,
        todayUsers: 0
      },
      recentOrders: [],
      loading: false
    }
  },
  onShow() {
    // 检查是否已登录
    this.checkLogin()
    this.loadStats()
    this.loadRecentOrders()
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.redirectTo({ url: '/pages/admin/login' })
        return
      }
    },
    async loadStats() {
      try {
        const res = await getAdminStats()
        this.stats = res.data || this.stats
      } catch (e) {
        console.error('加载统计数据失败', e)
      }
    },

    async loadRecentOrders() {
      this.loading = true
      try {
        const res = await getOrderList(null, null)
        this.recentOrders = (res.data || []).slice(0, 5)
      } catch (e) {
        console.error('加载最近订单失败', e)
      } finally {
        this.loading = false
      }
    },

    getStatusText(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '已退款'
      }
      return map[status] || '未知'
    },

    formatTime(time) {
      if (!time) return ''
      // 简单格式化时间
      return time.replace('T', ' ').substring(0, 16)
    },

    navigateTo(url) {
      uni.redirectTo({ url })
    }
  }
}
</script>

<style scoped>
.admin-home {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.page-header {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
  padding: 40rpx 30rpx 60rpx;
  color: #fff;
}

.page-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.welcome-text {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
}

/* 统计卡片 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 24rpx;
  margin-top: -30rpx;
}

.stat-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 16rpx;
}

.order-icon {
  background: #e6f0ff;
}

.product-icon {
  background: #fff0e6;
}

.user-icon {
  background: #e6f7e6;
}

.revenue-icon {
  background: #ffe6e6;
}

.stat-info {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 4rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
}

/* 今日数据 */
.today-stats {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 12rpx;
  padding: 24rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.today-cards {
  display: flex;
  gap: 16rpx;
}

.today-card {
  flex: 1;
  background: #f5f6fa;
  border-radius: 8rpx;
  padding: 20rpx;
  text-align: center;
}

.today-value {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #ff6600;
  margin-bottom: 8rpx;
}

.today-label {
  display: block;
  font-size: 22rpx;
  color: #666;
}

/* 快捷操作 */
.quick-actions {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 12rpx;
  padding: 24rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.action-item {
  text-align: center;
  padding: 16rpx 8rpx;
}

.action-icon {
  display: block;
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.action-text {
  display: block;
  font-size: 22rpx;
  color: #666;
}

/* 最近订单 */
.recent-orders {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 12rpx;
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.view-all {
  font-size: 24rpx;
  color: #ff6600;
}

.order-list {
  max-height: 400rpx;
  overflow-y: auto;
}

.order-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.order-no {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.order-amount {
  font-size: 26rpx;
  color: #ff3300;
  font-weight: bold;
}

.order-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.status-0 {
  background: #fff7e6;
  color: #ff9900;
}

.status-1 {
  background: #e6f7e6;
  color: #07c160;
}

.status-2 {
  background: #e6f0ff;
  color: #1890ff;
}

.status-3 {
  background: #f0f0f0;
  color: #666;
}

.status-4 {
  background: #f5f5f5;
  color: #999;
}

.status-5 {
  background: #ffe6e6;
  color: #ee0a24;
}

.order-time {
  font-size: 22rpx;
  color: #999;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 底部导航 */
.admin-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  border-top: 1rpx solid #eee;
  height: 100rpx;
}

.nav-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #666;
}

.nav-item.active {
  color: #ff6600;
  font-weight: bold;
}
</style>
