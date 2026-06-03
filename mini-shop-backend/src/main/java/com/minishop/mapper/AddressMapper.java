package com.minishop.mapper;

import com.minishop.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址 Mapper 接口
 */
@Mapper
public interface AddressMapper {

    /**
     * 获取用户地址列表
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查找地址
     */
    Address findById(@Param("id") Long id);

    /**
     * 新增地址
     */
    int insert(Address address);

    /**
     * 更新地址
     */
    int update(Address address);

    /**
     * 删除地址
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户地址数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 清除用户所有默认地址
     */
    int clearDefault(@Param("userId") Long userId);

    /**
     * 设置某地址为默认
     */
    int setDefault(@Param("id") Long id);
}
