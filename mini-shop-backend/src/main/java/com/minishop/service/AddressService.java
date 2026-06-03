package com.minishop.service;

import com.minishop.entity.Address;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址业务逻辑层
 */
@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 获取用户地址列表
     */
    public List<Address> getAddressList(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    /**
     * 新增地址
     * 每用户最多10个地址
     */
    public void addAddress(Address address) {
        // 检查地址数量是否已达上限
        int count = addressMapper.countByUserId(address.getUserId());
        if (count >= 10) {
            throw new BusinessException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        // 如果设为默认地址，先清除其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(address.getUserId());
        }

        addressMapper.insert(address);
    }

    /**
     * 更新地址
     */
    public void updateAddress(Address address, Long userId) {
        // 校验地址是否存在且属于当前用户
        Address existing = addressMapper.findById(address.getId());
        if (existing == null) {
            throw new BusinessException(400, "地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        // 如果设为默认地址，先清除其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }

        address.setUserId(userId);
        addressMapper.update(address);
    }

    /**
     * 删除地址
     */
    public void deleteAddress(Long id, Long userId) {
        // 校验地址是否存在且属于当前用户
        Address existing = addressMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(400, "地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        addressMapper.deleteById(id);
    }

    /**
     * 设为默认地址
     * 先清除该用户所有默认地址，再设置目标地址为默认
     */
    @Transactional
    public void setDefault(Long id, Long userId) {
        // 校验地址是否存在且属于当前用户
        Address existing = addressMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(400, "地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        // 先清除所有默认，再设置当前为默认
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id);
    }

    /**
     * 根据ID查找地址
     */
    public Address getById(Long id) {
        return addressMapper.findById(id);
    }
}
