package com.minishop.mapper;

import com.minishop.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 轮播图 Mapper 接口
 */
@Mapper
public interface BannerMapper {

    /**
     * 查询所有启用的轮播图（按 sort_order 降序排列）
     */
    List<Banner> selectActiveList();

    /**
     * 查询所有轮播图列表（管理端使用）
     */
    List<Banner> selectList();

    /**
     * 根据ID查询轮播图
     */
    Banner findById(@Param("id") Long id);

    /**
     * 新增轮播图
     */
    int insert(Banner banner);

    /**
     * 更新轮播图
     */
    int update(Banner banner);

    /**
     * 删除轮播图
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新轮播图状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
