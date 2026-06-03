<template>
  <view class="number-box">
    <view
      class="number-box__btn number-box__btn--minus"
      :class="{ 'number-box__btn--disabled': value <= min }"
      @click="decrease"
    >
      <text class="number-box__btn-text">-</text>
    </view>
    <view class="number-box__input">
      <text class="number-box__value">{{ value }}</text>
    </view>
    <view
      class="number-box__btn number-box__btn--plus"
      :class="{ 'number-box__btn--disabled': value >= max }"
      @click="increase"
    >
      <text class="number-box__btn-text">+</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'NumberBox',
  props: {
    value: {
      type: Number,
      default: 1
    },
    min: {
      type: Number,
      default: 1
    },
    max: {
      type: Number,
      default: 99
    }
  },
  methods: {
    decrease() {
      if (this.value <= this.min) return
      this.$emit('change', this.value - 1)
    },
    increase() {
      if (this.value >= this.max) return
      this.$emit('change', this.value + 1)
    }
  }
}
</script>

<style lang="scss" scoped>
.number-box {
  display: inline-flex;
  align-items: center;

  &__btn {
    width: 52rpx;
    height: 52rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $bg-color-grey;
    border-radius: $border-radius-sm;

    &--disabled {
      opacity: 0.4;
    }
  }

  &__btn-text {
    font-size: $font-size-lg;
    color: $text-color;
    line-height: 1;
  }

  &__input {
    width: 68rpx;
    height: 52rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 8rpx;
    background-color: $bg-color-grey;
    border-radius: $border-radius-sm;
  }

  &__value {
    font-size: $font-size-md;
    color: $text-color;
  }
}
</style>
