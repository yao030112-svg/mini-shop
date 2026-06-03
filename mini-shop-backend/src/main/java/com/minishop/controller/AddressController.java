package com.minishop.controller;

import com.minishop.dto.AddressDTO;
import com.minishop.entity.Address;
import com.minishop.security.UserContext;
import com.minishop.service.AddressService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

/**
 * 收货地址控制器（需认证）
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<Address>> list() {
        Long userId = UserContext.getCurrentUserId();
        List<Address> addressList = addressService.getAddressList(userId);
        return Result.success(addressList);
    }

    /**
     * 新增地址
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddressDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        addressService.addAddress(address);
        return Result.success();
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody AddressDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        Address address = new Address();
        address.setId(dto.getId());
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        addressService.updateAddress(address, userId);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        addressService.deleteAddress(id, userId);
        return Result.success();
    }

    /**
     * 设为默认地址
     */
    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        addressService.setDefault(id, userId);
        return Result.success();
    }
}
