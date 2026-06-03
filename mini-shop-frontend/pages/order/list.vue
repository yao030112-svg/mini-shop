<template>
  <view class="order-list">
    <!-- 状态 Tabs -->
    <view class="tabs">
      <view
        class="tabs__item"
        :class="{ 'tabs__item--active': currentTab === index }"
        v-for="(tab, index) in tabs"
        :key="index"
        @click="switchTab(index)"
      >
        <text class="tabs__text">{{ tab.name }}</text>
        <view v-if="currentTab === index" class="tabs__line"></view>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="list-content">
      <view v-if="orderList.length === 0" class="empty">
        <text class="empty__text">暂无订单</text>
      </view>

      <view
        class="order-card"
        v-for="order in orderList"
        :key="order.id"
        @click="goDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="order-card__header">
          <text class="order-card__no">订单号：{{ order.orderNo }}</text>
          <text class="order-card__status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
        </view>

        <!-- 商品列表 -->
        <view class="order-card__goods">
          <view class="order-card__goods-item" v-for="item in order.items" :key="item.id">
            <image class="order-card__goods-image" :src="item.image" mode="aspectFill" />
            <view class="order-card__goods-info">
              <text class="order-card__goods-name">{{ item.productName }}</text>
              <text class="order-card__goods-spec" v-if="item.skuSpecDesc">{{ item.skuSpecDesc }}</text>
              <view class="order-card__goods-bottom">
                <text class="order-card__goods-price">¥{{ item.price }}</text>
                <text class="order-card__goods-qty">x{{ item.quantity }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 订单底部 -->
        <view class="order-card__footer">
          <text class="order-card__total">合计：<text class="order-card__total-price">¥{{ order.totalAmount }}</text></text>
          <view class="order-card__actions" v-if="order.status === 0">
            <view class="order-card__btn order-card__btn--cancel" @click.stop="handleCancel(order.id)">
              <text class="order-card__btn-text">取消订单</text>
            </view>
            <view class="order-card__btn order-card__btn--pay" @click.stop="handlePay(order.id, order.totalAmount)">
              <text class="order-card__btn-text order-card__btn-text--white">去支付</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderList, cancelOrder, payOrder } from '@/api/order'

export default {
  data() {
    return {
      tabs: [
        { name: '全部', status: null },
        { name: '待支付', status: 0 },
        { name: '已支付', status: 1 },
        { name: '已发货', status: 2 },
        { name: '已完成', status: 3 }
      ],
      currentTab: 0,
      orderList: []
    }
  },

  onLoad(options) {
    if (options.tab) {
      this.currentTab = parseInt(options.tab) || 0
    }
  },

  onShow() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    // 切换 Tab
    switchTab(index) {
      this.currentTab = index
      this.loadOrders()
    },

    // 加载订单列表
    async loadOrders() {
      try {
        uni.showLoading({ title: '加载中' })
        const status = this.tabs[this.currentTab].status
        const res = await getOrderList(status)
        if (res.code === 200) {
          this.orderList = res.data || []
        }
      } catch (e) {
        console.error('加载订单列表失败', e)
      } finally {
        uni.hideLoading()
      }
    },

    // 获取状态文本
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

    // 跳转订单详情
    goDetail(id) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${id}`
      })
    },

    // 取消订单
    handleCancel(id) {
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await cancelOrder(id)
              if (result.code === 200) {
                uni.showToast({ title: '已取消', icon: 'success' })
                this.loadOrders()
              } else {
                uni.showToast({ title: result.message || '取消失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '取消失败', icon: 'none' })
            }
          }
        }
      })
    },

    // 去支付
    handlePay(id, amount) {
      uni.showModal({
        title: '支付确认',
        content: `订单金额：¥${amount}，确认支付？`,
        confirmText: '确认支付',
        cancelText: '取消',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '支付中' })
              const result = await payOrder(id)
              uni.hideLoading()

              if (result.code === 200) {
                uni.showToast({ title: '支付成功', icon: 'success' })
                this.loadOrders()
              } else {
                uni.showToast({ title: result.message || '支付失败', icon: 'none' })
              }
            } catch (e) {
              uni.hideLoading()
              uni.showToast({ title: '支付失败', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list {
  min-height: 100vh;
  background-color: $bg-color;
}

/* Tabs */
.tabs {
  display: flex;
  background-color: $bg-color-white;
  border-bottom: 1rpx solid $border-color;
  position: sticky;
  top: 0;
  z-index: 10;

  &__item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $spacing-md 0;
    position: relative;

    &--active .tabs__text {
      color: $primary-color;
      font-weight: bold;
    }
  }

  &__text {
    font-size: $font-size-md;
    color: $text-color-light;
  }

  &__line {
    position: absolute;
    bottom: 0;
    width: 48rpx;
    height: 4rpx;
    background-color: $primary-color;
    border-radius: 2rpx;
  }
}

/* 空状态 */
.empty {
  padding: 200rpx 0;
  text-align: center;

  &__text {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }
}

/* 列表内容 */
.list-content {
  padding: $spacing-sm;
}

/* 订单卡片 */
.order-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md;
    border-bottom: 1rpx solid $border-color-light;
  }

  &__no {
    font-size: $font-size-sm;
    color: $text-color-light;
  }

  &__status {
    font-size: $font-size-sm;
    font-weight: bold;
  }

  &__goods {
    padding: $spacing-sm $spacing-md;
  }

  &__goods-item {
    display: flex;
    padding: $spacing-xs 0;
  }

  &__goods-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: $border-radius-sm;
    margin-right: $spacing-sm;
    flex-shrink: 0;
  }

  &__goods-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  &__goods-name {
    font-size: $font-size-sm;
    color: $text-color;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__goods-spec {
    font-size: $font-size-xs;
    color: $text-color-lighter;
    margin-top: 4rpx;
  }

  &__goods-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__goods-price {
    font-size: $font-size-sm;
    color: $price-color;
  }

  &__goods-qty {
    font-size: $font-size-xs;
    color: $text-color-lighter;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md;
    border-top: 1rpx solid $border-color-light;
  }

  &__total {
    font-size: $font-size-sm;
    color: $text-color;
  }

  &__total-price {
    font-size: $font-size-md;
    color: $price-color;
    font-weight: bold;
  }

  &__actions {
    display: flex;
    align-items: center;
  }

  &__btn {
    padding: 10rpx 24rpx;
    border-radius: $border-radius-round;
    margin-left: $spacing-sm;

    &--cancel {
      border: 1rpx solid $border-color;
    }

    &--pay {
      background-color: $primary-color;
    }
  }

  &__btn-text {
    font-size: $font-size-sm;
    color: $text-color-light;

    &--white {
      color: #ffffff;
    }
  }
}

/* 状态颜色 */
.status-0 {
  color: $warning-color;
}
.status-1 {
  color: $success-color;
}
.status-2 {
  color: $primary-color;
}
.status-3 {
  color: $text-color-lighter;
}
.status-4 {
  color: $text-color-lighter;
}
.status-5 {
  color: $error-color;
}
</style>
