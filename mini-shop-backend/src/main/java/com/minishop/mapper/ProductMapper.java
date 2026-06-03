package com.minishop.mapper;

import com.minishop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper {

    /**
     * 查询热门商品（status=1, deleted=0, is_hot=1）
     */
    List<Product> selectHotProducts();

    /**
     * 分页查询上架商品（status=1, deleted=0）
     */
    List<Product> selectListByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据ID查询商品详情（deleted=0）
     */
    Product findById(@Param("id") Long id);

    /**
     * 按分类分页查询上架商品（status=1, deleted=0）
     */
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId,
                                     @Param("offset") int offset,
                                     @Param("pageSize") int pageSize);

    /**
     * 管理端分页查询所有商品（deleted=0，不限status）
     */
    List<Product> selectAdminList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 管理端查询商品总数
     */
    int countAll();

    /**
     * 新增商品
     */
    int insert(Product product);

    /**
     * 更新商品
     */
    int update(Product product);

    /**
     * 软删除商品
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新商品状态（上下架）
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新商品最低价格
     */
    int updateMinPrice(@Param("id") Long id, @Param("minPrice") BigDecimal minPrice);
}
