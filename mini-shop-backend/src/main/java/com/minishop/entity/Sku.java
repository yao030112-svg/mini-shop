package com.minishop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU实体类
 */
@Data
public class Sku {

    /** 主键ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** SKU价格 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 规格描述，如"红色,XL" */
    private String specDesc;

    /** SKU图片 */
    private String image;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 规格值列表（非数据库字段，用于关联查询） */
    private List<SkuSpecValue> specValues;
}
