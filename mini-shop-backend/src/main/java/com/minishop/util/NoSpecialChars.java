package com.minishop.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义校验注解：过滤SQL注入特征字符
 * 拒绝包含 '、"、;、--、/*、*\/、DROP、SELECT、INSERT、UPDATE、DELETE、UNION 等关键字的输入
 */
@Documented
@Constraint(validatedBy = NoSpecialCharsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSpecialChars {

    String message() default "输入包含非法字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
