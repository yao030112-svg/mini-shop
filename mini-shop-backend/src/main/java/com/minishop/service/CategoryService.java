package com.minishop.service;

import com.minishop.dto.CategoryDTO;
import com.minishop.entity.Category;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务逻辑层
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 获取分类列表（按 sort_order 降序）
     */
    public List<Category> getList() {
        return categoryMapper.selectList();
    }

    /**
     * 根据ID获取分类
     */
    public Category getById(Long id) {
        return categoryMapper.findById(id);
    }

    /**
     * 添加分类
     */
    public void add(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.insert(category);
    }

    /**
     * 更新分类
     */
    public void update(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSortOrder(dto.getSortOrder());
        categoryMapper.update(category);
    }

    /**
     * 删除分类（检查分类下是否有商品）
     */
    public void delete(Long id) {
        int productCount = categoryMapper.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }
        categoryMapper.deleteById(id);
    }
}
