package com.minishop.service;

import com.minishop.dto.ProductDTO;
import com.minishop.entity.*;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.*;
import com.minishop.vo.PageVO;
import com.minishop.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务逻辑层
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSpecMapper productSpecMapper;

    @Autowired
    private SpecValueMapper specValueMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuSpecValueMapper skuSpecValueMapper;

    // ==================== 用户端接口 ====================

    /**
     * 获取热门商品列表
     */
    public List<Product> getHotProducts() {
        return productMapper.selectHotProducts();
    }

    /**
     * 分页获取商品列表
     */
    public List<Product> getProductList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productMapper.selectListByPage(offset, pageSize);
    }

    /**
     * 获取商品详情（含规格和SKU信息）
     */
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 用户端只能查看上架商品
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF);
        }

        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);

        // 查询规格列表（含规格值）
        List<ProductSpec> specs = productSpecMapper.selectByProductId(id);
        vo.setSpecs(specs);

        // 查询SKU列表（含规格值关联）
        List<Sku> skus = skuMapper.selectByProductId(id);
        for (Sku sku : skus) {
            List<SkuSpecValue> specValues = skuSpecValueMapper.selectBySkuId(sku.getId());
            sku.setSpecValues(specValues);
        }
        vo.setSkus(skus);

        return vo;
    }

    /**
     * 按分类分页查询商品
     */
    public List<Product> getProductsByCategory(Long categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productMapper.selectByCategoryId(categoryId, offset, pageSize);
    }

    // ==================== 管理端接口 ====================

    /**
     * 管理端获取商品列表（分页）
     */
    public PageVO<Product> getAdminProductList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Product> list = productMapper.selectAdminList(offset, pageSize);
        int total = productMapper.countAll();
        return PageVO.of(list, total, page, pageSize);
    }

    /**
     * 添加商品（含规格和SKU，使用事务）
     */
    @Transactional
    public void addProduct(ProductDTO dto) {
        // 1. 创建商品
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setMainImage(dto.getMainImage());
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        product.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
        productMapper.insert(product);

        Long productId = product.getId();

        // 2. 创建规格和规格值
        if (dto.getSpecs() != null) {
            for (int i = 0; i < dto.getSpecs().size(); i++) {
                ProductDTO.SpecDTO specDTO = dto.getSpecs().get(i);
                ProductSpec spec = new ProductSpec();
                spec.setProductId(productId);
                spec.setName(specDTO.getName());
                spec.setSortOrder(specDTO.getSortOrder() != null ? specDTO.getSortOrder() : i);
                productSpecMapper.insert(spec);

                if (specDTO.getValues() != null) {
                    for (int j = 0; j < specDTO.getValues().size(); j++) {
                        SpecValue sv = new SpecValue();
                        sv.setSpecId(spec.getId());
                        sv.setValue(specDTO.getValues().get(j));
                        sv.setSortOrder(j);
                        specValueMapper.insert(sv);
                    }
                }
            }
        }

        // 3. 创建SKU和SKU规格值关联
        BigDecimal minPrice = null;
        if (dto.getSkus() != null) {
            for (ProductDTO.SkuDTO skuDTO : dto.getSkus()) {
                Sku sku = new Sku();
                sku.setProductId(productId);
                sku.setPrice(skuDTO.getPrice());
                sku.setStock(skuDTO.getStock() != null ? skuDTO.getStock() : 0);
                sku.setSpecDesc(skuDTO.getSpecDesc());
                sku.setImage(skuDTO.getImage());
                skuMapper.insert(sku);

                // 创建SKU与规格值关联
                if (skuDTO.getSpecValueIds() != null) {
                    for (Long specValueId : skuDTO.getSpecValueIds()) {
                        SkuSpecValue ssv = new SkuSpecValue();
                        ssv.setSkuId(sku.getId());
                        ssv.setSpecValueId(specValueId);
                        skuSpecValueMapper.insert(ssv);
                    }
                }

                // 计算最低价格
                if (minPrice == null || skuDTO.getPrice().compareTo(minPrice) < 0) {
                    minPrice = skuDTO.getPrice();
                }
            }
        }

        // 4. 更新商品最低价格
        if (minPrice != null) {
            productMapper.updateMinPrice(productId, minPrice);
        }
    }

    /**
     * 更新商品（含规格和SKU，使用事务）
     */
    @Transactional
    public void updateProduct(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }

        Product existing = productMapper.findById(dto.getId());
        if (existing == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 1. 更新商品基本信息
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setMainImage(dto.getMainImage());
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus());
        product.setIsHot(dto.getIsHot());
        productMapper.update(product);

        // 2. 如果传了规格信息，则重建规格
        if (dto.getSpecs() != null) {
            // 先删除旧的规格值关联、规格值、规格维度
            skuSpecValueMapper.deleteByProductId(dto.getId());
            skuMapper.deleteByProductId(dto.getId());
            specValueMapper.deleteByProductId(dto.getId());
            productSpecMapper.deleteByProductId(dto.getId());

            // 重新创建规格
            for (int i = 0; i < dto.getSpecs().size(); i++) {
                ProductDTO.SpecDTO specDTO = dto.getSpecs().get(i);
                ProductSpec spec = new ProductSpec();
                spec.setProductId(dto.getId());
                spec.setName(specDTO.getName());
                spec.setSortOrder(specDTO.getSortOrder() != null ? specDTO.getSortOrder() : i);
                productSpecMapper.insert(spec);

                if (specDTO.getValues() != null) {
                    for (int j = 0; j < specDTO.getValues().size(); j++) {
                        SpecValue sv = new SpecValue();
                        sv.setSpecId(spec.getId());
                        sv.setValue(specDTO.getValues().get(j));
                        sv.setSortOrder(j);
                        specValueMapper.insert(sv);
                    }
                }
            }
        }

        // 3. 如果传了SKU信息，则重建SKU
        if (dto.getSkus() != null) {
            // 如果规格没有重建，需要单独删除SKU相关数据
            if (dto.getSpecs() == null) {
                skuSpecValueMapper.deleteByProductId(dto.getId());
                skuMapper.deleteByProductId(dto.getId());
            }

            BigDecimal minPrice = null;
            for (ProductDTO.SkuDTO skuDTO : dto.getSkus()) {
                Sku sku = new Sku();
                sku.setProductId(dto.getId());
                sku.setPrice(skuDTO.getPrice());
                sku.setStock(skuDTO.getStock() != null ? skuDTO.getStock() : 0);
                sku.setSpecDesc(skuDTO.getSpecDesc());
                sku.setImage(skuDTO.getImage());
                skuMapper.insert(sku);

                if (skuDTO.getSpecValueIds() != null) {
                    for (Long specValueId : skuDTO.getSpecValueIds()) {
                        SkuSpecValue ssv = new SkuSpecValue();
                        ssv.setSkuId(sku.getId());
                        ssv.setSpecValueId(specValueId);
                        skuSpecValueMapper.insert(ssv);
                    }
                }

                if (minPrice == null || skuDTO.getPrice().compareTo(minPrice) < 0) {
                    minPrice = skuDTO.getPrice();
                }
            }

            if (minPrice != null) {
                productMapper.updateMinPrice(dto.getId(), minPrice);
            }
        }
    }

    /**
     * 删除商品（软删除）
     */
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 更新商品状态（上下架）
     */
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
    }
}
