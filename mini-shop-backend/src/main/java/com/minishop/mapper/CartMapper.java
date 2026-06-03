package com.minishop.mapper;

import com.minishop.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartMapper {

    /**
     * 查找用户某SKU的购物车条目
     */
    Cart findByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    /**
     * 根据ID查找购物车条目
     */
    Cart findById(@Param("id") Long id);

    /**
     * 新增购物车条目
     */
    int insert(Cart cart);

    /**
     * 更新数量
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 删除购物车条目
     */
    int deleteById(@Param("id") Long id);

    /**
     * 获取用户购物车列表（关联SKU和商品信息）
     */
    List<Cart> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新单个条目选中状态
     */
    int updateSelected(@Param("id") Long id, @Param("selected") Integer selected);

    /**
     * 全选/取消全选
     */
    int updateSelectAll(@Param("userId") Long userId, @Param("selected") Integer selected);

    /**
     * 获取用户已选中的购物车条目
     */
    List<Cart> getSelectedByUserId(@Param("userId") Long userId);

    /**
     * 批量删除购物车条目（下单后清除）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
