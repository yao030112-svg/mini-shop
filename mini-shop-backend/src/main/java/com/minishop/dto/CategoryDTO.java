package com.minishop.dto;

import com.minishop.util.NoSpecialChars;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 分类请求 DTO（添加/更新分类）
 */
@Data
public class CategoryDTO {

    /**
     * 分类ID（更新时必填）
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 64, message = "分类名称长度必须在1-64之间")
    @NoSpecialChars
    private String name;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 排序权重，越大越靠前
     */
    private Integer sortOrder;
}
