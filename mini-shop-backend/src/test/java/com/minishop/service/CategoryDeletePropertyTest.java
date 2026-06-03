package com.minishop.service;

import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.CategoryMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 分类删除保护 属性测试
 *
 * **Validates: Requirements 10.3**
 *
 * 属性 10：分类删除保护
 * - 对于任意包含商品的分类，执行删除操作应失败，且该分类数据保持不变
 */
class CategoryDeletePropertyTest {

    /**
     * 属性：对于任意包含商品的分类（productCount > 0），
     * 执行删除操作应抛出 BusinessException（错误码 1004），
     * 且 categoryMapper.deleteById 不应被调用（分类数据保持不变）。
     *
     * **Validates: Requirements 10.3**
     */
    @Property
    void categoryWithProductsShouldNotBeDeleted(
            @ForAll @LongRange(min = 1, max = 999999999L) Long categoryId,
            @ForAll @IntRange(min = 1, max = 10000) int productCount) throws Exception {

        // Mock CategoryMapper
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        when(categoryMapper.countProductsByCategoryId(categoryId)).thenReturn(productCount);

        // 创建 CategoryService 并注入 mock
        CategoryService categoryService = new CategoryService();
        Field mapperField = CategoryService.class.getDeclaredField("categoryMapper");
        mapperField.setAccessible(true);
        mapperField.set(categoryService, categoryMapper);

        // 验证删除操作抛出 BusinessException
        assertThatThrownBy(() -> categoryService.delete(categoryId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS.getCode());
                    assertThat(bex.getMessage()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS.getMessage());
                });

        // 验证 deleteById 从未被调用（分类数据保持不变）
        verify(categoryMapper, never()).deleteById(anyLong());
    }
}
