package com.minishop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
public class Category {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 排序权重，越大越靠前
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除
     */
    private Integer deleted;
}
