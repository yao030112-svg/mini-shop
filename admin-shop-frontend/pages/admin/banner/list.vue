<template>
  <view class="admin-banner">
    <view class="page-header">
      <text class="page-title">轮播图管理</text>
      <button class="add-btn" @click="showAddDialog">添加轮播图</button>
    </view>

    <!-- 轮播图列表 -->
    <view class="banner-list">
      <view class="banner-item" v-for="item in bannerList" :key="item.id">
        <view class="banner-preview">
          <image class="banner-image" :src="item.imageUrl || '/static/images/placeholder.png'" mode="aspectFill"></image>
        </view>
        <view class="banner-info">
          <text class="banner-title">{{ item.title || '未命名' }}</text>
          <view class="banner-meta">
            <text class="banner-status" :class="item.status === 1 ? 'active' : 'inactive'">
              {{ item.status === 1 ? '启用' : '禁用' }}
            </text>
            <text class="banner-sort">排序: {{ item.sortOrder || 0 }}</text>
          </view>
        </view>
        <view class="banner-actions">
          <button class="action-btn status-btn" 
                  :class="item.status === 1 ? 'disable' : 'enable'"
                  @click="handleToggleStatus(item)">
            {{ item.status === 1 ? '禁用' : '启用' }}
          </button>
          <button class="action-btn edit-btn" @click="showEditDialog(item)">编辑</button>
          <button class="action-btn del-btn" @click="handleDelete(item)">删除</button>
        </view>
      </view>

      <view class="empty" v-if="bannerList.length === 0 && !loading">
        <text>暂无轮播图</text>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view class="dialog-mask" v-if="dialogVisible" @click="closeDialog">
      <view class="dialog" @click.stop>
        <text class="dialog-title">{{ isEdit ? '编辑轮播图' : '添加轮播图' }}</text>
        <view class="dialog-body">
          <view class="form-item">
            <text class="label">标题</text>
            <input
              class="input"
              v-model="formData.title"
              placeholder="请输入轮播图标题"
              placeholder-class="placeholder"
            />
          </view>
          <view class="form-item">
            <text class="label">图片URL</text>
            <input
              class="input"
              v-model="formData.imageUrl"
              placeholder="请输入图片URL"
              placeholder-class="placeholder"
            />
          </view>
          <view class="form-item">
            <text class="label">跳转链接（可选）</text>
            <input
              class="input"
              v-model="formData.linkUrl"
              placeholder="请输入跳转链接"
              placeholder-class="placeholder"
            />
          </view>
          <view class="form-item">
            <text class="label">排序值（越大越靠前）</text>
            <input
              class="input"
              type="number"
              v-model="formData.sortOrder"
              placeholder="请输入排序值"
              placeholder-class="placeholder"
            />
          </view>
        </view>
        <view class="dialog-footer">
          <button class="cancel-btn" @click="closeDialog">取消</button>
          <button class="confirm-btn" @click="handleSubmit">确定</button>
        </view>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="admin-nav">
      <view class="nav-item" @click="navigateTo('/pages/admin/index')">
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
import { getBannerList, addBanner, updateBanner, deleteBanner, updateBannerStatus } from '../../../api/admin'

export default {
  data() {
    return {
      bannerList: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      formData: {
        id: null,
        title: '',
        imageUrl: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      }
    }
  },
  onShow() {
    this.checkLogin()
    this.loadBanners()
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.redirectTo({ url: '/pages/admin/login' })
        return
      }
    },
    async loadBanners() {
      this.loading = true
      try {
        const res = await getBannerList()
        this.bannerList = res.data || []
      } catch (e) {
        console.error('加载轮播图失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    showAddDialog() {
      this.isEdit = false
      this.formData = { id: null, title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 }
      this.dialogVisible = true
    },

    showEditDialog(item) {
      this.isEdit = true
      this.formData = {
        id: item.id,
        title: item.title || '',
        imageUrl: item.imageUrl || '',
        linkUrl: item.linkUrl || '',
        sortOrder: item.sortOrder || 0,
        status: item.status
      }
      this.dialogVisible = true
    },

    closeDialog() {
      this.dialogVisible = false
    },

    async handleSubmit() {
      if (!this.formData.imageUrl.trim()) {
        uni.showToast({ title: '请输入图片URL', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        if (this.isEdit) {
          await updateBanner(this.formData)
          uni.hideLoading()
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await addBanner(this.formData)
          uni.hideLoading()
          uni.showToast({ title: '添加成功', icon: 'success' })
        }
        this.closeDialog()
        this.loadBanners()
      } catch (e) {
        uni.hideLoading()
        console.error('操作失败', e)
      }
    },

    async handleToggleStatus(item) {
      const newStatus = item.status === 1 ? 0 : 1
      try {
        await updateBannerStatus(item.id, newStatus)
        uni.showToast({ title: newStatus === 1 ? '已启用' : '已禁用', icon: 'success' })
        this.loadBanners()
      } catch (e) {
        console.error('状态切换失败', e)
      }
    },

    handleDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除轮播图"${item.title || '未命名'}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await deleteBanner(item.id)
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadBanners()
            } catch (e) {
              console.error('删除失败', e)
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
.admin-banner {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.add-btn {
  width: 160rpx;
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
  background: #ff6600;
  color: #fff;
  border-radius: 8rpx;
  padding: 0;
}

.banner-list {
  padding: 24rpx;
}

.banner-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.banner-preview {
  width: 100%;
  height: 300rpx;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-info {
  margin-bottom: 16rpx;
}

.banner-title {
  display: block;
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.banner-status {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.banner-status.active {
  background: #e6f7ee;
  color: #52c41a;
}

.banner-status.inactive {
  background: #fff1f0;
  color: #ff4d4f;
}

.banner-sort {
  font-size: 24rpx;
  color: #999;
}

.banner-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  flex: 1;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 24rpx;
  border-radius: 6rpx;
  padding: 0;
}

.status-btn.enable {
  background: #e6f7ee;
  color: #52c41a;
  border: 1rpx solid #52c41a;
}

.status-btn.disable {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1rpx solid #ff4d4f;
}

.edit-btn {
  background: #e6f0ff;
  color: #1890ff;
  border: 1rpx solid #1890ff;
}

.del-btn {
  background: #fff;
  color: #ee0a24;
  border: 1rpx solid #ee0a24;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

/* 弹窗样式 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.dialog {
  width: 600rpx;
  max-height: 80vh;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  overflow-y: auto;
}

.dialog-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 32rpx;
}

.dialog-body .form-item {
  margin-bottom: 24rpx;
}

.dialog-body .label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.dialog-body .input {
  width: 100%;
  height: 72rpx;
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.dialog-footer {
  display: flex;
  gap: 24rpx;
  margin-top: 32rpx;
}

.cancel-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;
  background: #f5f5f5;
  color: #666;
  border-radius: 8rpx;
  border: none;
}

.confirm-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;
  background: #ff6600;
  color: #fff;
  border-radius: 8rpx;
  border: none;
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
