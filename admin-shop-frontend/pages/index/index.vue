<template>
  <view class="index">
    <!-- 轮播图区域 -->
    <swiper
      class="banner-swiper"
      :indicator-dots="true"
      :autoplay="true"
      :interval="3000"
      :duration="500"
      indicator-color="rgba(255,255,255,0.5)"
      indicator-active-color="#ffffff"
      circular
    >
      <swiper-item v-for="item in bannerList" :key="item.id" @click="onBannerClick(item)">
        <image class="banner-image" :src="item.imageUrl" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 热门商品推荐 -->
    <view class="section">
      <view class="section__header">
        <text class="section__title">热门推荐</text>
      </view>

      <view class="product-grid">
        <view class="product-grid__item" v-for="item in productList" :key="item.id">
          <ProductCard :product="item" />
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status">
        <text v-if="loading" class="load-status__text">加载中...</text>
        <text v-else-if="noMore" class="load-status__text">— 没有更多了 —</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getBannerList, getProductList } from '@/api/product'

export default {
  data() {
    return {
      bannerList: [],
      productList: [],
      page: 1,
      pageSize: 20,
      loading: false,
      noMore: false
    }
  },

  onLoad() {
    this.loadBanners()
    this.loadProducts()
  },

  onPullDownRefresh() {
    this.refreshData()
  },

  onReachBottom() {
    this.loadMore()
  },

  methods: {
    // 加载轮播图数据
    async loadBanners() {
      try {
        const res = await getBannerList()
        if (res.code === 200) {
          this.bannerList = res.data || []
        }
      } catch (e) {
        console.error('加载轮播图失败', e)
      }
    },

    // 加载商品列表
    async loadProducts() {
      if (this.loading || this.noMore) return
      this.loading = true
      try {
        const res = await getProductList(this.page, this.pageSize)
        if (res.code === 200) {
          const list = res.data || []
          if (list.length < this.pageSize) {
            this.noMore = true
          }
          this.productList = [...this.productList, ...list]
          this.page++
        }
      } catch (e) {
        console.error('加载商品列表失败', e)
      } finally {
        this.loading = false
      }
    },

    // 下拉刷新
    async refreshData() {
      this.page = 1
      this.noMore = false
      this.productList = []
      await Promise.all([this.loadBanners(), this.loadProducts()])
      uni.stopPullDownRefresh()
    },

    // 上拉加载更多
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.loadProducts()
      }
    },

    // 轮播图点击
    onBannerClick(item) {
      if (item.linkUrl) {
        uni.navigateTo({ url: item.linkUrl })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.index {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 轮播图 */
.banner-swiper {
  width: 100%;
  height: 360rpx;
}

.banner-image {
  width: 100%;
  height: 100%;
}

/* 区块 */
.section {
  padding: $spacing-sm;

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: bold;
    color: $text-color;
  }
}

/* 商品网格 - 两列布局 */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  &__item {
    width: 48.5%;
    margin-bottom: $spacing-sm;
  }
}

/* 加载状态 */
.load-status {
  padding: $spacing-lg 0;
  text-align: center;

  &__text {
    font-size: $font-size-sm;
    color: $text-color-lighter;
  }
}
</style>
