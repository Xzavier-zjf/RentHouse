<template>
  <div class="house-upload">
    <el-card>
      <h3>上传房源</h3>
      <el-form :model="houseForm" :rules="rules" ref="houseFormRef" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="houseForm.title" placeholder="请输入房源标题"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月租金" prop="price">
              <el-input-number v-model="houseForm.price" :min="1"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="位置" prop="location">
              <el-select v-model="houseForm.location" filterable allow-create default-first-option placeholder="请选择或输入城市/区域/小区" class="full-width">
                <el-option v-for="item in locationOptions" :key="item" :label="item" :value="item"></el-option>
                <template #append>
                  <el-button @click="chooseLocation" :loading="locating">定位</el-button>
                </template>
              </el-select>
              <el-button class="location-button" @click="chooseLocation" :loading="locating">定位</el-button>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户型">
              <el-select v-model="houseForm.layout" filterable allow-create default-first-option placeholder="请选择或输入户型" class="full-width">
                <el-option v-for="item in layoutOptions" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="面积">
              <el-input-number v-model="houseForm.area" :min="0"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="楼层">
              <el-select v-model="houseForm.floor" filterable allow-create default-first-option placeholder="请选择或输入楼层" class="full-width">
                <el-option v-for="item in floorOptions" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="朝向">
              <el-select v-model="houseForm.orientation" filterable allow-create default-first-option placeholder="请选择或输入朝向" class="full-width">
                <el-option v-for="item in orientationOptions" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="houseForm.contactName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="houseForm.contactPhone"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="houseForm.latitude && houseForm.longitude" label="定位坐标">
          <el-tag type="success">{{ houseForm.latitude }}, {{ houseForm.longitude }}</el-tag>
          <el-button type="primary" link @click="openMap(houseForm)">查看地图</el-button>
        </el-form-item>
        <el-form-item label="房源图片">
          <el-upload
            v-model:file-list="imageFiles"
            list-type="picture-card"
            :auto-upload="false"
            :limit="8"
            accept="image/jpeg,image/png,image/webp"
          >
            <span>添加图片</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="houseForm.description" type="textarea" :rows="4" placeholder="请输入房源描述"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="uploadHouse" :loading="loading">上传房源</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseUpload',
  setup() {
    const houseFormRef = ref(null)
    const loading = ref(false)
    const locating = ref(false)
    const imageFiles = ref([])
    const initialForm = () => ({
      title: '', price: 1, location: '', description: '', layout: '', area: null,
      floor: '', orientation: '', imageUrl: '', contactName: '', contactPhone: '', latitude: null, longitude: null
    })
    const houseForm = reactive(initialForm())
    const rules = {
      title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
      price: [{ required: true, message: '请输入月租金', trigger: 'blur' }],
      location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
    }
    const locationOptions = ['北京市 朝阳区', '上海市 浦东新区', '广州市 天河区', '深圳市 南山区', '杭州市 西湖区', '成都市 高新区', '武汉市 洪山区', '南京市 鼓楼区', '重庆市 渝北区', '西安市 雁塔区']
    const layoutOptions = ['一室一厅', '两室一厅', '两室两厅', '三室一厅', '三室两厅', '四室两厅', '单间', '复式', '公寓']
    const floorOptions = ['低楼层', '中楼层', '高楼层', '1/6层', '3/6层', '6/18层', '10/30层', '顶层']
    const orientationOptions = ['南向', '北向', '东向', '西向', '东南向', '西南向', '东北向', '西北向', '南北通透']
    const uploadHouse = async () => {
      houseFormRef.value.validate(async (valid) => {
        if (!valid) return
        loading.value = true
        try {
          const response = await houseAPI.uploadHouse(houseForm)
          const houseId = response.data?.houseId
          if (houseId && imageFiles.value.length > 0) {
            for (const item of imageFiles.value) {
              await houseAPI.uploadHouseImage(houseId, item.raw)
            }
          }
          ElMessage.success('房源上传成功，等待管理员审核')
          resetForm()
        } catch (error) {
          ElMessage.error('上传失败: ' + (error.response?.data || error.message))
        } finally {
          loading.value = false
        }
      })
    }
    const resetForm = () => {
      Object.assign(houseForm, initialForm())
      imageFiles.value = []
      houseFormRef.value?.clearValidate()
    }
    const chooseLocation = () => {
      if (!navigator.geolocation) {
        ElMessage.warning('当前浏览器不支持定位')
        return
      }
      locating.value = true
      navigator.geolocation.getCurrentPosition(
        (position) => {
          houseForm.latitude = Number(position.coords.latitude.toFixed(7))
          houseForm.longitude = Number(position.coords.longitude.toFixed(7))
          if (!houseForm.location) {
            houseForm.location = `当前位置(${houseForm.latitude}, ${houseForm.longitude})`
          }
          locating.value = false
          ElMessage.success('定位已获取')
        },
        (error) => {
          locating.value = false
          ElMessage.error('定位失败: ' + error.message)
        },
        { enableHighAccuracy: true, timeout: 10000 }
      )
    }
    const openMap = (house) => {
      window.open(`https://www.google.com/maps?q=${house.latitude},${house.longitude}`, '_blank')
    }
    return {
      houseForm, rules, houseFormRef, loading, locating, imageFiles, locationOptions, layoutOptions, floorOptions,
      orientationOptions, uploadHouse, resetForm, chooseLocation, openMap
    }
  }
}
</script>

<style scoped>
.house-upload { max-width: 980px; margin: 0 auto; }
.full-width { width: 100%; }
.location-button { margin-top: 8px; }
</style>
