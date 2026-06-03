package com.minishop.vo;

import com.minishop.entity.Product;
import com.minishop.entity.ProductSpec;
import com.minishop.entity.Sku;
import lombok.Data;

import java.util.List;

/**
 * 商品详情 VO（包含规格和SKU信息）
 */
@Data
public class ProductDetailVO {

    /** 商品基本信息 */
    private Product product;

    /** 规格列表（含规格值） */
    private List<ProductSpec> specs;

    /** SKU列表 */
    private List<Sku> skus;
}
