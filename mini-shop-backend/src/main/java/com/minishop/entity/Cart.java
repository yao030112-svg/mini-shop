package com.minishop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
public class Cart {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** SKU ID */
    private Long skuId;

    /** 数量 */
    private Integer quantity;

    /** 是否选中：1选中 0未选中 */
    private Integer selected;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ===== 非数据库字段，用于关联查询 =====

    /** SKU价格 */
    private BigDecimal skuPrice;

    /** SKU规格描述 */
    private String skuSpecDesc;

    /** SKU图片 */
    private String skuImage;

    /** SKU库存 */
    private Integer skuStock;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productMainImage;
}
