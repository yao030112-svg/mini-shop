<template>
  <view class="admin-order">
    <view class="page-header">
      <text class="page-title">订单管理</text>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view
        class="filter-item"
        :class="{ active: currentStatus === null }"
        @click="filterByStatus(null)"
      >
        <text>全部</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: currentStatus === 0 }"
        @click="filterByStatus(0)"
      >
        <text>待支付</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: currentStatus === 1 }"
        @click="filterByStatus(1)"
      >
        <text>已支付</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: currentStatus === 2 }"
        @click="filterByStatus(2)"
      >
        <text>已发货</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: currentStatus === 3 }"
        @click="filterByStatus(3)"
      >
        <text>已完成</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: currentStatus === 5 }"
        @click="filterByStatus(5)"
      >
        <text>已退款</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view class="order-card" v-for="item in orderList" :key="item.id">
        <view class="order-header">
          <text class="order-no">{{ item.orderNo }}</text>
          <text :class="['order-status', 'status-' + item.status]">{{ getStatusText(item.status) }}</text>
        </view>
        <view class="order-body">
          <view class="order-info">
            <text class="info-item">金额: ¥{{ item.totalAmount }}</text>
            <text class="info-item">用户ID: {{ item.userId }}</text>
            <text class="info-item">时间: {{ item.createTime }}</text>
          </view>
        </view>
        <view class="order-footer">
          <button class="action-btn detail-btn" @click="viewOrderDetail(item)">查看详情</button>
          <button
            class="action-btn ship-btn"
            v-if="item.status === 1"
            @click="handleShip(item)"
          >发货</button>
          <button
            class="action-btn refund-btn"
            v-if="item.status === 1 || item.status === 2"
            @click="handleRefund(item)"
          >退款</button>
        </view>
      </view>

      <view class="empty" v-if="orderList.length === 0 && !loading">
        <text>暂无订单</text>
      </view>
    </view>

    <!-- 订单详情弹窗 -->
    <view class="detail-mask" v-if="detailVisible" @click="closeDetail">
      <view class="detail-dialog" @click.stop>
        <view class="detail-header">
          <text class="detail-title">订单详情</text>
          <text class="close-btn" @click="closeDetail">×</text>
        </view>
        <scroll-view class="detail-body" scroll-y>
          <!-- 收货信息 -->
          <view class="detail-section">
            <text class="section-title">收货信息</text>
            <view class="address-info" v-if="addressInfo">
              <view class="address-row">
                <text class="label">收件人：</text>
                <text class="value">{{ addressInfo.receiverName }}</text>
              </view>
              <view class="address-row">
                <text class="label">联系电话：</text>
                <text class="value">{{ addressInfo.phone }}</text>
              </view>
              <view class="address-row">
                <text class="label">收货地址：</text>
                <text class="value">{{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }}{{ addressInfo.detail }}</text>
              </view>
            </view>
            <view class="no-address" v-else>
              <text>暂无收货信息</text>
            </view>
          </view>

          <!-- 商品信息 -->
          <view class="detail-section">
            <text class="section-title">商品信息</text>
            <view class="product-item" v-for="product in orderDetail.items" :key="product.id">
              <image class="product-image" :src="product.image || '/static/images/placeholder.png'" mode="aspectFill"></image>
              <view class="product-info">
                <text class="product-name">{{ product.productName }}</text>
                <text class="product-spec" v-if="product.skuSpecDesc">{{ product.skuSpecDesc }}</text>
                <view class="product-meta">
                  <text class="product-price">¥{{ product.price }}</text>
                  <text class="product-quantity">x{{ product.quantity }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 订单信息 -->
          <view class="detail-section">
            <text class="section-title">订单信息</text>
            <view class="order-info-detail">
              <view class="info-row">
                <text class="label">订单编号：</text>
                <text class="value">{{ orderDetail.order.orderNo }}</text>
              </view>
              <view class="info-row">
                <text class="label">订单状态：</text>
                <text class="value" :class="'status-' + orderDetail.order.status">{{ getStatusText(orderDetail.order.status) }}</text>
              </view>
              <view class="info-row total">
                <text class="label">订单金额：</text>
                <text class="value price">¥{{ orderDetail.order.totalAmount }}</text>
              </view>
              <view class="info-row">
                <text class="label">下单时间：</text>
                <text class="value">{{ orderDetail.order.createTime }}</text>
              </view>
              <view class="info-row" v-if="orderDetail.order.paymentTime">
                <text class="label">支付时间：</text>
                <text class="value">{{ orderDetail.order.paymentTime }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
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
      <view class="nav-item active">
        <text>订单</text>
      </view>
      <view class="nav-item" @click="navigateTo('/pages/admin/user/list')">
        <text>用户</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderList, shipOrder, refundOrder, getOrderDetail } from '../../../api/admin'

export default {
  data() {
    return {
      orderList: [],
      currentStatus: null,
      loading: false,
      detailVisible: false,
      orderDetail: {
        order: {},
        items: []
      },
      addressInfo: null
    }
  },
  onShow() {
    this.checkLogin()
    this.loadOrders()
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.redirectTo({ url: '/pages/admin/login' })
        return
      }
    },
    async loadOrders() {
      this.loading = true
      try {
        const res = await getOrderList(this.currentStatus, null)
        this.orderList = res.data || []
      } catch (e) {
        console.error('加载订单失败', e)
      } finally {
        this.loading = false
      }
    },

    filterByStatus(status) {
      this.currentStatus = status
      this.loadOrders()
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

    handleShip(item) {
      uni.showModal({
        title: '确认发货',
        content: `确定对订单 ${item.orderNo} 执行发货操作吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await shipOrder(item.id)
              uni.showToast({ title: '发货成功', icon: 'success' })
              this.loadOrders()
            } catch (e) {
              console.error('发货失败', e)
            }
          }
        }
      })
    },

    handleRefund(item) {
      uni.showModal({
        title: '确认退款',
        content: `确定对订单 ${item.orderNo} 执行退款操作吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await refundOrder(item.id)
              uni.showToast({ title: '退款成功', icon: 'success' })
              this.loadOrders()
            } catch (e) {
              console.error('退款失败', e)
            }
          }
        }
      })
    },

    navigateTo(url) {
      uni.redirectTo({ url })
    },

    async viewOrderDetail(item) {
      try {
        uni.showLoading({ title: '加载中' })
        const res = await getOrderDetail(item.id)
        this.orderDetail = res.data
        
        // 解析地址快照
        if (this.orderDetail && this.orderDetail.order && this.orderDetail.order.addressSnapshot) {
          try {
            this.addressInfo = JSON.parse(this.orderDetail.order.addressSnapshot)
          } catch (e) {
            this.addressInfo = null
          }
        } else {
          this.addressInfo = null
        }
        
        this.detailVisible = true
        uni.hideLoading()
      } catch (e) {
        console.error('加载订单详情失败', e)
        uni.hideLoading()
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },

    closeDetail() {
      this.detailVisible = false
      this.orderDetail = { order: {}, items: [] }
      this.addressInfo = null
    }
  }
}
</script>

<style scoped>
.admin-order {
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

.filter-bar {
  display: flex;
  background: #fff;
  padding: 16rpx 24rpx;
  margin-bottom: 16rpx;
  overflow-x: auto;
}

.filter-item {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  color: #666;
  border-radius: 24rpx;
  margin-right: 16rpx;
  background: #f5f5f5;
}

.filter-item.active {
  background: #ff6600;
  color: #fff;
}

.order-list {
  padding: 0 24rpx;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.order-no {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.order-status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
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

.order-body {
  margin-bottom: 16rpx;
}

.order-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.info-item {
  font-size: 24rpx;
  color: #666;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
}

.action-btn {
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
  border-radius: 6rpx;
  padding: 0 32rpx;
}

.ship-btn {
  background: #1890ff;
  color: #fff;
  border: none;
}

.refund-btn {
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

/* 订单详情弹窗样式 */
.detail-mask {
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

.detail-dialog {
  width: 90%;
  max-width: 700rpx;
  max-height: 85vh;
  background: #fff;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
  padding: 0 8rpx;
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx;
}

.detail-section {
  margin-bottom: 32rpx;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.address-info {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
}

.address-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 26rpx;
}

.address-row:last-child {
  margin-bottom: 0;
}

.address-row .label {
  color: #666;
  min-width: 140rpx;
}

.address-row .value {
  color: #333;
  flex: 1;
}

.no-address {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 26rpx;
}

.product-item {
  display: flex;
  padding: 16rpx 0;
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
  justify-content: space-between;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-price {
  font-size: 28rpx;
  color: #ff6600;
  font-weight: bold;
}

.product-quantity {
  font-size: 24rpx;
  color: #666;
}

.order-info-detail {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
}

.info-row {
  display: flex;
  margin-bottom: 16rpx;
  font-size: 26rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #666;
  min-width: 160rpx;
}

.info-row .value {
  color: #333;
  flex: 1;
}

.info-row.total {
  padding-top: 16rpx;
  margin-top: 16rpx;
  border-top: 1rpx solid #e0e0e0;
}

.info-row .value.price {
  color: #ff6600;
  font-size: 32rpx;
  font-weight: bold;
}

.info-row .value.status-0 { color: #ff9800; }
.info-row .value.status-1 { color: #1890ff; }
.info-row .value.status-2 { color: #52c41a; }
.info-row .value.status-3 { color: #52c41a; }
.info-row .value.status-4 { color: #999; }
.info-row .value.status-5 { color: #ff4d4f; }
</style>
