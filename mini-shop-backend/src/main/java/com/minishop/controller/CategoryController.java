package com.minishop.controller;

import com.minishop.entity.Category;
import com.minishop.service.CategoryService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端分类控制器（公开接口，无需认证）
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表（按 sort_order 降序排列）
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.getList();
        return Result.success(categories);
    }
}
