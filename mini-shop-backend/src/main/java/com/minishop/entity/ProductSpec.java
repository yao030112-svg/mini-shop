package com.minishop.entity;

import lombok.Data;

import java.util.List;

/**
 * 商品规格维度实体类
 */
@Data
public class ProductSpec {

    /** 主键ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 规格名称，如颜色、尺码 */
    private String name;

    /** 排序 */
    private Integer sortOrder;

    /** 规格值列表（非数据库字段，用于嵌套查询） */
    private List<SpecValue> values;
}
