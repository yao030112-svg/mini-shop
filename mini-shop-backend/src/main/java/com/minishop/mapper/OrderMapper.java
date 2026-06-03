package com.minishop.mapper;

import com.minishop.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单
     */
    int insert(Order order);

    /**
     * 根据ID查询订单
     */
    Order findById(@Param("id") Long id);

    /**
     * 根据订单编号查询订单
     */
    Order findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户订单列表
     */
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询订单列表（status为null时查全部）
     */
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新支付时间
     */
    int updatePaymentTime(@Param("id") Long id, @Param("paymentTime") LocalDateTime paymentTime);

    /**
     * 查询超时未支付的订单（超过30分钟未支付）
     */
    List<Order> selectTimeoutOrders(@Param("deadline") LocalDateTime deadline);

    /**
     * 管理员查询订单列表（支持 status、userId 筛选）
     */
    List<Order> selectAdminList(@Param("status") Integer status, @Param("userId") Long userId);

    /**
     * 更新发货时间
     */
    int updateShippingTime(@Param("id") Long id, @Param("shippingTime") LocalDateTime shippingTime);

    /**
     * 统计所有订单数量
     */
    int countAll();

    /**
     * 统计总收入（已支付、已发货、已完成状态的订单）
     */
    BigDecimal sumTotalRevenue();

    /**
     * 统计指定时间范围内的订单数量
     */
    int countByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的收入
     */
    BigDecimal sumRevenueByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
