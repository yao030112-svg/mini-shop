package com.minishop.controller;

import com.minishop.dto.CategoryDTO;
import com.minishop.entity.Category;
import com.minishop.entity.Product;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.security.UserContext;
import com.minishop.service.CategoryService;
import com.minishop.service.ProductService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

/**
 * 管理端分类控制器（需要管理员权限）
 */
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        checkAdmin();
        List<Category> categories = categoryService.getList();
        return Result.success(categories);
    }

    /**
     * 添加分类
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CategoryDTO categoryDTO) {
        checkAdmin();
        categoryService.add(categoryDTO);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CategoryDTO categoryDTO) {
        checkAdmin();
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类（检查是否有商品）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 获取分类下的商品列表
     */
    @GetMapping("/{categoryId}/products")
    public Result<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        checkAdmin();
        List<Product> products = productService.getProductsByCategory(categoryId, 1, 100);
        return Result.success(products);
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
