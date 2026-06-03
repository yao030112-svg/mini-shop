<template>
  <view class="address-list">
    <!-- 地址列表 -->
    <view v-if="addressList.length > 0" class="list-content">
      <view
        class="address-card"
        v-for="item in addressList"
        :key="item.id"
        @click="handleSelect(item)"
      >
        <view class="address-card__content">
          <view class="address-card__top">
            <text class="address-card__name">{{ item.receiverName }}</text>
            <text class="address-card__phone">{{ item.phone }}</text>
            <view class="address-card__tag" v-if="item.isDefault">
              <text class="address-card__tag-text">默认</text>
            </view>
          </view>
          <view class="address-card__detail">
            <text class="address-card__detail-text">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}</text>
          </view>
        </view>
        <view class="address-card__actions">
          <view class="address-card__action" @click.stop="handleSetDefault(item.id)" v-if="!item.isDefault">
            <text class="address-card__action-text">设为默认</text>
          </view>
          <view class="address-card__action" @click.stop="handleEdit(item.id)">
            <text class="address-card__action-text">编辑</text>
          </view>
          <view class="address-card__action address-card__action--danger" @click.stop="handleDelete(item.id)">
            <text class="address-card__action-text address-card__action-text--danger">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text class="empty__text">暂无收货地址</text>
      <text class="empty__sub">点击下方按钮添加地址</text>
    </view>

    <!-- 底部新增按钮 -->
    <view class="bottom-bar">
      <view class="add-btn" @click="handleAdd">
        <text class="add-btn__text">新增地址</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getAddressList, deleteAddress, setDefault } from '@/api/address'

export default {
  data() {
    return {
      addressList: [],
      from: '' // 来源页面标识
    }
  },

  onLoad(options) {
    if (options.from) {
      this.from = options.from
    }
  },

  onShow() {
    this.loadAddressList()
  },

  methods: {
    // 加载地址列表
    async loadAddressList() {
      try {
        uni.showLoading({ title: '加载中' })
        const res = await getAddressList()
        if (res.code === 200) {
          this.addressList = res.data || []
        }
      } catch (e) {
        console.error('加载地址列表失败', e)
      } finally {
        uni.hideLoading()
      }
    },

    // 选择地址（从订单确认页进入时）
    handleSelect(item) {
      if (this.from === 'order') {
        uni.$emit('selectAddress', item)
        uni.navigateBack()
      }
    },

    // 新增地址
    handleAdd() {
      uni.navigateTo({ url: '/pages/address/edit' })
    },

    // 编辑地址
    handleEdit(id) {
      uni.navigateTo({ url: `/pages/address/edit?id=${id}` })
    },

    // 删除地址
    handleDelete(id) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该地址吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await deleteAddress(id)
              if (result.code === 200) {
                uni.showToast({ title: '已删除', icon: 'success' })
                this.loadAddressList()
              } else {
                uni.showToast({ title: result.message || '删除失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
    },

    // 设为默认地址
    async handleSetDefault(id) {
      try {
        const res = await setDefault(id)
        if (res.code === 200) {
          uni.showToast({ title: '设置成功', icon: 'success' })
          this.loadAddressList()
        } else {
          uni.showToast({ title: res.message || '设置失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '设置失败', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.address-list {
  min-height: 100vh;
  background-color: $bg-color;
  padding-bottom: 120rpx;
}

.list-content {
  padding: $spacing-sm;
}

/* 地址卡片 */
.address-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
  overflow: hidden;

  &__content {
    padding: $spacing-md;
  }

  &__top {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
  }

  &__name {
    font-size: $font-size-lg;
    color: $text-color;
    font-weight: bold;
    margin-right: $spacing-md;
  }

  &__phone {
    font-size: $font-size-md;
    color: $text-color-light;
  }

  &__tag {
    margin-left: $spacing-sm;
    background-color: rgba(255, 102, 0, 0.1);
    padding: 2rpx 12rpx;
    border-radius: $border-radius-sm;
  }

  &__tag-text {
    font-size: $font-size-xs;
    color: $primary-color;
  }

  &__detail {
    padding-right: $spacing-md;
  }

  &__detail-text {
    font-size: $font-size-sm;
    color: $text-color-light;
    line-height: 1.5;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: $spacing-sm $spacing-md;
    border-top: 1rpx solid $border-color-light;
  }

  &__action {
    padding: 8rpx 20rpx;
    margin-left: $spacing-sm;

    &--danger {
      /* danger style handled by text */
    }
  }

  &__action-text {
    font-size: $font-size-sm;
    color: $text-color-light;

    &--danger {
      color: $error-color;
    }
  }
}

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;

  &__text {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $text-color-placeholder;
    margin-top: 12rpx;
  }
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-sm $spacing-md;
  padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));
  background-color: $bg-color-white;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.add-btn {
  background-color: $primary-color;
  border-radius: $border-radius-round;
  padding: $spacing-md;
  text-align: center;

  &__text {
    font-size: $font-size-md;
    color: #ffffff;
    font-weight: bold;
  }
}
</style>
