package com.minishop.service;

import com.minishop.entity.Address;
import com.minishop.exception.BusinessException;
import com.minishop.mapper.AddressMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 地址数量上限属性测试
 *
 * **Validates: Requirements 8.4**
 *
 * 属性 13：地址数量上限约束 - 每用户最多10个地址，已有10个时新增应失败
 */
class AddressLimitPropertyTest {

    private AddressService createServiceWithMock(AddressMapper addressMapper) throws Exception {
        AddressService service = new AddressService();
        Field field = AddressService.class.getDeclaredField("addressMapper");
        field.setAccessible(true);
        field.set(service, addressMapper);
        return service;
    }

    /**
     * 属性 13：地址数量上限约束
     * 当用户已有10个地址时，新增地址操作应失败并抛出 BusinessException(1003)
     *
     * **Validates: Requirements 8.4**
     */
    @Property
    void shouldRejectAddressWhenLimitReached(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId) throws Exception {

        // Mock addressMapper.countByUserId 返回 10（已达上限）
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);
        when(addressMapper.countByUserId(userId)).thenReturn(10);

        AddressService service = createServiceWithMock(addressMapper);

        // 构造新地址
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName("测试用户");
        address.setPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("科技园路1号");
        address.setIsDefault(0);

        // 验证抛出 BusinessException 且错误码为 1003
        assertThatThrownBy(() -> service.addAddress(address))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(1003);
                });

        // 验证 insert 未被调用（地址未被添加）
        verify(addressMapper, never()).insert(any(Address.class));
    }

    /**
     * 属性 13（补充）：地址数量未达上限时应允许新增
     *
     * **Validates: Requirements 8.4**
     */
    @Property
    void shouldAllowAddressWhenBelowLimit(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 9) int currentCount) throws Exception {

        // Mock addressMapper.countByUserId 返回小于10的值
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);
        when(addressMapper.countByUserId(userId)).thenReturn(currentCount);

        AddressService service = createServiceWithMock(addressMapper);

        // 构造新地址
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName("测试用户");
        address.setPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("科技园路1号");
        address.setIsDefault(0);

        // 不应抛出异常
        assertThatCode(() -> service.addAddress(address)).doesNotThrowAnyException();

        // 验证 insert 被调用
        verify(addressMapper).insert(address);
    }
}
