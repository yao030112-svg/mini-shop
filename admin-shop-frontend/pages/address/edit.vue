<template>
  <view class="address-edit">
    <!-- 表单 -->
    <view class="form">
      <!-- 收货人 -->
      <view class="form-item">
        <text class="form-item__label">收货人</text>
        <input
          class="form-item__input"
          v-model="form.receiverName"
          placeholder="请输入收货人姓名"
          maxlength="20"
        />
      </view>

      <!-- 联系电话 -->
      <view class="form-item">
        <text class="form-item__label">联系电话</text>
        <input
          class="form-item__input"
          v-model="form.phone"
          type="number"
          placeholder="请输入联系电话"
          maxlength="11"
        />
      </view>

      <!-- 省份选择 -->
      <picker mode="selector" :range="provinceList" range-key="name" :value="provinceIndex" @change="onProvinceChange">
        <view class="form-item">
          <text class="form-item__label">省份</text>
          <view class="form-item__picker">
            <text :class="['form-item__picker-text', { 'form-item__picker-text--placeholder': !form.province }]">
              {{ form.province || '请选择省份' }}
            </text>
            <text class="form-item__arrow">></text>
          </view>
        </view>
      </picker>
      
      <!-- 城市选择 -->
      <picker mode="selector" :range="cityList" range-key="name" :value="cityIndex" @change="onCityChange" :disabled="!form.province">
        <view class="form-item">
          <text class="form-item__label">城市</text>
          <view class="form-item__picker">
            <text :class="['form-item__picker-text', { 'form-item__picker-text--placeholder': !form.city }]">
              {{ form.city || '请选择城市' }}
            </text>
            <text class="form-item__arrow">></text>
          </view>
        </view>
      </picker>
      
      <!-- 区县选择 -->
      <picker mode="selector" :range="districtList" range-key="name" :value="districtIndex" @change="onDistrictChange" :disabled="!form.city">
        <view class="form-item">
          <text class="form-item__label">区县</text>
          <view class="form-item__picker">
            <text :class="['form-item__picker-text', { 'form-item__picker-text--placeholder': !form.district }]">
              {{ form.district || '请选择区县' }}
            </text>
            <text class="form-item__arrow">></text>
          </view>
        </view>
      </picker>

      <!-- 详细地址 -->
      <view class="form-item">
        <text class="form-item__label">详细地址</text>
        <input
          class="form-item__input"
          v-model="form.detail"
          placeholder="请输入详细地址（街道、门牌号等）"
          maxlength="100"
        />
      </view>

      <!-- 设为默认 -->
      <view class="form-item form-item--switch">
        <text class="form-item__label">设为默认地址</text>
        <switch
          :checked="form.isDefault"
          color="#ff6600"
          @change="onSwitchChange"
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-section">
      <view class="save-btn" @click="handleSave">
        <text class="save-btn__text">保存</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getAddressList, addAddress, updateAddress } from '@/api/address'

export default {
  data() {
    return {
      isEdit: false,
      addressId: null,
      form: {
        receiverName: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      },
      // 省市区数据
      provinceList: [
        { name: '广东省' },
        { name: '北京市' },
        { name: '上海市' },
        { name: '浙江省' },
        { name: '江苏省' }
      ],
      cityList: [],
      districtList: [],
      // 当前选择的索引
      provinceIndex: -1,
      cityIndex: -1,
      districtIndex: -1
    }
  },

  computed: {
    // 不需要额外的 computed
  },

  onLoad(options) {
    if (options.id) {
      this.isEdit = true
      this.addressId = options.id
      uni.setNavigationBarTitle({ title: '编辑地址' })
      this.loadAddressDetail()
    } else {
      uni.setNavigationBarTitle({ title: '新增地址' })
    }
  },

  methods: {
    // 省份选择变化
    onProvinceChange(e) {
      const index = e.detail.value
      this.provinceIndex = index
      this.cityIndex = -1
      this.districtIndex = -1
      
      const province = this.provinceList[index]
      if (province) {
        this.form.province = province.name
        // 根据省份加载城市数据
        this.cityList = this.getCityList(province.name)
        this.form.city = ''
        this.form.district = ''
        this.districtList = []
      }
    },
    
    // 城市选择变化
    onCityChange(e) {
      const index = e.detail.value
      this.cityIndex = index
      this.districtIndex = -1
      
      const city = this.cityList[index]
      if (city) {
        this.form.city = city.name
        // 根据城市加载区县数据
        this.districtList = this.getDistrictList(this.form.province, city.name)
        this.form.district = ''
      }
    },
    
    // 区县选择变化
    onDistrictChange(e) {
      const index = e.detail.value
      this.districtIndex = index
      
      const district = this.districtList[index]
      if (district) {
        this.form.district = district.name
      }
    },
    
    // 获取城市列表
    getCityList(province) {
      const cityMap = {
        '广东省': [{ name: '深圳市' }, { name: '广州市' }, { name: '东莞市' }],
        '北京市': [{ name: '北京市' }],
        '上海市': [{ name: '上海市' }],
        '浙江省': [{ name: '杭州市' }, { name: '宁波市' }],
        '江苏省': [{ name: '南京市' }, { name: '苏州市' }]
      }
      return cityMap[province] || []
    },
    
    // 获取区县列表
    getDistrictList(province, city) {
      const districtMap = {
        '广东省': {
          '深圳市': [{ name: '南山区' }, { name: '福田区' }, { name: '罗湖区' }],
          '广州市': [{ name: '天河区' }, { name: '越秀区' }, { name: '海珠区' }]
        },
        '北京市': {
          '北京市': [{ name: '东城区' }, { name: '西城区' }, { name: '朝阳区' }]
        },
        '上海市': {
          '上海市': [{ name: '黄浦区' }, { name: '徐汇区' }, { name: '浦东新区' }]
        },
        '浙江省': {
          '杭州市': [{ name: '西湖区' }, { name: '拱墅区' }],
          '宁波市': [{ name: '海曙区' }, { name: '江北区' }]
        },
        '江苏省': {
          '南京市': [{ name: '玄武区' }, { name: '秦淮区' }],
          '苏州市': [{ name: '姑苏区' }, { name: '虎丘区' }]
        }
      }
      return (districtMap[province] && districtMap[province][city]) || []
    },
    
    // 加载地址详情（编辑模式）
    async loadAddressDetail() {
      try {
        uni.showLoading({ title: '加载中' })
        const res = await getAddressList()
        if (res.code === 200) {
          const address = (res.data || []).find(item => item.id == this.addressId)
          if (address) {
            // 使用 $set 确保响应式更新
            this.$set(this.form, 'receiverName', address.receiverName || '')
            this.$set(this.form, 'phone', address.phone || '')
            this.$set(this.form, 'province', address.province || '')
            this.$set(this.form, 'city', address.city || '')
            this.$set(this.form, 'district', address.district || '')
            this.$set(this.form, 'detail', address.detail || '')
            this.$set(this.form, 'isDefault', !!address.isDefault)
            
            // 设置省市区选择器的索引
            if (address.province) {
              const provinceIdx = this.provinceList.findIndex(p => p.name === address.province)
              if (provinceIdx !== -1) {
                this.provinceIndex = provinceIdx
                this.cityList = this.getCityList(address.province)
                
                if (address.city) {
                  const cityIdx = this.cityList.findIndex(c => c.name === address.city)
                  if (cityIdx !== -1) {
                    this.cityIndex = cityIdx
                    this.districtList = this.getDistrictList(address.province, address.city)
                    
                    if (address.district) {
                      const districtIdx = this.districtList.findIndex(d => d.name === address.district)
                      if (districtIdx !== -1) {
                        this.districtIndex = districtIdx
                      }
                    }
                  }
                }
              }
            }
          } else {
            uni.showToast({ title: '地址不存在', icon: 'none' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          }
        }
      } catch (e) {
        console.error('加载地址详情失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },

    // 省市区选择回调（小程序环境）
    onRegionChange(e) {
      const value = e.detail.value
      if (!value || value.length < 3) return
      
      this.form.province = value[0]
      this.form.city = value[1]
      this.form.district = value[2]
      this.regionValue = value
      // #ifdef H5
      this.regionInput = `${value[0]} ${value[1]} ${value[2]}`
      // #endif
    },

    // 解析 H5 环境下的省市区输入
    parseRegionInput() {
      if (!this.regionInput.trim()) return
      
      // 尝试用空格或中文分隔符分割
      const parts = this.regionInput.trim().split(/[\s,，]+/)
      if (parts.length >= 3) {
        this.form.province = parts[0]
        this.form.city = parts[1]
        this.form.district = parts[2]
        this.regionValue = parts.slice(0, 3)
      } else if (parts.length === 2) {
        this.form.province = parts[0]
        this.form.city = parts[1]
        this.form.district = ''
        this.regionValue = parts
      } else if (parts.length === 1) {
        this.form.province = parts[0]
        this.form.city = ''
        this.form.district = ''
        this.regionValue = parts
      }
    },

    // 默认地址开关
    onSwitchChange(e) {
      this.form.isDefault = e.detail.value
    },

    // 表单验证
    validate() {
      if (!this.form.receiverName.trim()) {
        uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
        return false
      }
      if (!this.form.phone.trim()) {
        uni.showToast({ title: '请输入联系电话', icon: 'none' })
        return false
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return false
      }
      if (!this.form.province || !this.form.city || !this.form.district) {
        uni.showToast({ title: '请选择所在地区', icon: 'none' })
        return false
      }
      if (!this.form.detail.trim()) {
        uni.showToast({ title: '请输入详细地址', icon: 'none' })
        return false
      }
      return true
    },

    // 保存地址
    async handleSave() {
      if (!this.validate()) return

      try {
        uni.showLoading({ title: '保存中' })
        const data = {
          receiverName: this.form.receiverName.trim(),
          phone: this.form.phone.trim(),
          province: this.form.province,
          city: this.form.city,
          district: this.form.district,
          detail: this.form.detail.trim(),
          isDefault: this.form.isDefault ? 1 : 0
        }

        let res
        if (this.isEdit) {
          data.id = this.addressId
          res = await updateAddress(data)
        } else {
          res = await addAddress(data)
        }

        if (res.code === 200) {
          uni.showToast({ title: '保存成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          uni.showToast({ title: res.message || '保存失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '保存失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.address-edit {
  min-height: 100vh;
  background-color: $bg-color;
  padding: $spacing-sm;
}

/* 表单 */
.form {
  background-color: $bg-color-white;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-light;

  &:last-child {
    border-bottom: none;
  }

  &--switch {
    justify-content: space-between;
  }

  &__label {
    font-size: $font-size-md;
    color: $text-color;
    width: 160rpx;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    font-size: $font-size-md;
    color: $text-color;
  }

  &__picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__picker-text {
    font-size: $font-size-md;
    color: $text-color;

    &--placeholder {
      color: $text-color-placeholder;
    }
  }

  &__arrow {
    font-size: $font-size-md;
    color: $text-color-lighter;
  }
}

/* 保存按钮 */
.save-section {
  margin-top: $spacing-xl;
}

.save-btn {
  background-color: $primary-color;
  border-radius: $border-radius-round;
  padding: $spacing-md;
  text-align: center;

  &__text {
    font-size: $font-size-md;
    color: #ffffff;
    font-weight: bold;
  }
}
</style>
