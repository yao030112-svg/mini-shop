package com.minishop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
public class Banner {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
