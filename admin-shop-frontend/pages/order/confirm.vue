<template>
  <view class="order-confirm">
    <!-- 收货地址 -->
    <view class="address-section" @click="chooseAddress">
      <view v-if="address" class="address-card">
        <view class="address-card__info">
          <view class="address-card__user">
            <text class="address-card__name">{{ address.receiverName }}</text>
            <text class="address-card__phone">{{ address.phone }}</text>
          </view>
          <view class="address-card__detail">
            <text>{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</text>
          </view>
        </view>
        <view class="address-card__arrow">
          <text class="iconfont">›</text>
        </view>
      </view>
      <view v-else class="address-empty">
        <text class="address-empty__text">请添加收货地址</text>
        <text class="address-empty__arrow">›</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="goods-section">
      <view class="goods-item" v-for="item in goodsList" :key="item.id">
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

    <!-- 金额汇总 -->
    <view class="summary-section">
      <view class="summary-row">
        <text class="summary-row__label">商品金额</text>
        <text class="summary-row__value">¥{{ totalAmount }}</text>
      </view>
      <view class="summary-row">
        <text class="summary-row__label">运费</text>
        <text class="summary-row__value">免运费</text>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder"></view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="bottom-bar__total">
        <text class="bottom-bar__label">合计：</text>
        <text class="bottom-bar__price">¥{{ totalAmount }}</text>
      </view>
      <view class="bottom-bar__btn" @click="handleSubmit">
        <text class="bottom-bar__btn-text">提交订单</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getAddressList } from '@/api/address'
import { createOrder, payOrder } from '@/api/order'

export default {
  data() {
    return {
      address: null,
      addressList: [],
      goodsList: [],
      totalAmount: '0.00'
    }
  },

  onLoad() {
    this.loadAddress()
    this.loadGoods()
  },

  onShow() {
    // 从地址选择页返回时刷新地址
    const selectedAddress = uni.getStorageSync('selectedAddress')
    if (selectedAddress) {
      this.address = selectedAddress
      uni.removeStorageSync('selectedAddress')
    }
  },

  methods: {
    // 加载收货地址
    async loadAddress() {
      try {
        const res = await getAddressList()
        if (res.code === 200) {
          this.addressList = res.data || []
          // 优先使用默认地址
          const defaultAddr = this.addressList.find(item => item.isDefault === 1)
          if (defaultAddr) {
            this.address = defaultAddr
          } else if (this.addressList.length > 0) {
            this.address = this.addressList[0]
          }
        }
      } catch (e) {
        console.error('加载地址失败', e)
      }
    },

    // 加载商品数据（从购物车选中商品获取）
    loadGoods() {
      const cartStore = this.$store.state.cart
      const selectedGoods = (cartStore.cartList || []).filter(item => item.selected)
      this.goodsList = selectedGoods
      this.calculateTotal()
    },

    // 计算总金额
    calculateTotal() {
      let total = 0
      this.goodsList.forEach(item => {
        total += parseFloat(item.price) * item.quantity
      })
      this.totalAmount = total.toFixed(2)
    },

    // 选择地址
    chooseAddress() {
      uni.navigateTo({
        url: '/pages/address/list?from=order'
      })
    },

    // 提交订单
    async handleSubmit() {
      if (!this.address) {
        uni.showToast({ title: '请先添加收货地址', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中' })
        const res = await createOrder(this.address.id)
        uni.hideLoading()

        if (res.code === 200) {
          const orderId = res.data.id || res.data
          // 弹出支付确认
          this.showPayConfirm(orderId)
        } else {
          uni.showToast({ title: res.message || '下单失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        console.error('提交订单失败', e)
        uni.showToast({ title: '下单失败', icon: 'none' })
      }
    },

    // 支付确认弹窗
    showPayConfirm(orderId) {
      uni.showModal({
        title: '支付确认',
        content: `订单金额：¥${this.totalAmount}，确认支付？`,
        confirmText: '确认支付',
        cancelText: '稍后支付',
        success: (modalRes) => {
          if (modalRes.confirm) {
            this.handlePay(orderId)
          } else {
            // 稍后支付，跳转订单列表
            uni.redirectTo({
              url: '/pages/order/list'
            })
          }
        }
      })
    },

    // 模拟支付
    async handlePay(orderId) {
      try {
        uni.showLoading({ title: '支付中' })
        const res = await payOrder(orderId)
        uni.hideLoading()

        if (res.code === 200) {
          uni.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            uni.redirectTo({
              url: '/pages/order/list'
            })
          }, 1500)
        } else {
          uni.showToast({ title: res.message || '支付失败', icon: 'none' })
          setTimeout(() => {
            uni.redirectTo({
              url: '/pages/order/list'
            })
          }, 1500)
        }
      } catch (e) {
        uni.hideLoading()
        console.error('支付失败', e)
        uni.showToast({ title: '支付失败，请稍后重试', icon: 'none' })
        setTimeout(() => {
          uni.redirectTo({
            url: '/pages/order/list'
          })
        }, 1500)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.order-confirm {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 地址区域 */
.address-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.address-card {
  display: flex;
  align-items: center;

  &__info {
    flex: 1;
  }

  &__user {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-size-lg;
    font-weight: bold;
    color: $text-color;
    margin-right: $spacing-md;
  }

  &__phone {
    font-size: $font-size-md;
    color: $text-color-light;
  }

  &__detail {
    font-size: $font-size-sm;
    color: $text-color-light;
    line-height: 1.4;
  }

  &__arrow {
    font-size: 40rpx;
    color: $text-color-lighter;
    padding-left: $spacing-sm;
  }
}

.address-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;

  &__text {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }

  &__arrow {
    font-size: 40rpx;
    color: $text-color-lighter;
  }
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
    width: 160rpx;
    height: 160rpx;
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

/* 金额汇总 */
.summary-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xs 0;

  &__label {
    font-size: $font-size-md;
    color: $text-color-light;
  }

  &__value {
    font-size: $font-size-md;
    color: $text-color;
  }
}

/* 底部占位 */
.bottom-placeholder {
  height: 120rpx;
}

/* 底部提交栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;

  &__total {
    display: flex;
    align-items: center;
  }

  &__label {
    font-size: $font-size-md;
    color: $text-color;
  }

  &__price {
    font-size: $font-size-xl;
    color: $price-color;
    font-weight: bold;
  }

  &__btn {
    height: 72rpx;
    padding: 0 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $primary-color;
    border-radius: $border-radius-round;
  }

  &__btn-text {
    font-size: $font-size-md;
    color: #ffffff;
    font-weight: bold;
  }
}
</style>
