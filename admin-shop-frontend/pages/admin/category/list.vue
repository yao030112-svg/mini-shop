<template>
  <view class="admin-category">
    <view class="page-header">
      <text class="page-title">分类管理</text>
      <button class="add-btn" @click="showAddDialog">添加分类</button>
    </view>

    <!-- 分类列表 -->
    <view class="category-list">
      <view class="category-item" v-for="(item, index) in categoryList" :key="item.id">
        <view class="category-header" @click="toggleProducts(item)">
          <view class="category-info">
            <text class="category-name">{{ item.name }}</text>
            <text class="category-sort">排序: {{ item.sortOrder || 0 }}</text>
          </view>
          <view class="category-actions">
            <button class="action-btn sort-btn" @click.stop="handleSortUp(item, index)">↑</button>
            <button class="action-btn sort-btn" @click.stop="handleSortDown(item, index)">↓</button>
            <button class="action-btn edit-btn" @click.stop="showEditDialog(item)">编辑</button>
            <button class="action-btn del-btn" @click.stop="handleDelete(item)">删除</button>
          </view>
        </view>
        
        <!-- 商品列表 -->
        <view class="product-list" v-if="expandedCategoryId === item.id">
          <view class="product-item" v-for="product in currentProducts" :key="product.id">
            <image class="product-image" :src="product.mainImage || '/static/images/placeholder.png'" mode="aspectFill"></image>
            <view class="product-info">
              <text class="product-name">{{ product.name }}</text>
              <text class="product-price">¥{{ product.minPrice }}</text>
            </view>
            <view class="product-status" :class="product.status === 1 ? 'on-sale' : 'off-sale'">
              {{ product.status === 1 ? '上架中' : '已下架' }}
            </view>
          </view>
          <view class="empty-product" v-if="currentProducts.length === 0">
            <text>该分类下暂无商品</text>
          </view>
        </view>
      </view>

      <view class="empty" v-if="categoryList.length === 0 && !loading">
        <text>暂无分类</text>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view class="dialog-mask" v-if="dialogVisible" @click="closeDialog">
      <view class="dialog" @click.stop>
        <text class="dialog-title">{{ isEdit ? '编辑分类' : '添加分类' }}</text>
        <view class="dialog-body">
          <view class="form-item">
            <text class="label">分类名称</text>
            <input
              class="input"
              v-model="formData.name"
              placeholder="请输入分类名称"
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
      <view class="nav-item active">
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
import { getCategoryList, addCategory, updateCategory, deleteCategory, getCategoryProducts } from '../../../api/admin'

export default {
  data() {
    return {
      categoryList: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      expandedCategoryId: null,
      currentProducts: [],
      formData: {
        id: null,
        name: '',
        sortOrder: 0
      }
    }
  },
  onShow() {
    this.checkLogin()
    this.loadCategories()
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.redirectTo({ url: '/pages/admin/login' })
        return
      }
    },
    async loadCategories() {
      this.loading = true
      try {
        const res = await getCategoryList()
        this.categoryList = res.data || []
      } catch (e) {
        console.error('加载分类失败', e)
      } finally {
        this.loading = false
      }
    },

    showAddDialog() {
      this.isEdit = false
      this.formData = { id: null, name: '', sortOrder: 0 }
      this.dialogVisible = true
    },

    showEditDialog(item) {
      this.isEdit = true
      this.formData = {
        id: item.id,
        name: item.name,
        sortOrder: item.sortOrder || 0
      }
      this.dialogVisible = true
    },

    closeDialog() {
      this.dialogVisible = false
    },

    async handleSubmit() {
      if (!this.formData.name.trim()) {
        uni.showToast({ title: '请输入分类名称', icon: 'none' })
        return
      }

      try {
        if (this.isEdit) {
          await updateCategory(this.formData)
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await addCategory(this.formData)
          uni.showToast({ title: '添加成功', icon: 'success' })
        }
        this.closeDialog()
        this.loadCategories()
      } catch (e) {
        console.error('操作失败', e)
      }
    },

    handleDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除分类"${item.name}"吗？如果该分类下有商品将无法删除。`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await deleteCategory(item.id)
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadCategories()
            } catch (e) {
              console.error('删除失败', e)
            }
          }
        }
      })
    },

    async handleSortUp(item, index) {
      if (index === 0) return
      const newSort = (item.sortOrder || 0) + 1
      try {
        await updateCategory({ id: item.id, name: item.name, sortOrder: newSort })
        this.loadCategories()
      } catch (e) {
        console.error('排序调整失败', e)
      }
    },

    async handleSortDown(item, index) {
      if (index === this.categoryList.length - 1) return
      const newSort = Math.max(0, (item.sortOrder || 0) - 1)
      try {
        await updateCategory({ id: item.id, name: item.name, sortOrder: newSort })
        this.loadCategories()
      } catch (e) {
        console.error('排序调整失败', e)
      }
    },

    navigateTo(url) {
      uni.redirectTo({ url })
    },

    async toggleProducts(item) {
      if (this.expandedCategoryId === item.id) {
        // 收起
        this.expandedCategoryId = null
        this.currentProducts = []
      } else {
        // 展开并加载商品
        this.expandedCategoryId = item.id
        try {
          const res = await getCategoryProducts(item.id)
          this.currentProducts = res.data || []
        } catch (e) {
          console.error('加载商品失败', e)
          this.currentProducts = []
        }
      }
    }
  }
}
</script>

<style scoped>
.admin-category {
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

.category-list {
  padding: 24rpx;
}

.category-item {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
}

.category-info {
  flex: 1;
}

.category-name {
  display: block;
  font-size: 30rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.category-sort {
  font-size: 24rpx;
  color: #999;
}

.category-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

/* 商品列表样式 */
.product-list {
  padding: 0 24rpx 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 26rpx;
  color: #ff6600;
  font-weight: bold;
}

.product-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  flex-shrink: 0;
}

.product-status.on-sale {
  background: #e6f7ee;
  color: #52c41a;
}

.product-status.off-sale {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty-product {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

.action-btn {
  height: 48rpx;
  line-height: 48rpx;
  font-size: 22rpx;
  border-radius: 6rpx;
  padding: 0 16rpx;
  min-width: auto;
}

.sort-btn {
  background: #f5f5f5;
  color: #666;
  border: 1rpx solid #ddd;
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
