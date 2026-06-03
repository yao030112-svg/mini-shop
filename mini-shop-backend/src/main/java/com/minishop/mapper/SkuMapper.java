package com.minishop.mapper;

import com.minishop.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU Mapper 接口
 */
@Mapper
public interface SkuMapper {

    /**
     * 根据商品ID查询SKU列表
     */
    List<Sku> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据ID查询SKU
     */
    Sku findById(@Param("id") Long id);

    /**
     * 新增SKU
     */
    int insert(Sku sku);

    /**
     * 根据商品ID删除所有SKU
     */
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * 查询商品的最低价格
     */
    BigDecimal selectMinPriceByProductId(@Param("productId") Long productId);

    /**
     * 乐观锁扣减库存
     * 返回影响行数，为0表示库存不足
     */
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 恢复库存
     */
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
