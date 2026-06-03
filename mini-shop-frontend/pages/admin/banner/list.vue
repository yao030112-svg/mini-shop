<template>
  <view class="admin-banner">
    <view class="page-header">
      <text class="page-title">轮播图管理</text>
      <button class="add-btn" @click="showAddDialog">添加轮播图</button>
    </view>

    <!-- 轮播图列表 -->
    <view class="banner-list">
      <view class="banner-item" v-for="(item, index) in bannerList" :key="item.id">
        <view class="banner-image-wrapper">
          <image class="banner-image" :src="item.imageUrl" mode="aspectFill" />
        </view>
        <view class="banner-info">
          <text class="banner-title">排序: {{ item.sortOrder || 0 }}</text>
          <text class="banner-status" :class="item.status === 1 ? 'status-enabled' : 'status-disabled'">
            {{ item.status === 1 ? '启用' : '禁用' }}
          </text>
          <text class="banner-link" v-if="item.linkUrl">链接: {{ item.linkUrl }}</text>
        </view>
        <view class="banner-actions">
          <button class="action-btn toggle-btn" @click="handleToggleStatus(item)">
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
            <text class="label">排序值</text>
            <input
              class="input"
              type="number"
              v-model="formData.sortOrder"
              placeholder="请输入排序值"
              placeholder-class="placeholder"
            />
          </view>
          <view class="form-item">
            <text class="label">状态</text>
            <view class="radio-group">
              <view class="radio-item" :class="{ active: formData.status === 1 }" @click="formData.status = 1">
                <text>启用</text>
              </view>
              <view class="radio-item" :class="{ active: formData.status === 0 }" @click="formData.status = 0">
                <text>禁用</text>
              </view>
            </view>
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
      <view class="nav-item active">
        <text>轮播图</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getBannerList, addBanner, updateBanner, deleteBanner, toggleBannerStatus } from '../../../api/admin'

export default {
  data() {
    return {
      bannerList: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      formData: {
        id: null,
        imageUrl: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      }
    }
  },
  onShow() {
    this.loadBanners()
  },
  methods: {
    async loadBanners() {
      this.loading = true
      try {
        const res = await getBannerList()
        this.bannerList = res.data || []
      } catch (e) {
        console.error('加载轮播图失败', e)
      } finally {
        this.loading = false
      }
    },

    showAddDialog() {
      this.isEdit = false
      this.formData = { id: null, imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 }
      this.dialogVisible = true
    },

    showEditDialog(item) {
      this.isEdit = true
      this.formData = {
        id: item.id,
        imageUrl: item.imageUrl,
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
        if (this.isEdit) {
          await updateBanner(this.formData)
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await addBanner(this.formData)
          uni.showToast({ title: '添加成功', icon: 'success' })
        }
        this.closeDialog()
        this.loadBanners()
      } catch (e) {
        console.error('操作失败', e)
      }
    },

    handleToggleStatus(item) {
      const newStatus = item.status === 1 ? 0 : 1
      uni.showModal({
        title: '确认操作',
        content: `确定要${newStatus === 1 ? '启用' : '禁用'}该轮播图吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await toggleBannerStatus(item.id, newStatus)
              uni.showToast({ title: '操作成功', icon: 'success' })
              this.loadBanners()
            } catch (e) {
              console.error('操作失败', e)
            }
          }
        }
      })
    },

    handleDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除该轮播图吗？',
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

.banner-image-wrapper {
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
  font-size: 26rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.banner-status {
  display: inline-block;
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.status-enabled {
  background: #e6f7e6;
  color: #07c160;
}

.status-disabled {
  background: #f5f5f5;
  color: #999;
}

.banner-link {
  display: block;
  font-size: 24rpx;
  color: #1890ff;
  margin-top: 8rpx;
}

.banner-actions {
  display: flex;
  gap: 12rpx;
  justify-content: flex-end;
}

.action-btn {
  height: 48rpx;
  line-height: 48rpx;
  font-size: 22rpx;
  border-radius: 6rpx;
  padding: 0 16rpx;
  min-width: auto;
}

.toggle-btn {
  background: #e6f0ff;
  color: #1890ff;
  border: 1rpx solid #1890ff;
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
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  max-height: 80vh;
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

.radio-group {
  display: flex;
  gap: 24rpx;
}

.radio-item {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #666;
}

.radio-item.active {
  background: #ff6600;
  color: #fff;
  border-color: #ff6600;
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
