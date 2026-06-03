<template>
  <view class="product-detail">
    <!-- 商品图片轮播 -->
    <swiper
      class="product-swiper"
      :indicator-dots="true"
      :autoplay="false"
      :duration="300"
      indicator-color="rgba(0,0,0,0.3)"
      indicator-active-color="#ff6600"
      :current="currentSwiperIndex"
    >
      <swiper-item v-for="(img, index) in imageList" :key="index">
        <image class="product-swiper__image" :src="img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 商品基本信息 -->
    <view class="product-info">
      <view class="product-info__price">
        <Price :value="displayPrice" size="large" />
        <text v-if="selectedSku" class="product-info__stock">库存: {{ selectedSku.stock }}</text>
      </view>
      <view class="product-info__name">
        <text>{{ product.name }}</text>
      </view>
      <view v-if="product.description" class="product-info__desc">
        <text>{{ product.description }}</text>
      </view>
    </view>

    <!-- SKU 规格选择器 -->
    <view class="spec-selector" v-if="specs.length > 0">
      <view class="spec-selector__title">
        <text>选择规格</text>
      </view>
      <view class="spec-group" v-for="spec in specs" :key="spec.id">
        <view class="spec-group__name">
          <text>{{ spec.name }}</text>
        </view>
        <view class="spec-group__values">
          <view
            v-for="val in spec.values"
            :key="val.id"
            class="spec-value"
            :class="{
              'spec-value--active': selectedValues[spec.id] === val.id,
              'spec-value--disabled': isValueDisabled(spec.id, val.id)
            }"
            @click="onSelectSpec(spec.id, val.id)"
          >
            <text class="spec-value__text">{{ val.value }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder"></view>

    <!-- 底部固定栏 -->
    <view class="bottom-bar">
      <view
        class="bottom-bar__btn bottom-bar__btn--cart"
        :class="{ 'bottom-bar__btn--disabled': !canAddToCart }"
        @click="handleAddToCart"
      >
        <text class="bottom-bar__btn-text">加入购物车</text>
      </view>
      <view
        class="bottom-bar__btn bottom-bar__btn--buy"
        :class="{ 'bottom-bar__btn--disabled': !canAddToCart }"
        @click="handleBuyNow"
      >
        <text class="bottom-bar__btn-text">立即购买</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'

export default {
  data() {
    return {
      productId: null,
      product: {},
      specs: [],
      skus: [],
      selectedValues: {},
      currentSwiperIndex: 0
    }
  },

  computed: {
    // 商品图片列表
    imageList() {
      if (this.product.images) {
        try {
          const images = JSON.parse(this.product.images)
          if (Array.isArray(images) && images.length > 0) {
            return images
          }
        } catch (e) {
          // ignore
        }
      }
      // 兜底使用主图
      return this.product.mainImage ? [this.product.mainImage] : []
    },

    // 当前选中的 SKU
    selectedSku() {
      if (!this.isAllSpecSelected) return null
      const selectedValueIds = Object.values(this.selectedValues)
      return this.skus.find(sku => {
        const skuValueIds = sku.specValues.map(sv => sv.specValueId)
        return selectedValueIds.length === skuValueIds.length &&
          selectedValueIds.every(id => skuValueIds.includes(id))
      }) || null
    },

    // 是否已选择所有规格维度
    isAllSpecSelected() {
      return this.specs.length > 0 &&
        Object.keys(this.selectedValues).length === this.specs.length
    },

    // 展示价格：选中 SKU 时显示 SKU 价格，否则显示商品最低价
    displayPrice() {
      if (this.selectedSku) {
        return this.selectedSku.price
      }
      return this.product.minPrice || 0
    },

    // 是否可以加入购物车
    canAddToCart() {
      // 无规格商品直接可加入（使用第一个 SKU）
      if (this.specs.length === 0 && this.skus.length > 0) {
        return this.skus[0].stock > 0
      }
      return this.selectedSku && this.selectedSku.stock > 0
    }
  },

  onLoad(options) {
    if (options.id) {
      this.productId = options.id
      this.loadProductDetail()
    }
  },

  methods: {
    // 加载商品详情
    async loadProductDetail() {
      try {
        uni.showLoading({ title: '加载中' })
        const res = await getProductDetail(this.productId)
        if (res.code === 200) {
          const { product, specs, skus } = res.data
          this.product = product || {}
          this.specs = specs || []
          this.skus = skus || []
        } else {
          uni.showToast({ title: '商品不存在', icon: 'none' })
        }
      } catch (e) {
        console.error('加载商品详情失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    // 选择规格值
    onSelectSpec(specId, valueId) {
      // 如果该值不可选，不响应点击
      if (this.isValueDisabled(specId, valueId)) return

      // 如果已选中，取消选择
      if (this.selectedValues[specId] === valueId) {
        this.$delete(this.selectedValues, specId)
      } else {
        this.$set(this.selectedValues, specId, valueId)
      }

      // 如果选中了 SKU 且 SKU 有独立图片，切换轮播图
      if (this.selectedSku && this.selectedSku.image) {
        const idx = this.imageList.indexOf(this.selectedSku.image)
        if (idx >= 0) {
          this.currentSwiperIndex = idx
        }
      }
    },

    // 判断某个规格值是否不可选（库存为0）
    isValueDisabled(specId, valueId) {
      // 构造一个假设选中该值后的选择集合
      const tempSelected = { ...this.selectedValues, [specId]: valueId }

      // 找出所有包含该规格值的 SKU
      const matchingSkus = this.skus.filter(sku => {
        const skuValueIds = sku.specValues.map(sv => sv.specValueId)
        // 检查当前已选的所有值是否都在该 SKU 中
        return Object.values(tempSelected).every(vid => skuValueIds.includes(vid))
      })

      // 如果没有匹配的 SKU，或所有匹配的 SKU 库存都为0，则不可选
      if (matchingSkus.length === 0) return true
      return matchingSkus.every(sku => sku.stock <= 0)
    },

    // 加入购物车
    async handleAddToCart() {
      if (!this.canAddToCart) {
        if (!this.isAllSpecSelected && this.specs.length > 0) {
          uni.showToast({ title: '请选择完整规格', icon: 'none' })
        } else {
          uni.showToast({ title: '该规格暂无库存', icon: 'none' })
        }
        return
      }

      let skuId
      if (this.specs.length === 0 && this.skus.length > 0) {
        skuId = this.skus[0].id
      } else {
        skuId = this.selectedSku.id
      }

      try {
        uni.showLoading({ title: '加入中' })
        const res = await addToCart(skuId)
        if (res.code === 200) {
          uni.showToast({ title: '已加入购物车', icon: 'success' })
        } else {
          uni.showToast({ title: res.message || '加入失败', icon: 'none' })
        }
      } catch (e) {
        console.error('加入购物车失败', e)
        uni.showToast({ title: '加入失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    // 立即购买
    handleBuyNow() {
      if (!this.canAddToCart) {
        if (!this.isAllSpecSelected && this.specs.length > 0) {
          uni.showToast({ title: '请选择完整规格', icon: 'none' })
        } else {
          uni.showToast({ title: '该规格暂无库存', icon: 'none' })
        }
        return
      }

      let skuId
      if (this.specs.length === 0 && this.skus.length > 0) {
        skuId = this.skus[0].id
      } else {
        skuId = this.selectedSku.id
      }

      // 跳转到订单确认页面，传递 SKU ID 和购买数量
      uni.navigateTo({
        url: `/pages/order/confirm?skuId=${skuId}&quantity=1`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.product-detail {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 商品图片轮播 */
.product-swiper {
  width: 100%;
  height: 750rpx;

  &__image {
    width: 100%;
    height: 100%;
  }
}

/* 商品基本信息 */
.product-info {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__price {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-sm;
  }

  &__stock {
    font-size: $font-size-sm;
    color: $text-color-lighter;
  }

  &__name {
    font-size: $font-size-lg;
    font-weight: bold;
    color: $text-color;
    line-height: 1.4;
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $text-color-light;
    line-height: 1.6;
  }
}

/* SKU 规格选择器 */
.spec-selector {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__title {
    font-size: $font-size-md;
    font-weight: bold;
    color: $text-color;
    margin-bottom: $spacing-md;
  }
}

.spec-group {
  margin-bottom: $spacing-md;

  &:last-child {
    margin-bottom: 0;
  }

  &__name {
    font-size: $font-size-sm;
    color: $text-color-light;
    margin-bottom: $spacing-sm;
  }

  &__values {
    display: flex;
    flex-wrap: wrap;
  }
}

.spec-value {
  padding: 12rpx 28rpx;
  margin-right: $spacing-sm;
  margin-bottom: $spacing-sm;
  background-color: $bg-color-grey;
  border-radius: $border-radius-round;
  border: 2rpx solid transparent;

  &--active {
    background-color: rgba(255, 102, 0, 0.1);
    border-color: $primary-color;
  }

  &--active .spec-value__text {
    color: $primary-color;
  }

  &--disabled {
    opacity: 0.4;
    background-color: $bg-color-grey;
  }

  &--disabled .spec-value__text {
    color: $text-color-lighter;
    text-decoration: line-through;
  }

  &__text {
    font-size: $font-size-sm;
    color: $text-color;
  }
}

/* 底部占位 */
.bottom-placeholder {
  height: 120rpx;
}

/* 底部固定栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;

  &__btn {
    flex: 1;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $border-radius-round;
    margin-right: $spacing-sm;

    &:last-child {
      margin-right: 0;
    }

    &--cart {
      background-color: $primary-color;
    }

    &--buy {
      background-color: #ff4444;
    }

    &--disabled {
      background-color: $text-color-lighter;
    }
  }

  &__btn-text {
    font-size: $font-size-md;
    color: #ffffff;
    font-weight: bold;
  }
}
</style>
