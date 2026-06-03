package com.minishop.mapper;

import com.minishop.entity.SpecValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 规格值 Mapper 接口
 */
@Mapper
public interface SpecValueMapper {

    /**
     * 根据规格维度ID查询规格值列表
     */
    List<SpecValue> selectBySpecId(@Param("specId") Long specId);

    /**
     * 新增规格值
     */
    int insert(SpecValue specValue);

    /**
     * 根据规格维度ID删除所有规格值
     */
    int deleteBySpecId(@Param("specId") Long specId);

    /**
     * 根据商品ID删除所有规格值（通过关联product_spec）
     */
    int deleteByProductId(@Param("productId") Long productId);
}
