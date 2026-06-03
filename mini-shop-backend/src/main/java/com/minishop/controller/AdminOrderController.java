package com.minishop.controller;

import com.minishop.entity.Order;
import com.minishop.entity.OrderItem;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.security.UserContext;
import com.minishop.service.OrderService;
import com.minishop.vo.OrderDetailVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端订单控制器（需要管理员权限）
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 管理员订单列表（支持状态、用户筛选）
     */
    @GetMapping("/list")
    public Result<List<Order>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {
        checkAdmin();
        List<Order> orders = orderService.getAdminOrderList(status, userId);
        return Result.success(orders);
    }

    /**
     * 获取订单详情（含订单项和地址信息）
     */
    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        checkAdmin();
        OrderDetailVO detail = orderService.getOrderDetailForAdmin(id);
        return Result.success(detail);
    }

    /**
     * 发货（已支付→已发货）
     */
    @PutMapping("/ship/{id}")
    public Result<Void> ship(@PathVariable Long id) {
        checkAdmin();
        orderService.shipOrder(id);
        return Result.success();
    }

    /**
     * 退款（恢复库存）
     */
    @PutMapping("/refund/{id}")
    public Result<Void> refund(@PathVariable Long id) {
        checkAdmin();
        orderService.refundOrder(id);
        return Result.success();
    }

    /**
     * 检查当前用户是否为管理员
     */
    private void checkAdmin() {
        Integer role = UserContext.getCurrentRole();
        if (role == null || role != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
