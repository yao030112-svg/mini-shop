package com.minishop.service;

import com.minishop.entity.Banner;
import com.minishop.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图业务逻辑层
 */
@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    /**
     * 获取启用的轮播图列表（按 sort_order 降序）
     */
    public List<Banner> getActiveList() {
        return bannerMapper.selectActiveList();
    }

    /**
     * 获取所有轮播图列表（管理端使用）
     */
    public List<Banner> getList() {
        return bannerMapper.selectList();
    }

    /**
     * 根据ID查询轮播图
     */
    public Banner getById(Long id) {
        return bannerMapper.findById(id);
    }

    /**
     * 新增轮播图
     */
    public void add(Banner banner) {
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        bannerMapper.insert(banner);
    }

    /**
     * 更新轮播图
     */
    public void update(Banner banner) {
        bannerMapper.update(banner);
    }

    /**
     * 删除轮播图
     */
    public void delete(Long id) {
        bannerMapper.deleteById(id);
    }

    /**
     * 更新轮播图状态
     */
    public void updateStatus(Long id, Integer status) {
        bannerMapper.updateStatus(id, status);
    }
}
