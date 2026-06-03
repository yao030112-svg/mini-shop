<template>
  <view class="product-card" @click="goDetail">
    <image class="product-card__image" :src="product.mainImage" mode="aspectFill" />
    <view class="product-card__info">
      <text class="product-card__name">{{ product.name }}</text>
      <view class="product-card__price">
        <text class="product-card__symbol">¥</text>
        <text class="product-card__amount">{{ formatPrice }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  computed: {
    formatPrice() {
      const price = Number(this.product.minPrice)
      if (isNaN(price)) return '0.00'
      return price.toFixed(2)
    }
  },
  methods: {
    goDetail() {
      uni.navigateTo({
        url: `/pages/product/detail?id=${this.product.id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.product-card {
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border-radius: $border-radius-md;
  overflow: hidden;

  &__image {
    width: 100%;
    height: 340rpx;
  }

  &__info {
    padding: $spacing-sm;
  }

  &__name {
    font-size: $font-size-md;
    color: $text-color;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.4;
  }

  &__price {
    margin-top: $spacing-xs;
    display: flex;
    align-items: baseline;
  }

  &__symbol {
    font-size: $font-size-sm;
    color: $price-color;
    font-weight: bold;
  }

  &__amount {
    font-size: $font-size-lg;
    color: $price-color;
    font-weight: bold;
  }
}
</style>
