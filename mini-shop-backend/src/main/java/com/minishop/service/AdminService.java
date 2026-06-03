package com.minishop.service;

import com.minishop.mapper.OrderMapper;
import com.minishop.mapper.ProductMapper;
import com.minishop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端服务
 */
@Service
public class AdminService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取管理端统计数据
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        stats.put("orderCount", orderMapper.countAll());

        // 商品总数
        stats.put("productCount", productMapper.countAll());

        // 用户总数
        stats.put("userCount", userMapper.countAll());

        // 总收入（已支付、已发货、已完成状态的订单）
        BigDecimal totalRevenue = orderMapper.sumTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 今日数据
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 今日订单数
        stats.put("todayOrders", orderMapper.countByDateRange(startOfDay, endOfDay));

        // 今日收入
        BigDecimal todayRevenue = orderMapper.sumRevenueByDateRange(startOfDay, endOfDay);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // 今日新增用户
        stats.put("todayUsers", userMapper.countByDateRange(startOfDay, endOfDay));

        return stats;
    }
}
