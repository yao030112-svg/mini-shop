<template>
  <view class="price" :class="'price--' + size">
    <text class="price__symbol">¥</text>
    <text class="price__value">{{ formatValue }}</text>
  </view>
</template>

<script>
export default {
  name: 'Price',
  props: {
    value: {
      type: [Number, String],
      required: true
    },
    size: {
      type: String,
      default: 'normal',
      validator(val) {
        return ['small', 'normal', 'large'].includes(val)
      }
    }
  },
  computed: {
    formatValue() {
      const num = Number(this.value)
      if (isNaN(num)) return '0.00'
      return num.toFixed(2)
    }
  }
}
</script>

<style lang="scss" scoped>
.price {
  display: inline-flex;
  align-items: baseline;
  color: $price-color;
  font-weight: bold;

  &--small {
    .price__symbol {
      font-size: $font-size-xs;
    }
    .price__value {
      font-size: $font-size-sm;
    }
  }

  &--normal {
    .price__symbol {
      font-size: $font-size-sm;
    }
    .price__value {
      font-size: $font-size-lg;
    }
  }

  &--large {
    .price__symbol {
      font-size: $font-size-md;
    }
    .price__value {
      font-size: $font-size-xl;
    }
  }
}
</style>
