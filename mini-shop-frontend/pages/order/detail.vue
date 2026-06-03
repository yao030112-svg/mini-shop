<template>
  <view class="order-detail">
    <!-- 订单状态 -->
    <view class="status-section" :class="'status-bg-' + order.status">
      <text class="status-section__text">{{ getStatusText(order.status) }}</text>
      <text class="status-section__desc">{{ getStatusDesc(order.status) }}</text>
    </view>

    <!-- 收货地址 -->
    <view class="address-section" v-if="addressInfo">
      <view class="address-section__icon">
        <text>📍</text>
      </view>
      <view class="address-section__info">
        <view class="address-section__user">
          <text class="address-section__name">{{ addressInfo.receiverName }}</text>
          <text class="address-section__phone">{{ addressInfo.phone }}</text>
        </view>
        <text class="address-section__detail">{{ addressInfo.province }}{{ addressInfo.city }}{{ addressInfo.district }}{{ addressInfo.detail }}</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="goods-section">
      <view class="section-title">
        <text>商品信息</text>
      </view>
      <view class="goods-item" v-for="item in order.items" :key="item.id">
        <image class="goods-item__image" :src="item.image" mode="aspectFill" />
        <view class="goods-item__info">
          <text class="goods-item__name">{{ item.productName }}</text>
          <text class="goods-item__spec" v-if="item.skuSpecDesc">{{ item.skuSpecDesc }}</text>
          <view class="goods-item__bottom">
            <text class="goods-item__price">¥{{ item.price }}</text>
            <text class="goods-item__qty">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 金额信息 -->
    <view class="amount-section">
      <view class="section-title">
        <text>金额信息</text>
      </view>
      <view class="amount-row">
        <text class="amount-row__label">商品总额</text>
        <text class="amount-row__value">¥{{ order.totalAmount }}</text>
      </view>
      <view class="amount-row">
        <text class="amount-row__label">运费</text>
        <text class="amount-row__value">免运费</text>
      </view>
      <view class="amount-row amount-row--total">
        <text class="amount-row__label">实付金额</text>
        <text class="amount-row__value amount-row__value--price">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="info-section">
      <view class="section-title">
        <text>订单信息</text>
      </view>
      <view class="info-row">
        <text class="info-row__label">订单编号</text>
        <text class="info-row__value">{{ order.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-row__label">创建时间</text>
        <text class="info-row__value">{{ order.createTime }}</text>
      </view>
      <view class="info-row" v-if="order.paymentTime">
        <text class="info-row__label">支付时间</text>
        <text class="info-row__value">{{ order.paymentTime }}</text>
      </view>
      <view class="info-row" v-if="order.shippingTime">
        <text class="info-row__label">发货时间</text>
        <text class="info-row__value">{{ order.shippingTime }}</text>
      </view>
      <view class="info-row" v-if="order.completeTime">
        <text class="info-row__label">完成时间</text>
        <text class="info-row__value">{{ order.completeTime }}</text>
      </view>
      <view class="info-row" v-if="order.cancelTime">
        <text class="info-row__label">取消时间</text>
        <text class="info-row__value">{{ order.cancelTime }}</text>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder" v-if="order.status === 0"></view>

    <!-- 底部操作栏（待支付状态） -->
    <view class="bottom-bar" v-if="order.status === 0">
      <view class="bottom-bar__btn bottom-bar__btn--cancel" @click="handleCancel">
        <text class="bottom-bar__btn-text">取消订单</text>
      </view>
      <view class="bottom-bar__btn bottom-bar__btn--pay" @click="handlePay">
        <text class="bottom-bar__btn-text bottom-bar__btn-text--white">立即支付</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderDetail, cancelOrder, payOrder } from '@/api/order'

export default {
  data() {
    return {
      orderId: null,
      order: {},
      addressInfo: null
    }
  },

  onLoad(options) {
    if (options.id) {
      this.orderId = options.id
      this.loadDetail()
    }
  },

  methods: {
    // 加载订单详情
    async loadDetail() {
      try {
        uni.showLoading({ title: '加载中' })
        const res = await getOrderDetail(this.orderId)
        if (res.code === 200) {
          this.order = res.data || {}
          // 解析地址快照
          if (this.order.addressSnapshot) {
            try {
              this.addressInfo = JSON.parse(this.order.addressSnapshot)
            } catch (e) {
              this.addressInfo = null
            }
          }
        } else {
          uni.showToast({ title: '订单不存在', icon: 'none' })
        }
      } catch (e) {
        console.error('加载订单详情失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
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
      return map[status] || ''
    },

    // 获取状态描述
    getStatusDesc(status) {
      const map = {
        0: '请在30分钟内完成支付',
        1: '商家正在准备发货',
        2: '商品正在配送中',
        3: '订单已完成，感谢您的购买',
        4: '订单已取消',
        5: '退款已处理'
      }
      return map[status] || ''
    },

    // 取消订单
    handleCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await cancelOrder(this.orderId)
              if (result.code === 200) {
                uni.showToast({ title: '已取消', icon: 'success' })
                this.loadDetail()
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

    // 立即支付
    handlePay() {
      uni.showModal({
        title: '支付确认',
        content: `订单金额：¥${this.order.totalAmount}，确认支付？`,
        confirmText: '确认支付',
        cancelText: '取消',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '支付中' })
              const result = await payOrder(this.orderId)
              uni.hideLoading()

              if (result.code === 200) {
                uni.showToast({ title: '支付成功', icon: 'success' })
                this.loadDetail()
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
.order-detail {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 状态区域 */
.status-section {
  padding: $spacing-lg $spacing-md;
  background-color: $primary-color;

  &__text {
    font-size: $font-size-xl;
    color: #ffffff;
    font-weight: bold;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.8);
  }
}

.status-bg-0 {
  background-color: $warning-color;
}
.status-bg-1 {
  background-color: $success-color;
}
.status-bg-2 {
  background-color: $primary-color;
}
.status-bg-3 {
  background-color: $text-color-lighter;
}
.status-bg-4 {
  background-color: $text-color-lighter;
}
.status-bg-5 {
  background-color: $error-color;
}

/* 地址区域 */
.address-section {
  display: flex;
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__icon {
    margin-right: $spacing-sm;
    font-size: 36rpx;
  }

  &__info {
    flex: 1;
  }

  &__user {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-size-md;
    font-weight: bold;
    color: $text-color;
    margin-right: $spacing-md;
  }

  &__phone {
    font-size: $font-size-sm;
    color: $text-color-light;
  }

  &__detail {
    font-size: $font-size-sm;
    color: $text-color-light;
    line-height: 1.4;
  }
}

/* 区块标题 */
.section-title {
  font-size: $font-size-md;
  font-weight: bold;
  color: $text-color;
  padding-bottom: $spacing-sm;
  border-bottom: 1rpx solid $border-color-light;
  margin-bottom: $spacing-sm;
}

/* 商品列表 */
.goods-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.goods-item {
  display: flex;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-color-light;

  &:last-child {
    border-bottom: none;
  }

  &__image {
    width: 140rpx;
    height: 140rpx;
    border-radius: $border-radius-sm;
    margin-right: $spacing-md;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  &__name {
    font-size: $font-size-md;
    color: $text-color;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__spec {
    font-size: $font-size-sm;
    color: $text-color-lighter;
    margin-top: $spacing-xs;
  }

  &__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: $spacing-xs;
  }

  &__price {
    font-size: $font-size-md;
    color: $price-color;
    font-weight: bold;
  }

  &__qty {
    font-size: $font-size-sm;
    color: $text-color-lighter;
  }
}

/* 金额信息 */
.amount-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xs 0;

  &--total {
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-color-light;
    margin-top: $spacing-xs;
  }

  &__label {
    font-size: $font-size-sm;
    color: $text-color-light;
  }

  &__value {
    font-size: $font-size-sm;
    color: $text-color;

    &--price {
      font-size: $font-size-lg;
      color: $price-color;
      font-weight: bold;
    }
  }
}

/* 订单信息 */
.info-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xs 0;

  &__label {
    font-size: $font-size-sm;
    color: $text-color-light;
  }

  &__value {
    font-size: $font-size-sm;
    color: $text-color;
  }
}

/* 底部占位 */
.bottom-placeholder {
  height: 120rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;

  &__btn {
    height: 64rpx;
    padding: 0 36rpx;
    display: flex;
    align-items: center;
    justify-content: center;
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
    font-size: $font-size-md;
    color: $text-color-light;

    &--white {
      color: #ffffff;
    }
  }
}
</style>
