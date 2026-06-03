package com.minishop.controller;

import com.minishop.dto.ProductDTO;
import com.minishop.entity.Product;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.security.UserContext;
import com.minishop.service.ProductService;
import com.minishop.vo.PageVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端商品控制器（需要管理员权限）
 */
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表（分页）
     */
    @GetMapping("/list")
    public Result<PageVO<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        checkAdmin();
        PageVO<Product> pageVO = productService.getAdminProductList(page, pageSize);
        return Result.success(pageVO);
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ProductDTO productDTO) {
        checkAdmin();
        productService.addProduct(productDTO);
        return Result.success();
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody ProductDTO productDTO) {
        checkAdmin();
        productService.updateProduct(productDTO);
        return Result.success();
    }

    /**
     * 删除商品（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        productService.deleteProduct(id);
        return Result.success();
    }

    /**
     * 上下架商品
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        productService.updateStatus(id, status);
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
