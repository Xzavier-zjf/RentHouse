<template>
  <div class="house-search">
    <el-card>
      <h3>房源搜索</h3>
      <el-form :model="searchForm" label-width="90px" class="search-form">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="位置">
              <el-input v-model="searchForm.location" placeholder="请输入位置"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="户型">
              <el-input v-model="searchForm.layout" placeholder="如 两室一厅"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="价格">
              <div class="range-inputs">
                <el-input-number v-model="searchForm.minPrice" :min="0" placeholder="最低"></el-input-number>
                <span>-</span>
                <el-input-number v-model="searchForm.maxPrice" :min="0" placeholder="最高"></el-input-number>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="面积">
              <div class="range-inputs">
                <el-input-number v-model="searchForm.minArea" :min="0" placeholder="最小"></el-input-number>
                <span>-</span>
                <el-input-number v-model="searchForm.maxArea" :min="0" placeholder="最大"></el-input-number>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item>
              <el-button type="primary" @click="searchHouses" :loading="loading">搜索</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-table :data="searchResults" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="140"></el-table-column>
        <el-table-column prop="price" label="租金" width="110" :formatter="priceFormatter"></el-table-column>
        <el-table-column prop="location" label="位置" min-width="140"></el-table-column>
        <el-table-column prop="layout" label="户型" width="110"></el-table-column>
        <el-table-column prop="area" label="面积" width="100" :formatter="areaFormatter"></el-table-column>
        <el-table-column prop="rentStatus" label="出租状态" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.rentStatus === 'RENTED' ? 'danger' : 'success'">{{ scope.row.rentStatus || 'AVAILABLE' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
            <el-button size="small" type="warning" @click="favorite(scope.row)">收藏</el-button>
            <el-button size="small" type="primary" @click="openAppointment(scope.row)">预约</el-button>
            <el-button size="small" type="success" @click="openApplication(scope.row)">申请</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-alert v-if="searchResults.length === 0 && searched" title="未找到符合条件的房源" type="info" show-icon class="empty-tip"></el-alert>
    </el-card>

    <el-dialog v-model="detailVisible" title="房源详情" width="620px">
      <el-descriptions v-if="selectedHouse" :column="2" border>
        <el-descriptions-item label="标题">{{ selectedHouse.title }}</el-descriptions-item>
        <el-descriptions-item label="租金">{{ priceFormatter(null, null, selectedHouse.price) }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ selectedHouse.location }}</el-descriptions-item>
        <el-descriptions-item label="户型">{{ selectedHouse.layout || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ areaFormatter(null, null, selectedHouse.area) }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ selectedHouse.floor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="朝向">{{ selectedHouse.orientation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ selectedHouse.contactName || '-' }} {{ selectedHouse.contactPhone || '' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ selectedHouse.description || '暂无描述' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="appointmentVisible" title="预约看房" width="520px">
      <el-form :model="appointmentForm" label-width="90px">
        <el-form-item label="看房时间">
          <el-date-picker v-model="appointmentForm.appointmentTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"></el-date-picker>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="appointmentForm.contactName"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="appointmentForm.contactPhone"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="appointmentForm.message" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointmentVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">提交预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applicationVisible" title="租赁申请" width="520px">
      <el-form :model="applicationForm" label-width="90px">
        <el-form-item label="姓名">
          <el-input v-model="applicationForm.applicantName"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="applicationForm.applicantPhone"></el-input>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="applicationForm.moveInDate" type="date" value-format="YYYY-MM-DD"></el-date-picker>
        </el-form-item>
        <el-form-item label="租期(月)">
          <el-input-number v-model="applicationForm.leaseMonths" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applicationForm.message" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applicationVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApplication">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseSearch',
  setup() {
    const searchForm = reactive({ location: '', layout: '', minPrice: null, maxPrice: null, minArea: null, maxArea: null })
    const loading = ref(false)
    const searchResults = ref([])
    const searched = ref(false)
    const selectedHouse = ref(null)
    const detailVisible = ref(false)
    const appointmentVisible = ref(false)
    const applicationVisible = ref(false)
    const appointmentForm = reactive({ houseId: null, appointmentTime: '', contactName: '', contactPhone: '', message: '' })
    const applicationForm = reactive({ houseId: null, applicantName: '', applicantPhone: '', moveInDate: '', leaseMonths: 12, message: '' })

    const cleanParams = () => Object.fromEntries(Object.entries(searchForm).filter(([, value]) => value !== '' && value !== null && value !== undefined))

    const searchHouses = async () => {
      loading.value = true
      try {
        const response = await houseAPI.searchHouses(cleanParams())
        searchResults.value = response.data
        searched.value = true
      } catch (error) {
        ElMessage.error('搜索失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }

    const resetForm = () => {
      Object.assign(searchForm, { location: '', layout: '', minPrice: null, maxPrice: null, minArea: null, maxArea: null })
      searchHouses()
    }

    const openDetail = (house) => {
      selectedHouse.value = house
      detailVisible.value = true
    }

    const favorite = async (house) => {
      try {
        await houseAPI.addFavorite(house.id)
        ElMessage.success('收藏成功')
      } catch (error) {
        ElMessage.error('收藏失败: ' + (error.response?.data || error.message))
      }
    }

    const openAppointment = (house) => {
      Object.assign(appointmentForm, { houseId: house.id, appointmentTime: '', contactName: '', contactPhone: '', message: '' })
      appointmentVisible.value = true
    }

    const submitAppointment = async () => {
      try {
        await houseAPI.createAppointment(appointmentForm)
        ElMessage.success('预约已提交')
        appointmentVisible.value = false
      } catch (error) {
        ElMessage.error('预约失败: ' + (error.response?.data || error.message))
      }
    }

    const openApplication = (house) => {
      Object.assign(applicationForm, { houseId: house.id, applicantName: '', applicantPhone: '', moveInDate: '', leaseMonths: 12, message: '' })
      applicationVisible.value = true
    }

    const submitApplication = async () => {
      try {
        await houseAPI.createRentalApplication(applicationForm)
        ElMessage.success('申请已提交')
        applicationVisible.value = false
      } catch (error) {
        ElMessage.error('申请失败: ' + (error.response?.data || error.message))
      }
    }

    const priceFormatter = (row, column, cellValue) => cellValue ? `￥${cellValue}` : '-'
    const areaFormatter = (row, column, cellValue) => cellValue ? `${cellValue}㎡` : '-'

    onMounted(searchHouses)

    return {
      searchForm, loading, searchResults, searched, selectedHouse, detailVisible, appointmentVisible, applicationVisible,
      appointmentForm, applicationForm, searchHouses, resetForm, openDetail, favorite, openAppointment, submitAppointment,
      openApplication, submitApplication, priceFormatter, areaFormatter
    }
  }
}
</script>

<style scoped>
.house-search { max-width: 1200px; margin: 0 auto; }
.search-form { margin-bottom: 16px; }
.range-inputs { display: flex; align-items: center; gap: 8px; }
.range-inputs :deep(.el-input-number) { width: 120px; }
.empty-tip { margin-top: 16px; }
</style>
