package com.minishop.mapper;

import com.minishop.entity.ProductSpec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格维度 Mapper 接口
 */
@Mapper
public interface ProductSpecMapper {

    /**
     * 根据商品ID查询规格列表（含规格值）
     */
    List<ProductSpec> selectByProductId(@Param("productId") Long productId);

    /**
     * 新增规格维度
     */
    int insert(ProductSpec productSpec);

    /**
     * 根据商品ID删除所有规格维度
     */
    int deleteByProductId(@Param("productId") Long productId);
}
