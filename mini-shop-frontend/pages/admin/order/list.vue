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
    <view class="dialog-mask" v-if="detailVisible" @click="closeDetail">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">订单详情</text>
          <text class="close-btn" @click="closeDetail">×</text>
        </view>
        
        <scroll-view class="dialog-body" scroll-y>
          <!-- 收货地址 -->
          <view class="detail-section" v-if="addressInfo">
            <text class="section-title">📍 收货信息</text>
            <view class="address-detail">
              <text class="address-name">{{ addressInfo.receiverName }}</text>
              <text class="address-phone">{{ addressInfo.phone }}</text>
              <text class="address-full">{{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }}{{ addressInfo.detail }}</text>
            </view>
          </view>

          <!-- 商品信息 -->
          <view class="detail-section" v-if="orderDetail && orderDetail.items">
            <text class="section-title">🛒 商品信息</text>
            <view class="goods-list">
              <view class="goods-item" v-for="goods in orderDetail.items" :key="goods.id">
                <image class="goods-image" :src="goods.image" mode="aspectFill" />
                <view class="goods-info">
                  <text class="goods-name">{{ goods.productName }}</text>
                  <text class="goods-spec" v-if="goods.skuSpecDesc">{{ goods.skuSpecDesc }}</text>
                  <view class="goods-bottom">
                    <text class="goods-price">¥{{ goods.price }}</text>
                    <text class="goods-qty">x{{ goods.quantity }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 订单信息 -->
          <view class="detail-section" v-if="orderDetail && orderDetail.order">
            <text class="section-title">📋 订单信息</text>
            <view class="order-info-detail">
              <view class="info-row">
                <text class="info-label">订单编号：</text>
                <text class="info-value">{{ orderDetail.order.orderNo }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">订单状态：</text>
                <text class="info-value">{{ getStatusText(orderDetail.order.status) }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">订单金额：</text>
                <text class="info-value price">¥{{ orderDetail.order.totalAmount }}</text>
              </view>
              <view class="info-row" v-if="orderDetail.order.paymentTime">
                <text class="info-label">支付时间：</text>
                <text class="info-value">{{ orderDetail.order.paymentTime }}</text>
              </view>
              <view class="info-row" v-if="orderDetail.order.shippingTime">
                <text class="info-label">发货时间：</text>
                <text class="info-value">{{ orderDetail.order.shippingTime }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">创建时间：</text>
                <text class="info-value">{{ orderDetail.order.createTime }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
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
import { getOrderList, getOrderDetail, shipOrder, refundOrder } from '../../../api/admin'

export default {
  data() {
    return {
      orderList: [],
      currentStatus: null,
      loading: false,
      // 订单详情弹窗
      detailVisible: false,
      orderDetail: null,
      addressInfo: null
    }
  },
  onShow() {
    this.loadOrders()
  },
  methods: {
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

    // 查看订单详情
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
        }
        
        this.detailVisible = true
        uni.hideLoading()
      } catch (e) {
        console.error('加载订单详情失败', e)
        uni.hideLoading()
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },

    // 关闭详情弹窗
    closeDetail() {
      this.detailVisible = false
      this.orderDetail = null
      this.addressInfo = null
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

.detail-btn {
  background: #e6f0ff;
  color: #1890ff;
  border: 1rpx solid #1890ff;
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

/* 订单详情弹窗 */
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

.dialog-content {
  width: 90%;
  max-height: 80vh;
  background: #fff;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.dialog-body {
  flex: 1;
  padding: 24rpx;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.address-detail {
  background: #f8f8f8;
  border-radius: 8rpx;
  padding: 20rpx;
}

.address-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.address-phone {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.address-full {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.goods-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.goods-item {
  display: flex;
  background: #f8f8f8;
  border-radius: 8rpx;
  padding: 16rpx;
}

.goods-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 26rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-spec {
  font-size: 22rpx;
  color: #999;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 26rpx;
  color: #ff6600;
  font-weight: bold;
}

.goods-qty {
  font-size: 24rpx;
  color: #666;
}

.order-info-detail {
  background: #f8f8f8;
  border-radius: 8rpx;
  padding: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #eee;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 26rpx;
  color: #666;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.info-value.price {
  color: #ff6600;
  font-weight: bold;
}
</style>
