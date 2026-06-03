<template>
  <view class="empty">
    <image
      v-if="icon"
      class="empty__icon"
      :src="icon"
      mode="aspectFit"
    />
    <view v-else class="empty__default-icon">
      <text class="empty__default-icon-text">∅</text>
    </view>
    <text class="empty__text">{{ text }}</text>
  </view>
</template>

<script>
export default {
  name: 'Empty',
  props: {
    text: {
      type: String,
      default: '暂无数据'
    },
    icon: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="scss" scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  &__icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: $spacing-md;
  }

  &__default-icon {
    width: 200rpx;
    height: 200rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-md;
  }

  &__default-icon-text {
    font-size: 120rpx;
    color: $text-color-placeholder;
  }

  &__text {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }
}
</style>
