/**
 * 购物车状态管理模块
 */
import { getCartList, addToCart, updateQuantity, deleteItem, selectAll, toggleSelect as toggleSelectApi } from '@/api/cart'

export default {
  namespaced: true,

  state: {
    cartList: [],
    totalPrice: 0,
    selectedCount: 0
  },

  mutations: {
    SET_CART_LIST(state, list) {
      state.cartList = list
    },
    SET_TOTAL_PRICE(state, price) {
      state.totalPrice = price
    },
    SET_SELECTED_COUNT(state, count) {
      state.selectedCount = count
    }
  },

  actions: {
    /**
     * 获取购物车列表
     */
    async getCartList({ commit }) {
      const res = await getCartList()
      const { cartList, totalPrice } = res.data
      commit('SET_CART_LIST', cartList || [])
      commit('SET_TOTAL_PRICE', totalPrice || 0)
      // 计算选中数量
      const selectedCount = (cartList || []).filter(item => item.selected).length
      commit('SET_SELECTED_COUNT', selectedCount)
      return res
    },

    /**
     * 添加到购物车
     * @param {number} skuId - SKU ID
     */
    async addToCart({ dispatch }, skuId) {
      const res = await addToCart(skuId)
      // 添加成功后刷新购物车列表
      await dispatch('getCartList')
      return res
    },

    /**
     * 更新购物车数量
     * @param {Object} payload - { id, quantity }
     */
    async updateQuantity({ dispatch }, { id, quantity }) {
      const res = await updateQuantity(id, quantity)
      await dispatch('getCartList')
      return res
    },

    /**
     * 删除购物车条目
     * @param {number} id - 购物车条目 ID
     */
    async deleteItem({ dispatch }, id) {
      const res = await deleteItem(id)
      await dispatch('getCartList')
      return res
    },

    /**
     * 全选/取消全选
     * @param {boolean} selected - 是否选中
     */
    async toggleSelectAll({ dispatch }, selected) {
      const res = await selectAll(selected)
      await dispatch('getCartList')
      return res
    },

    /**
     * 切换单个条目选中状态
     * @param {Object} payload - { id, selected }
     */
    async toggleSelect({ dispatch }, { id, selected }) {
      const res = await toggleSelectApi(id, selected)
      await dispatch('getCartList')
      return res
    }
  }
}
