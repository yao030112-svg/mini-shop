package com.minishop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品请求 DTO（添加/更新商品）
 */
@Data
public class ProductDTO {

    /** 商品ID（更新时必填） */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 主图URL */
    private String mainImage;

    /** 商品图片列表(JSON数组) */
    private String images;

    /** 状态：1上架 0下架 */
    private Integer status;

    /** 是否热门推荐 */
    private Integer isHot;

    /** 规格列表 */
    private List<SpecDTO> specs;

    /** SKU列表 */
    private List<SkuDTO> skus;

    /**
     * 规格维度 DTO
     */
    @Data
    public static class SpecDTO {
        /** 规格名称 */
        private String name;
        /** 排序 */
        private Integer sortOrder;
        /** 规格值列表 */
        private List<String> values;
    }

    /**
     * SKU DTO
     */
    @Data
    public static class SkuDTO {
        /** SKU价格 */
        private BigDecimal price;
        /** 库存数量 */
        private Integer stock;
        /** 规格描述，如"红色,XL" */
        private String specDesc;
        /** SKU图片 */
        private String image;
        /** 规格值索引列表（对应specs中的值索引） */
        private List<Long> specValueIds;
    }
}
