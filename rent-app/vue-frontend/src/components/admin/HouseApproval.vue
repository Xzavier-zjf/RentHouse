<template>
  <div class="house-approval">
    <el-card>
      <template #header>
        <div class="header">
          <span>房源审核</span>
          <div>
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable class="status-select" @change="getAllHouses">
              <el-option label="待审核" value="PENDING"></el-option>
              <el-option label="已通过" value="APPROVED"></el-option>
              <el-option label="已拒绝" value="REJECTED"></el-option>
              <el-option label="已下架" value="OFFLINE"></el-option>
            </el-select>
            <el-button type="primary" @click="getAllHouses" :loading="loading">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="houses" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="140"></el-table-column>
        <el-table-column prop="price" label="租金" width="100" :formatter="priceFormatter"></el-table-column>
        <el-table-column prop="location" label="位置" min-width="130"></el-table-column>
        <el-table-column prop="layout" label="户型" width="110"></el-table-column>
        <el-table-column prop="area" label="面积" width="90"></el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="scope">
            <el-tag :type="getHouseStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核原因" min-width="140"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="success" :disabled="scope.row.status !== 'PENDING'" @click="approveHouse(scope.row.id, 'APPROVED')">批准</el-button>
            <el-button size="small" type="danger" :disabled="scope.row.status !== 'PENDING'" @click="openReject(scope.row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectVisible" title="拒绝房源" width="460px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseApproval',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    const statusFilter = ref('PENDING')
    const rejectVisible = ref(false)
    const rejectHouseId = ref(null)
    const rejectReason = ref('')
    const getAllHouses = async () => {
      loading.value = true
      try {
        houses.value = (await houseAPI.getAllHouses(statusFilter.value)).data
      } catch (error) {
        ElMessage.error('获取房源列表失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    const approveHouse = async (houseId, status, reason = '') => {
      try {
        await houseAPI.approveHouse(houseId, status, reason)
        ElMessage.success(`房源已${status === 'APPROVED' ? '批准' : '拒绝'}`)
        getAllHouses()
      } catch (error) {
        ElMessage.error('审核失败: ' + (error.response?.data || error.message))
      }
    }
    const openReject = (house) => {
      rejectHouseId.value = house.id
      rejectReason.value = house.reason || '房源信息不完整或不符合发布要求'
      rejectVisible.value = true
    }
    const submitReject = async () => {
      await approveHouse(rejectHouseId.value, 'REJECTED', rejectReason.value)
      rejectVisible.value = false
    }
    const priceFormatter = (row, column, cellValue) => cellValue ? `￥${cellValue}` : '-'
    const getHouseStatusTagType = (status) => ({ APPROVED: 'success', REJECTED: 'danger', PENDING: 'warning', OFFLINE: 'info' }[status] || 'info')
    onMounted(getAllHouses)
    return { loading, houses, statusFilter, rejectVisible, rejectReason, getAllHouses, approveHouse, openReject, submitReject, priceFormatter, getHouseStatusTagType }
  }
}
</script>

<style scoped>
.house-approval { max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.status-select { width: 130px; margin-right: 10px; }
</style>
