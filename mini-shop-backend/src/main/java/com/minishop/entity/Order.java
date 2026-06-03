package com.minishop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
public class Order {

    /** 主键ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 状态：0待支付 1已支付 2已发货 3已完成 4已取消 5已退款 */
    private Integer status;

    /** 收货地址快照(JSON) */
    private String addressSnapshot;

    /** 支付时间 */
    private LocalDateTime paymentTime;

    /** 发货时间 */
    private LocalDateTime shippingTime;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
