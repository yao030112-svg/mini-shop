package com.minishop.entity;

import lombok.Data;

/**
 * SKU与规格值关联实体类
 */
@Data
public class SkuSpecValue {

    /** 主键ID */
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 规格值ID */
    private Long specValueId;
}
