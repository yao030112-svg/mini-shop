package com.minishop.vo;

import com.minishop.entity.Order;
import com.minishop.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 订单详情VO（包含订单信息和订单项列表）
 */
@Data
public class OrderDetailVO {

    /** 订单信息 */
    private Order order;

    /** 订单项列表 */
    private List<OrderItem> items;

    public OrderDetailVO(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }
}
