package com.minishop.controller;

import com.minishop.dto.OrderCreateRequest;
import com.minishop.entity.Order;
import com.minishop.security.UserContext;
import com.minishop.service.OrderService;
import com.minishop.vo.OrderDetailVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 订单控制器（需认证）
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Order order = orderService.createOrder(userId, request.getAddressId());
        return Result.success(order);
    }

    /**
     * 模拟支付
     */
    @PostMapping("/pay/{id}")
    public Result<Order> pay(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        Order order = orderService.payOrder(id, userId);
        return Result.success(order);
    }

    /**
     * 订单列表（按时间倒序，可选状态筛选）
     */
    @GetMapping("/list")
    public Result<List<Order>> list(@RequestParam(required = false) Integer status) {
        Long userId = UserContext.getCurrentUserId();
        List<Order> orders = orderService.getOrderList(userId, status);
        return Result.success(orders);
    }

    /**
     * 订单详情（含订单项列表）
     */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        OrderDetailVO detail = orderService.getOrderDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        orderService.cancelOrder(id, userId);
        return Result.success();
    }
}
