package com.minishop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
public class Product {

    /** 主键ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 主图URL */
    private String mainImage;

    /** 商品图片列表(JSON数组) */
    private String images;

    /** 最低价格(冗余，取所有SKU最低价) */
    private BigDecimal minPrice;

    /** 状态：1上架 0下架 */
    private Integer status;

    /** 是否热门推荐 */
    private Integer isHot;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除 1已删除 */
    private Integer deleted;
}
