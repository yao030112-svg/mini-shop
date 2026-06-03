<template>
  <view class="admin-user">
    <view class="page-header">
      <text class="page-title">用户管理</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="keyword"
        placeholder="搜索用户昵称"
        placeholder-class="placeholder"
        @confirm="filterUsers"
      />
      <button class="search-btn" @click="filterUsers">搜索</button>
    </view>

    <!-- 用户列表 -->
    <view class="user-list">
      <view class="user-card" v-for="item in filteredList" :key="item.id">
        <view class="user-info">
          <image
            class="user-avatar"
            :src="item.avatarUrl || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="user-detail">
            <text class="user-name">{{ item.nickname || '未设置昵称' }}</text>
            <text class="user-id">ID: {{ item.id }}</text>
            <text :class="['user-status', item.status === 1 ? 'normal' : 'disabled']">
              {{ item.status === 1 ? '正常' : '已禁用' }}
            </text>
          </view>
        </view>
        <view class="user-actions">
          <button
            v-if="item.status === 1"
            class="action-btn disable-btn"
            @click="handleDisable(item)"
          >禁用</button>
          <button
            v-else
            class="action-btn enable-btn"
            @click="handleEnable(item)"
          >启用</button>
        </view>
      </view>

      <view class="empty" v-if="filteredList.length === 0 && !loading">
        <text>暂无用户</text>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="admin-nav">
      <view class="nav-item" @click="navigateTo('/pages/admin/product/list')">
        <text>商品</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/category/list')">
        <text>分类</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/order/list')">
        <text>订单</text>
      </view>
      <view class="nav-item active">
        <text>用户</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserList, disableUser, enableUser } from '../../../api/admin'

export default {
  data() {
    return {
      userList: [],
      keyword: '',
      loading: false
    }
  },
  computed: {
    filteredList() {
      if (!this.keyword.trim()) {
        return this.userList
      }
      return this.userList.filter(item =>
        (item.nickname || '').includes(this.keyword.trim())
      )
    }
  },
  onShow() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      try {
        const res = await getUserList()
        this.userList = res.data || []
      } catch (e) {
        console.error('加载用户失败', e)
      } finally {
        this.loading = false
      }
    },

    filterUsers() {
      // 使用 computed 属性自动过滤，此方法用于触发搜索
    },

    handleDisable(item) {
      uni.showModal({
        title: '确认禁用',
        content: `确定要禁用用户"${item.nickname || item.id}"吗？禁用后该用户将无法使用系统。`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await disableUser(item.id)
              uni.showToast({ title: '已禁用', icon: 'success' })
              this.loadUsers()
            } catch (e) {
              console.error('禁用失败', e)
            }
          }
        }
      })
    },

    handleEnable(item) {
      uni.showModal({
        title: '确认启用',
        content: `确定要启用用户"${item.nickname || item.id}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await enableUser(item.id)
              uni.showToast({ title: '已启用', icon: 'success' })
              this.loadUsers()
            } catch (e) {
              console.error('启用失败', e)
            }
          }
        }
      })
    },

    navigateTo(url) {
      uni.redirectTo({ url })
    }
  }
}
</script>

<style scoped>
.admin-user {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.page-header {
  background: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  margin-bottom: 16rpx;
}

.search-input {
  flex: 1;
  height: 64rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.search-btn {
  width: 120rpx;
  height: 64rpx;
  line-height: 64rpx;
  background: #ff6600;
  color: #fff;
  font-size: 26rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
  padding: 0;
}

.user-list {
  padding: 0 24rpx;
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f5f5f5;
}

.user-detail {
  margin-left: 20rpx;
}

.user-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 6rpx;
}

.user-id {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.user-status {
  font-size: 22rpx;
  padding: 2rpx 12rpx;
  border-radius: 4rpx;
}

.user-status.normal {
  background: #e6f7e6;
  color: #07c160;
}

.user-status.disabled {
  background: #ffe6e6;
  color: #ee0a24;
}

.user-actions {
  flex-shrink: 0;
}

.action-btn {
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
  border-radius: 6rpx;
  padding: 0 24rpx;
}

.disable-btn {
  background: #fff;
  color: #ee0a24;
  border: 1rpx solid #ee0a24;
}

.enable-btn {
  background: #07c160;
  color: #fff;
  border: none;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.placeholder {
  color: #ccc;
}

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
