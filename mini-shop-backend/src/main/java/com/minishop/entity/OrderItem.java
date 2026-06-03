package com.minishop.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体类
 */
@Data
public class OrderItem {

    /** 主键ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** SKU ID */
    private Long skuId;

    /** 商品名称快照 */
    private String productName;

    /** 规格描述快照 */
    private String skuSpecDesc;

    /** 下单时单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 商品图片快照 */
    private String image;
}
