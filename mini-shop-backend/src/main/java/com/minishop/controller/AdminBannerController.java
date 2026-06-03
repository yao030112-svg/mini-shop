package com.minishop.controller;

import com.minishop.entity.Banner;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.security.UserContext;
import com.minishop.service.BannerService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端轮播图控制器（需要管理员权限）
 */
@RestController
@RequestMapping("/api/admin/banner")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        checkAdmin();
        List<Banner> banners = bannerService.getList();
        return Result.success(banners);
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Banner banner) {
        checkAdmin();
        bannerService.add(banner);
        return Result.success();
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Banner banner) {
        checkAdmin();
        bannerService.update(banner);
        return Result.success();
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        bannerService.delete(id);
        return Result.success();
    }

    /**
     * 更新轮播图状态
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        bannerService.updateStatus(id, status);
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
