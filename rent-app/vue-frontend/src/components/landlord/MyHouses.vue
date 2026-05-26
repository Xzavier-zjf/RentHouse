<template>
  <div class="my-houses">
    <el-card>
      <template #header>
        <div class="header">
          <span>我的房源</span>
          <el-button type="primary" @click="getMyHouses" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="houses" stripe style="width: 100%" v-loading="loading">
        <el-table-column label="封面" width="110">
          <template #default="scope">
            <el-image v-if="coverUrl(scope.row)" :src="coverUrl(scope.row)" fit="cover" class="cover-image" />
            <span v-else class="no-image">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140"></el-table-column>
        <el-table-column prop="price" label="月租金" width="100" :formatter="priceFormatter"></el-table-column>
        <el-table-column prop="location" label="位置" min-width="130"></el-table-column>
        <el-table-column prop="layout" label="户型" width="110"></el-table-column>
        <el-table-column prop="status" label="审核" width="110">
          <template #default="scope"><el-tag>{{ scope.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="rentStatus" label="出租" width="110">
          <template #default="scope"><el-tag>{{ scope.row.rentStatus || 'AVAILABLE' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="reason" label="审核原因" min-width="130"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editHouse(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="offlineHouse(scope.row.id)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑房源" width="680px">
      <el-form :model="editForm" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="标题"><el-input v-model="editForm.title"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="月租金"><el-input-number v-model="editForm.price" :min="1"></el-input-number></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="位置">
            <el-select v-model="editForm.location" filterable allow-create default-first-option placeholder="请选择或输入城市/区域/小区" class="full-width">
              <el-option v-for="item in locationOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
            <el-button class="location-button" @click="chooseEditLocation" :loading="locating">定位</el-button>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="户型">
            <el-select v-model="editForm.layout" filterable allow-create default-first-option placeholder="请选择或输入户型" class="full-width">
              <el-option v-for="item in layoutOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item v-if="editForm.latitude && editForm.longitude" label="定位坐标">
          <el-tag type="success">{{ editForm.latitude }}, {{ editForm.longitude }}</el-tag>
          <el-button type="primary" link @click="openMap(editForm)">查看地图</el-button>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="面积"><el-input-number v-model="editForm.area" :min="0"></el-input-number></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼层">
            <el-select v-model="editForm.floor" filterable allow-create default-first-option placeholder="请选择或输入楼层" class="full-width">
              <el-option v-for="item in floorOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="朝向">
            <el-select v-model="editForm.orientation" filterable allow-create default-first-option placeholder="请选择或输入朝向" class="full-width">
              <el-option v-for="item in orientationOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="editForm.contactName"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.contactPhone"></el-input></el-form-item></el-col>
        </el-row>
        <el-form-item label="旧图片地址"><el-input v-model="editForm.imageUrl" placeholder="兼容旧网络图片地址"></el-input></el-form-item>
        <el-form-item label="房源图片">
          <div class="image-manager">
            <el-upload
              :show-file-list="false"
              :before-upload="uploadEditImage"
              accept="image/jpeg,image/png,image/webp"
            >
              <el-button type="primary" :loading="imageUploading">上传图片</el-button>
            </el-upload>
            <div v-if="editImages.length" class="image-list">
              <div v-for="(image, index) in editImages" :key="image.fileId" class="image-item">
                <el-image :src="imageUrl(image.url)" fit="cover" class="house-image" />
                <div class="image-actions">
                  <el-tag v-if="image.cover" type="success" size="small">封面</el-tag>
                  <el-button v-else size="small" @click="setCover(image)">设封面</el-button>
                  <el-button size="small" :disabled="index === 0" @click="moveImage(image, -1)">上移</el-button>
                  <el-button size="small" :disabled="index === editImages.length - 1" @click="moveImage(image, 1)">下移</el-button>
                  <el-button size="small" type="danger" @click="deleteImage(image)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无图片" :image-size="72" />
          </div>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3"></el-input></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveHouse">保存并重新审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assetUrl, houseAPI } from '../../utils/api'

export default {
  name: 'MyHouses',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    const dialogVisible = ref(false)
    const editForm = reactive({})
    const editImages = ref([])
    const imageUploading = ref(false)
    const locating = ref(false)
    const locationOptions = ['北京市 朝阳区', '上海市 浦东新区', '广州市 天河区', '深圳市 南山区', '杭州市 西湖区', '成都市 高新区', '武汉市 洪山区', '南京市 鼓楼区', '重庆市 渝北区', '西安市 雁塔区']
    const layoutOptions = ['一室一厅', '两室一厅', '两室两厅', '三室一厅', '三室两厅', '四室两厅', '单间', '复式', '公寓']
    const floorOptions = ['低楼层', '中楼层', '高楼层', '1/6层', '3/6层', '6/18层', '10/30层', '顶层']
    const orientationOptions = ['南向', '北向', '东向', '西向', '东南向', '西南向', '东北向', '西北向', '南北通透']
    const getMyHouses = async () => {
      loading.value = true
      try {
        houses.value = (await houseAPI.getMyHouses()).data
      } catch (error) {
        ElMessage.error('获取房源失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    const loadHouseImages = async (houseId) => {
      editImages.value = (await houseAPI.getHouseImages(houseId)).data
    }
    const editHouse = async (house) => {
      Object.assign(editForm, house)
      editImages.value = house.images || []
      dialogVisible.value = true
      await loadHouseImages(house.id)
    }
    const saveHouse = async () => {
      try {
        await houseAPI.updateHouse(editForm.id, editForm)
        ElMessage.success('房源已保存，等待重新审核')
        dialogVisible.value = false
        getMyHouses()
      } catch (error) {
        ElMessage.error('保存失败: ' + (error.response?.data || error.message))
      }
    }
    const uploadEditImage = async (file) => {
      imageUploading.value = true
      try {
        await houseAPI.uploadHouseImage(editForm.id, file)
        await loadHouseImages(editForm.id)
        await getMyHouses()
        ElMessage.success('图片已上传，房源将重新审核')
      } catch (error) {
        ElMessage.error('上传失败: ' + (error.response?.data || error.message))
      } finally {
        imageUploading.value = false
      }
      return false
    }
    const setCover = async (image) => {
      try {
        await houseAPI.updateHouseImage(editForm.id, image.fileId, { cover: true })
        await loadHouseImages(editForm.id)
        await getMyHouses()
        ElMessage.success('封面已更新，房源将重新审核')
      } catch (error) {
        ElMessage.error('设置封面失败: ' + (error.response?.data || error.message))
      }
    }
    const moveImage = async (image, offset) => {
      const currentIndex = editImages.value.findIndex(item => item.fileId === image.fileId)
      const target = editImages.value[currentIndex + offset]
      if (!target) return
      try {
        await houseAPI.updateHouseImage(editForm.id, image.fileId, { sortOrder: target.sortOrder })
        await houseAPI.updateHouseImage(editForm.id, target.fileId, { sortOrder: image.sortOrder })
        await loadHouseImages(editForm.id)
        await getMyHouses()
        ElMessage.success('排序已更新，房源将重新审核')
      } catch (error) {
        ElMessage.error('调整排序失败: ' + (error.response?.data || error.message))
      }
    }
    const deleteImage = async (image) => {
      try {
        await ElMessageBox.confirm('确定删除该图片吗？', '确认删除')
        await houseAPI.deleteHouseImage(editForm.id, image.fileId)
        await loadHouseImages(editForm.id)
        await getMyHouses()
        ElMessage.success('图片已删除，房源将重新审核')
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败: ' + (error.response?.data || error.message))
      }
    }
    const chooseEditLocation = () => {
      if (!navigator.geolocation) {
        ElMessage.warning('当前浏览器不支持定位')
        return
      }
      locating.value = true
      navigator.geolocation.getCurrentPosition(
        (position) => {
          editForm.latitude = Number(position.coords.latitude.toFixed(7))
          editForm.longitude = Number(position.coords.longitude.toFixed(7))
          if (!editForm.location) {
            editForm.location = `当前位置(${editForm.latitude}, ${editForm.longitude})`
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
    const offlineHouse = async (id) => {
      try {
        await ElMessageBox.confirm('确定下架该房源吗？', '确认下架')
        await houseAPI.offlineHouse(id)
        ElMessage.success('房源已下架')
        getMyHouses()
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('下架失败: ' + (error.response?.data || error.message))
      }
    }
    const priceFormatter = (row, column, cellValue) => cellValue ? `￥${cellValue}/月` : '-'
    const imageUrl = (url) => assetUrl(url)
    const coverUrl = (house) => assetUrl(house.coverImageUrl)
    onMounted(getMyHouses)
    return {
      loading, houses, dialogVisible, editForm, editImages, imageUploading, locating, locationOptions, layoutOptions,
      floorOptions, orientationOptions, getMyHouses, editHouse, saveHouse,
      uploadEditImage, setCover, moveImage, deleteImage, chooseEditLocation, openMap, offlineHouse, priceFormatter, imageUrl, coverUrl
    }
  }
}
</script>

<style scoped>
.my-houses { max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.image-manager { width: 100%; }
.image-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; margin-top: 12px; }
.image-item { border: 1px solid #ebeef5; border-radius: 6px; padding: 8px; }
.house-image { width: 100%; height: 100px; border-radius: 4px; background: #f5f7fa; }
.image-actions { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.cover-image { width: 76px; height: 56px; border-radius: 4px; background: #f5f7fa; }
.no-image { color: #909399; font-size: 12px; }
.full-width { width: 100%; }
.location-button { margin-top: 8px; }
</style>
