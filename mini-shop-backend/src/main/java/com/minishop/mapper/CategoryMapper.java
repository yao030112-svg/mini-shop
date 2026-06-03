package com.minishop.mapper;

import com.minishop.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper 接口
 */
@Mapper
public interface CategoryMapper {

    /**
     * 查询所有分类列表（按 sort_order 降序排列）
     */
    List<Category> selectList();

    /**
     * 根据ID查询分类
     */
    Category findById(@Param("id") Long id);

    /**
     * 新增分类
     */
    int insert(Category category);

    /**
     * 更新分类
     */
    int update(Category category);

    /**
     * 逻辑删除分类
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计分类下商品数量
     */
    int countProductsByCategoryId(@Param("categoryId") Long categoryId);
}
