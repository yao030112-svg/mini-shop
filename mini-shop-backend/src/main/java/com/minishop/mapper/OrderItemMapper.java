package com.minishop.mapper;

import com.minishop.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 批量插入订单项
     */
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 根据订单ID查询订单项列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
