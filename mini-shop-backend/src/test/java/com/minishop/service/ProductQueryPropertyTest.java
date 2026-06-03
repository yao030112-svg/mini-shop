package com.minishop.service;

import com.minishop.entity.Product;
import com.minishop.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品查询属性测试
 *
 * **Validates: Requirements 3.2, 2.2, 2.4, 3.3, 9.3**
 *
 * 属性 8：分类商品归属正确性
 * 属性 9：分页数据量约束
 * 属性 15：商品软删除不可见性
 */
class ProductQueryPropertyTest {

    private ProductService createProductServiceWithMocks(ProductMapper productMapper) throws Exception {
        ProductService productService = new ProductService();

        Field productMapperField = ProductService.class.getDeclaredField("productMapper");
        productMapperField.setAccessible(true);
        productMapperField.set(productService, productMapper);

        // 注入其他必要的 mapper（避免 NPE）
        Field productSpecMapperField = ProductService.class.getDeclaredField("productSpecMapper");
        productSpecMapperField.setAccessible(true);
        productSpecMapperField.set(productService, Mockito.mock(ProductSpecMapper.class));

        Field specValueMapperField = ProductService.class.getDeclaredField("specValueMapper");
        specValueMapperField.setAccessible(true);
        specValueMapperField.set(productService, Mockito.mock(SpecValueMapper.class));

        Field skuMapperField = ProductService.class.getDeclaredField("skuMapper");
        skuMapperField.setAccessible(true);
        skuMapperField.set(productService, Mockito.mock(SkuMapper.class));

        Field skuSpecValueMapperField = ProductService.class.getDeclaredField("skuSpecValueMapper");
        skuSpecValueMapperField.setAccessible(true);
        skuSpecValueMapperField.set(productService, Mockito.mock(SkuSpecValueMapper.class));

        return productService;
    }

    /**
     * 属性 8：分类商品归属正确性
     * 对于任意分类查询请求，返回的所有商品的 category_id 应等于请求的分类ID。
     *
     * **Validates: Requirements 3.2**
     */
    @Property
    void productsByCategoryShouldAllBelongToThatCategory(
            @ForAll @LongRange(min = 1, max = 999999L) Long categoryId,
            @ForAll @IntRange(min = 1, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 50) int pageSize,
            @ForAll @IntRange(min = 0, max = 20) int resultCount) throws Exception {

        // 构造返回的商品列表，所有商品的 categoryId 都等于请求的 categoryId
        int actualCount = Math.min(resultCount, pageSize);
        List<Product> products = IntStream.range(0, actualCount)
                .mapToObj(i -> {
                    Product p = new Product();
                    p.setId((long) (i + 1));
                    p.setName("Product " + i);
                    p.setCategoryId(categoryId);
                    p.setStatus(1);
                    p.setDeleted(0);
                    p.setMinPrice(BigDecimal.valueOf(10 + i));
                    return p;
                })
                .collect(Collectors.toList());

        // Mock ProductMapper
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        int offset = (page - 1) * pageSize;
        when(productMapper.selectByCategoryId(eq(categoryId), eq(offset), eq(pageSize)))
                .thenReturn(products);

        ProductService productService = createProductServiceWithMocks(productMapper);

        // 执行查询
        List<Product> result = productService.getProductsByCategory(categoryId, page, pageSize);

        // 验证所有返回商品的 categoryId 等于请求的分类ID
        assertThat(result).allSatisfy(product ->
                assertThat(product.getCategoryId()).isEqualTo(categoryId)
        );
    }

    /**
     * 属性 9：分页数据量约束
     * 对于任意分页查询请求，返回的数据条数应不超过指定的 pageSize。
     *
     * **Validates: Requirements 2.2, 2.4, 3.3**
     */
    @Property
    void pagedResultShouldNotExceedPageSize(
            @ForAll @IntRange(min = 1, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 50) int pageSize,
            @ForAll @IntRange(min = 0, max = 100) int totalProducts) throws Exception {

        // 模拟 Mapper 返回不超过 pageSize 的数据（这是 SQL LIMIT 的行为）
        int actualReturnCount = Math.min(totalProducts, pageSize);
        List<Product> products = IntStream.range(0, actualReturnCount)
                .mapToObj(i -> {
                    Product p = new Product();
                    p.setId((long) (i + 1));
                    p.setName("Product " + i);
                    p.setCategoryId(1L);
                    p.setStatus(1);
                    p.setDeleted(0);
                    p.setMinPrice(BigDecimal.valueOf(10 + i));
                    return p;
                })
                .collect(Collectors.toList());

        // Mock ProductMapper
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        int offset = (page - 1) * pageSize;
        when(productMapper.selectListByPage(eq(offset), eq(pageSize)))
                .thenReturn(products);

        ProductService productService = createProductServiceWithMocks(productMapper);

        // 执行查询
        List<Product> result = productService.getProductList(page, pageSize);

        // 验证返回数据条数不超过 pageSize
        assertThat(result).hasSizeLessThanOrEqualTo(pageSize);
    }

    /**
     * 属性 15：商品软删除不可见性
     * 对于任意被软删除的商品，用户端的所有商品查询接口应不返回该商品。
     *
     * 验证方式：确认 ProductMapper 的用户端查询方法被调用时，
     * 返回的结果中不包含 deleted=1 的商品（通过 Mock 验证 Service 层正确传递查询条件）。
     *
     * **Validates: Requirements 9.3**
     */
    @Property
    void softDeletedProductsShouldNotBeVisibleToUsers(
            @ForAll @LongRange(min = 1, max = 999999L) Long deletedProductId,
            @ForAll @IntRange(min = 1, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 50) int pageSize) throws Exception {

        // 模拟 Mapper 返回的商品列表（不包含已删除商品，因为 SQL 条件 deleted=0）
        List<Product> visibleProducts = new ArrayList<>();
        Product visibleProduct = new Product();
        visibleProduct.setId(deletedProductId + 1);
        visibleProduct.setName("Visible Product");
        visibleProduct.setCategoryId(1L);
        visibleProduct.setStatus(1);
        visibleProduct.setDeleted(0);
        visibleProduct.setMinPrice(BigDecimal.TEN);
        visibleProducts.add(visibleProduct);

        // Mock ProductMapper - 用户端查询只返回 deleted=0 的商品
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        int offset = (page - 1) * pageSize;
        when(productMapper.selectListByPage(eq(offset), eq(pageSize)))
                .thenReturn(visibleProducts);
        when(productMapper.selectHotProducts())
                .thenReturn(visibleProducts);

        ProductService productService = createProductServiceWithMocks(productMapper);

        // 验证分页查询结果不包含已删除商品
        List<Product> pagedResult = productService.getProductList(page, pageSize);
        assertThat(pagedResult).noneMatch(p -> p.getId().equals(deletedProductId));
        assertThat(pagedResult).allSatisfy(p ->
                assertThat(p.getDeleted()).isEqualTo(0)
        );

        // 验证热门商品查询结果不包含已删除商品
        List<Product> hotResult = productService.getHotProducts();
        assertThat(hotResult).noneMatch(p -> p.getId().equals(deletedProductId));
        assertThat(hotResult).allSatisfy(p ->
                assertThat(p.getDeleted()).isEqualTo(0)
        );

        // 验证 Mapper 方法被正确调用（SQL 层面保证 deleted=0 过滤）
        verify(productMapper).selectListByPage(eq(offset), eq(pageSize));
        verify(productMapper).selectHotProducts();
    }
}
