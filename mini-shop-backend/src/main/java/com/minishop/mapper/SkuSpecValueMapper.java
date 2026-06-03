package com.minishop.mapper;

import com.minishop.entity.SkuSpecValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SKU与规格值关联 Mapper 接口
 */
@Mapper
public interface SkuSpecValueMapper {

    /**
     * 根据SKU ID查询关联的规格值
     */
    List<SkuSpecValue> selectBySkuId(@Param("skuId") Long skuId);

    /**
     * 新增SKU与规格值关联
     */
    int insert(SkuSpecValue skuSpecValue);

    /**
     * 根据SKU ID删除关联
     */
    int deleteBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据商品ID删除所有SKU的规格值关联
     */
    int deleteByProductId(@Param("productId") Long productId);
}
