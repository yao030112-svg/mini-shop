package com.minishop.controller;

import com.minishop.entity.Product;
import com.minishop.service.ProductService;
import com.minishop.vo.ProductDetailVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端商品控制器（公开接口，无需认证）
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 热门商品推荐
     */
    @GetMapping("/hot")
    public Result<List<Product>> hot() {
        List<Product> products = productService.getHotProducts();
        return Result.success(products);
    }

    /**
     * 商品列表（分页）
     */
    @GetMapping("/list")
    public Result<List<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<Product> products = productService.getProductList(page, pageSize);
        return Result.success(products);
    }

    /**
     * 商品详情（含规格和SKU信息）
     */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.success(detail);
    }

    /**
     * 按分类查询商品（分页）
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<Product> products = productService.getProductsByCategory(categoryId, page, pageSize);
        return Result.success(products);
    }
}
