package com.minishop.dto;

import com.minishop.util.NoSpecialChars;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 新增/更新地址请求DTO
 */
@Data
public class AddressDTO {

    /** 地址ID（更新时必填） */
    private Long id;

    /** 收货人姓名 */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(min = 1, max = 32, message = "收货人姓名长度必须在1-32之间")
    @NoSpecialChars
    private String receiverName;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 省 */
    @NotBlank(message = "省份不能为空")
    @NoSpecialChars
    private String province;

    /** 市 */
    @NotBlank(message = "城市不能为空")
    @NoSpecialChars
    private String city;

    /** 区 */
    @NotBlank(message = "区县不能为空")
    @NoSpecialChars
    private String district;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    @NoSpecialChars
    private String detail;

    /** 是否默认地址：1是 0否 */
    private Integer isDefault;
}
