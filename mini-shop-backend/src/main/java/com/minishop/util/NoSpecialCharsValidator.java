package com.minishop.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * NoSpecialChars 注解的校验器实现
 * 检测SQL注入特征字符和关键字
 */
public class NoSpecialCharsValidator implements ConstraintValidator<NoSpecialChars, String> {

    /**
     * SQL注入特征正则：匹配单引号、双引号、分号、注释符号、常见SQL关键字
     */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(['\";]|--|/\\*|\\*/|\\b(DROP|SELECT|INSERT|UPDATE|DELETE|UNION|ALTER|CREATE|EXEC|EXECUTE|TRUNCATE)\\b)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void initialize(NoSpecialChars constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            // null/空值由 @NotBlank 等注解处理
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(value).find();
    }
}
