package com.minishop.service;

import com.minishop.util.NoSpecialCharsValidator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * 输入校验属性测试
 *
 * **Validates: Requirements 13.5**
 *
 * 属性 17：输入校验拒绝非法数据 - SQL注入特征字符或超长输入应被拒绝
 */
class InputValidationPropertyTest {

    private final NoSpecialCharsValidator validator = new NoSpecialCharsValidator();

    /**
     * 属性 17：包含SQL注入特征字符的字符串应被拒绝
     * 测试包含单引号、双引号、分号、注释符号的输入
     *
     * **Validates: Requirements 13.5**
     */
    @Property
    void shouldRejectInputWithSqlInjectionCharacters(
            @ForAll("sqlInjectionInputs") String maliciousInput) {

        boolean result = validator.isValid(maliciousInput, null);

        assertThat(result).isFalse();
    }

    /**
     * 属性 17（补充）：包含SQL关键字的字符串应被拒绝
     *
     * **Validates: Requirements 13.5**
     */
    @Property
    void shouldRejectInputWithSqlKeywords(
            @ForAll("sqlKeywordInputs") String maliciousInput) {

        boolean result = validator.isValid(maliciousInput, null);

        assertThat(result).isFalse();
    }

    /**
     * 属性 17（补充）：正常字符串应通过校验
     *
     * **Validates: Requirements 13.5**
     */
    @Property
    void shouldAcceptNormalInput(
            @ForAll("normalInputs") String normalInput) {

        boolean result = validator.isValid(normalInput, null);

        assertThat(result).isTrue();
    }

    /**
     * 属性 17（补充）：null和空字符串应通过校验（由其他注解处理）
     *
     * **Validates: Requirements 13.5**
     */
    @Property
    void shouldAcceptNullAndEmpty() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Provide
    Arbitrary<String> sqlInjectionInputs() {
        // 生成包含SQL注入特征字符的字符串
        Arbitrary<String> prefixes = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(10);
        Arbitrary<String> suffixes = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(10);
        Arbitrary<String> injectionChars = Arbitraries.of(
                "'", "\"", ";", "--", "/*", "*/"
        );

        return Combinators.combine(prefixes, injectionChars, suffixes)
                .as((prefix, injection, suffix) -> prefix + injection + suffix);
    }

    @Provide
    Arbitrary<String> sqlKeywordInputs() {
        // 生成包含SQL关键字的字符串
        Arbitrary<String> prefixes = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(5);
        Arbitrary<String> suffixes = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(5);
        Arbitrary<String> keywords = Arbitraries.of(
                " DROP ", " SELECT ", " INSERT ", " UPDATE ",
                " DELETE ", " UNION ", " ALTER ", " CREATE ",
                " EXEC ", " EXECUTE ", " TRUNCATE "
        );

        return Combinators.combine(prefixes, keywords, suffixes)
                .as((prefix, keyword, suffix) -> prefix + keyword + suffix);
    }

    @Provide
    Arbitrary<String> normalInputs() {
        // 生成不包含SQL注入特征的正常字符串（中文、字母、数字、空格）
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '.', ',', '!', '?', '(', ')', '-')
                .withCharRange('\u4e00', '\u9fff') // 中文字符
                .ofMinLength(1)
                .ofMaxLength(50)
                .filter(s -> !s.contains("--") && !s.contains("/*") && !s.contains("*/"));
    }
}
