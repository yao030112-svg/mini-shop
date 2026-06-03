package com.minishop.entity;

import lombok.Data;

/**
 * 规格值实体类
 */
@Data
public class SpecValue {

    /** 主键ID */
    private Long id;

    /** 规格维度ID */
    private Long specId;

    /** 规格值，如红色、XL */
    private String value;

    /** 排序 */
    private Integer sortOrder;
}
