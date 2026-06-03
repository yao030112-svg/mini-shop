<template>
  <view class="cart">
    <!-- 空购物车 -->
    <Empty v-if="!loading && cartList.length === 0" text="购物车空空如也，快去逛逛吧" />

    <!-- 购物车列表 -->
    <view v-else class="cart__list">
      <!-- 全选栏 -->
      <view class="cart__select-all" @click="handleSelectAll">
        <view class="cart__checkbox" :class="{ 'cart__checkbox--checked': isAllSelected }">
          <text v-if="isAllSelected" class="cart__checkbox-icon">✓</text>
        </view>
        <text class="cart__select-all-text">全选</text>
      </view>

      <!-- 购物车条目 -->
      <view class="cart__item" v-for="item in cartList" :key="item.id">
        <view class="cart__item-inner">
          <!-- 滑动容器 -->
          <movable-area class="cart__movable-area">
            <movable-view
              class="cart__movable-view"
              direction="horizontal"
              :x="item._x || 0"
              damping="30"
              :out-of-bounds="false"
              @change="onMovableChange($event, item)"
              @touchend="onTouchEnd(item)"
            >
              <!-- 商品内容 -->
              <view class="cart__item-content">
                <!-- 复选框 -->
                <view class="cart__checkbox" :class="{ 'cart__checkbox--checked': item.selected }" @click.stop="handleToggleSelect(item)">
                  <text v-if="item.selected" class="cart__checkbox-icon">✓</text>
                </view>

                <!-- 商品图片 -->
                <image class="cart__item-image" :src="item.skuImage || item.productMainImage" mode="aspectFill" @click="goProductDetail(item)" />

                <!-- 商品信息 -->
                <view class="cart__item-info">
                  <text class="cart__item-name" @click="goProductDetail(item)">{{ item.productName }}</text>
                  <text class="cart__item-spec">{{ item.skuSpecDesc }}</text>
                  <view class="cart__item-bottom">
                    <Price :value="item.skuPrice" size="normal" />
                    <NumberBox
                      :value="item.quantity"
                      :min="1"
                      :max="item.skuStock || 99"
                      @change="handleQuantityChange(item, $event)"
                    />
                  </view>
                </view>
              </view>
            </movable-view>
          </movable-area>

          <!-- 删除按钮（左滑显示） -->
          <view class="cart__item-delete" @click="handleDelete(item)">
            <text class="cart__item-delete-text">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view v-if="cartList.length > 0" class="cart__footer">
      <view class="cart__footer-left">
        <view class="cart__checkbox" :class="{ 'cart__checkbox--checked': isAllSelected }" @click="handleSelectAll">
          <text v-if="isAllSelected" class="cart__checkbox-icon">✓</text>
        </view>
        <text class="cart__footer-select-text">全选</text>
      </view>
      <view class="cart__footer-right">
        <view class="cart__footer-total">
          <text class="cart__footer-total-label">合计：</text>
          <Price :value="totalPrice" size="large" />
        </view>
        <view class="cart__footer-btn" :class="{ 'cart__footer-btn--disabled': selectedCount === 0 }" @click="handleCheckout">
          <text class="cart__footer-btn-text">结算({{ selectedCount }})</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'

export default {
  data() {
    return {
      loading: true
    }
  },

  computed: {
    ...mapState('cart', ['cartList', 'totalPrice', 'selectedCount']),

    // 是否全选
    isAllSelected() {
      if (this.cartList.length === 0) return false
      return this.cartList.every(item => item.selected)
    }
  },

  onShow() {
    this.loadCartList()
  },

  methods: {
    // 加载购物车列表
    async loadCartList() {
      this.loading = true
      try {
        await this.$store.dispatch('cart/getCartList')
      } catch (e) {
        console.error('加载购物车失败', e)
      } finally {
        this.loading = false
      }
    },

    // 切换单个商品选中状态
    handleToggleSelect(item) {
      const newSelected = !item.selected
      this.$store.dispatch('cart/toggleSelect', {
        id: item.id,
        selected: newSelected
      }).catch(e => {
        console.error('切换选中状态失败', e)
      })
    },

    // 全选/取消全选
    handleSelectAll() {
      const selected = !this.isAllSelected
      this.$store.dispatch('cart/toggleSelectAll', selected).catch(e => {
        uni.showToast({ title: '操作失败', icon: 'none' })
        console.error('全选操作失败', e)
      })
    },

    // 修改数量
    handleQuantityChange(item, newQuantity) {
      this.$store.dispatch('cart/updateQuantity', {
        id: item.id,
        quantity: newQuantity
      }).catch(e => {
        uni.showToast({ title: '修改数量失败', icon: 'none' })
        console.error('修改数量失败', e)
      })
    },

    // 删除商品
    handleDelete(item) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该商品吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('cart/deleteItem', item.id).then(() => {
              uni.showToast({ title: '已删除', icon: 'success' })
            }).catch(e => {
              uni.showToast({ title: '删除失败', icon: 'none' })
              console.error('删除失败', e)
            })
          }
        }
      })
    },

    // 滑动事件处理
    onMovableChange(e, item) {
      // 记录当前滑动位置
      item._currentX = e.detail.x
    },

    // 触摸结束，判断是否展开删除按钮
    onTouchEnd(item) {
      // 如果滑动超过阈值，展开删除按钮
      if (item._currentX < -60) {
        this.$set(item, '_x', -130)
      } else {
        this.$set(item, '_x', 0)
      }
    },

    // 跳转商品详情
    goProductDetail(item) {
      uni.navigateTo({
        url: `/pages/product/product?id=${item.productId}`
      })
    },

    // 结算
    handleCheckout() {
      if (this.selectedCount === 0) {
        uni.showToast({ title: '请选择要结算的商品', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: '/pages/order/confirm'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.cart {
  min-height: 100vh;
  background-color: $bg-color;
  padding-bottom: 120rpx;
}

/* 全选栏 */
.cart__select-all {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-white;
  margin-bottom: $spacing-xs;
}

.cart__select-all-text {
  font-size: $font-size-md;
  color: $text-color;
  margin-left: $spacing-sm;
}

/* 复选框 */
.cart__checkbox {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid $border-color;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--checked {
    background-color: $primary-color;
    border-color: $primary-color;
  }
}

.cart__checkbox-icon {
  font-size: $font-size-sm;
  color: #ffffff;
}

/* 购物车条目 */
.cart__item {
  margin-bottom: 2rpx;
  background-color: $bg-color-white;
  overflow: hidden;
}

.cart__item-inner {
  position: relative;
  height: 220rpx;
}

/* 滑动区域 */
.cart__movable-area {
  width: 100%;
  height: 220rpx;
}

.cart__movable-view {
  width: calc(100% + 130rpx);
  height: 220rpx;
}

/* 商品内容 */
.cart__item-content {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  width: 750rpx;
  height: 220rpx;
  box-sizing: border-box;
}

.cart__item-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-md;
  margin-left: $spacing-sm;
  flex-shrink: 0;
}

.cart__item-info {
  flex: 1;
  margin-left: $spacing-sm;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 160rpx;
  overflow: hidden;
}

.cart__item-name {
  font-size: $font-size-md;
  color: $text-color;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.cart__item-spec {
  font-size: $font-size-sm;
  color: $text-color-lighter;
  margin-top: 8rpx;
}

.cart__item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

/* 删除按钮 */
.cart__item-delete {
  position: absolute;
  right: 0;
  top: 0;
  width: 130rpx;
  height: 100%;
  background-color: $error-color;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart__item-delete-text {
  font-size: $font-size-md;
  color: #ffffff;
}

/* 底部结算栏 */
.cart__footer {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 100rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-sizing: border-box;
  border-top: 1rpx solid $border-color;
  z-index: 100;
}

.cart__footer-left {
  display: flex;
  align-items: center;
}

.cart__footer-select-text {
  font-size: $font-size-sm;
  color: $text-color;
  margin-left: $spacing-xs;
}

.cart__footer-right {
  display: flex;
  align-items: center;
}

.cart__footer-total {
  display: flex;
  align-items: baseline;
  margin-right: $spacing-md;
}

.cart__footer-total-label {
  font-size: $font-size-sm;
  color: $text-color;
}

.cart__footer-btn {
  width: 200rpx;
  height: 72rpx;
  background-color: $primary-color;
  border-radius: $border-radius-round;
  display: flex;
  align-items: center;
  justify-content: center;

  &--disabled {
    opacity: 0.5;
  }
}

.cart__footer-btn-text {
  font-size: $font-size-md;
  color: #ffffff;
  font-weight: bold;
}
</style>
