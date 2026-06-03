package com.minishop.controller;

import com.minishop.entity.Banner;
import com.minishop.service.BannerService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 轮播图控制器（公开接口，无需认证）
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表（仅返回启用状态，按 sort_order 降序）
     */
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        List<Banner> banners = bannerService.getActiveList();
        return Result.success(banners);
    }
}
