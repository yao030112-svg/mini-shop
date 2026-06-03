<template>
  <view class="category">
    <!-- 左侧分类列表 -->
    <scroll-view class="category__left" scroll-y>
      <view
        class="category__item"
        :class="{ 'category__item--active': currentIndex === index }"
        v-for="(item, index) in categoryList"
        :key="item.id"
        @click="onCategoryClick(index)"
      >
        <text class="category__item-text">{{ item.name }}</text>
      </view>
    </scroll-view>

    <!-- 右侧商品列表 -->
    <scroll-view
      class="category__right"
      scroll-y
      @scrolltolower="loadMore"
      :scroll-top="scrollTop"
    >
      <view class="product-grid" v-if="productList.length > 0">
        <view class="product-grid__item" v-for="item in productList" :key="item.id">
          <ProductCard :product="item" />
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && productList.length === 0">
        <text class="empty__text">该分类下暂无商品</text>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="productList.length > 0 || loading">
        <text v-if="loading" class="load-status__text">加载中...</text>
        <text v-else-if="noMore" class="load-status__text">— 没有更多了 —</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getCategoryList, getProductsByCategory } from '@/api/product'

export default {
  data() {
    return {
      categoryList: [],
      currentIndex: 0,
      productList: [],
      page: 1,
      pageSize: 20,
      loading: false,
      noMore: false,
      scrollTop: 0
    }
  },

  onLoad() {
    this.loadCategories()
  },

  methods: {
    // 加载分类列表
    async loadCategories() {
      try {
        const res = await getCategoryList()
        if (res.code === 200) {
          this.categoryList = res.data || []
          // 默认选中第一个分类并加载商品
          if (this.categoryList.length > 0) {
            this.loadProducts()
          }
        }
      } catch (e) {
        console.error('加载分类列表失败', e)
      }
    },

    // 点击分类切换
    onCategoryClick(index) {
      if (this.currentIndex === index) return
      this.currentIndex = index
      this.resetProducts()
      this.loadProducts()
    },

    // 重置商品列表状态
    resetProducts() {
      this.productList = []
      this.page = 1
      this.noMore = false
      // 滚动到顶部
      this.scrollTop = this.scrollTop === 0 ? -1 : 0
    },

    // 加载商品列表
    async loadProducts() {
      if (this.loading || this.noMore) return
      const category = this.categoryList[this.currentIndex]
      if (!category) return

      this.loading = true
      try {
        const res = await getProductsByCategory(category.id, this.page, this.pageSize)
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

    // 上拉加载更多
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.loadProducts()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.category {
  display: flex;
  height: 100vh;
  background-color: $bg-color;
}

/* 左侧分类列表 */
.category__left {
  width: 180rpx;
  height: 100%;
  background-color: $bg-color-grey;
  flex-shrink: 0;
}

.category__item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  padding: 0 $spacing-xs;
  position: relative;

  &--active {
    background-color: $bg-color-white;

    .category__item-text {
      color: $primary-color;
      font-weight: bold;
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 40rpx;
      background-color: $primary-color;
      border-radius: 0 $border-radius-sm $border-radius-sm 0;
    }
  }
}

.category__item-text {
  font-size: $font-size-md;
  color: $text-color;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧商品列表 */
.category__right {
  flex: 1;
  height: 100%;
  padding: $spacing-sm;
  box-sizing: border-box;
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

/* 空状态 */
.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;

  &__text {
    font-size: $font-size-md;
    color: $text-color-lighter;
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
