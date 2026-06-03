<template>
  <view class="admin-product">
    <view class="page-header">
      <text class="page-title">商品管理</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="keyword"
        placeholder="搜索商品名称"
        placeholder-class="placeholder"
        @confirm="loadProducts"
      />
      <button class="search-btn" @click="loadProducts">搜索</button>
    </view>

    <!-- 商品列表 -->
    <view class="product-list">
      <view class="product-card" v-for="item in productList" :key="item.id">
        <image class="product-image" :src="item.mainImage" mode="aspectFill" />
        <view class="product-info">
          <text class="product-name">{{ item.name }}</text>
          <text class="product-price">¥{{ item.minPrice }}</text>
          <view class="product-status">
            <text :class="['status-tag', item.status === 1 ? 'on' : 'off']">
              {{ item.status === 1 ? '上架中' : '已下架' }}
            </text>
          </view>
        </view>
        <view class="product-actions">
          <switch
            :checked="item.status === 1"
            color="#ff6600"
            @change="handleStatusChange(item, $event)"
          />
          <button class="delete-btn" @click="handleDelete(item)">删除</button>
        </view>
      </view>

      <view class="empty" v-if="productList.length === 0 && !loading">
        <text>暂无商品</text>
      </view>
    </view>

    <!-- 分页 -->
    <view class="pagination" v-if="productList.length > 0">
      <button class="page-btn" :disabled="page <= 1" @click="prevPage">上一页</button>
      <text class="page-info">第 {{ page }} 页</text>
      <button class="page-btn" :disabled="productList.length < pageSize" @click="nextPage">下一页</button>
    </view>

    <!-- 底部导航 -->
    <view class="admin-nav">
      <view class="nav-item" @click="navigateTo('/pages/admin/index')">
        <text>首页</text>
      </view>
      <view class="nav-item active">
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
import { getProductList, deleteProduct, updateProductStatus } from '../../../api/admin'

export default {
  data() {
    return {
      productList: [],
      keyword: '',
      page: 1,
      pageSize: 20,
      loading: false
    }
  },
  onShow() {
    this.checkLogin()
    this.loadProducts()
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.redirectTo({ url: '/pages/admin/login' })
        return
      }
    },
    async loadProducts() {
      this.loading = true
      try {
        const res = await getProductList(this.page, this.pageSize)
        let list = res.data.records || res.data.list || res.data || []
        // 前端关键词过滤
        if (this.keyword.trim()) {
          list = list.filter(item => item.name.includes(this.keyword.trim()))
        }
        this.productList = list
      } catch (e) {
        console.error('加载商品失败', e)
      } finally {
        this.loading = false
      }
    },

    async handleStatusChange(item, e) {
      const newStatus = e.detail.value ? 1 : 0
      try {
        await updateProductStatus(item.id, newStatus)
        item.status = newStatus
        uni.showToast({ title: newStatus === 1 ? '已上架' : '已下架', icon: 'success' })
      } catch (e) {
        console.error('状态更新失败', e)
        // 恢复开关状态
        this.loadProducts()
      }
    },

    handleDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除商品"${item.name}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await deleteProduct(item.id)
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadProducts()
            } catch (e) {
              console.error('删除失败', e)
            }
          }
        }
      })
    },

    prevPage() {
      if (this.page > 1) {
        this.page--
        this.loadProducts()
      }
    },

    nextPage() {
      this.page++
      this.loadProducts()
    },

    navigateTo(url) {
      uni.redirectTo({ url })
    }
  }
}
</script>

<style scoped>
.admin-product {
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

.product-list {
  padding: 0 24rpx;
}

.product-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  margin-left: 20rpx;
}

.product-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300rpx;
}

.product-price {
  display: block;
  font-size: 28rpx;
  color: #ff3300;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.status-tag.on {
  background: #e6f7e6;
  color: #07c160;
}

.status-tag.off {
  background: #fff0e6;
  color: #ff6600;
}

.product-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.delete-btn {
  width: 100rpx;
  height: 48rpx;
  line-height: 48rpx;
  font-size: 22rpx;
  color: #ee0a24;
  border: 1rpx solid #ee0a24;
  border-radius: 6rpx;
  background: #fff;
  padding: 0;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  gap: 24rpx;
}

.page-btn {
  width: 160rpx;
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
  background: #fff;
  border: 1rpx solid #ddd;
  border-radius: 6rpx;
  padding: 0;
}

.page-btn[disabled] {
  color: #ccc;
  border-color: #eee;
}

.page-info {
  font-size: 26rpx;
  color: #666;
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
