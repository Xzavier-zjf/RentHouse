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
        <el-table-column prop="title" label="标题" min-width="140"></el-table-column>
        <el-table-column prop="price" label="租金" width="100" :formatter="priceFormatter"></el-table-column>
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
          <el-col :span="12"><el-form-item label="租金"><el-input-number v-model="editForm.price" :min="1"></el-input-number></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="位置"><el-input v-model="editForm.location"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="户型"><el-input v-model="editForm.layout"></el-input></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="面积"><el-input-number v-model="editForm.area" :min="0"></el-input-number></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼层"><el-input v-model="editForm.floor"></el-input></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="朝向"><el-input v-model="editForm.orientation"></el-input></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="editForm.contactName"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.contactPhone"></el-input></el-form-item></el-col>
        </el-row>
        <el-form-item label="图片地址"><el-input v-model="editForm.imageUrl"></el-input></el-form-item>
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
import { houseAPI } from '../../utils/api'

export default {
  name: 'MyHouses',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    const dialogVisible = ref(false)
    const editForm = reactive({})
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
    const editHouse = (house) => {
      Object.assign(editForm, house)
      dialogVisible.value = true
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
    const priceFormatter = (row, column, cellValue) => cellValue ? `￥${cellValue}` : '-'
    onMounted(getMyHouses)
    return { loading, houses, dialogVisible, editForm, getMyHouses, editHouse, saveHouse, offlineHouse, priceFormatter }
  }
}
</script>

<style scoped>
.my-houses { max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
