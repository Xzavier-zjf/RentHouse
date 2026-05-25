<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>我的预约</span>
        <el-button type="primary" size="small" @click="loadAppointments">刷新</el-button>
      </div>
    </template>
    <el-table :data="appointments" stripe v-loading="loading">
      <el-table-column label="房源">
        <template #default="scope">{{ scope.row.house?.title }}</template>
      </el-table-column>
      <el-table-column prop="appointmentTime" label="看房时间" min-width="160"></el-table-column>
      <el-table-column prop="contactName" label="联系人"></el-table-column>
      <el-table-column prop="contactPhone" label="电话"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope"><el-tag>{{ scope.row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="reply" label="房东回复"></el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="scope">
          <el-button size="small" type="danger" :disabled="scope.row.status !== 'PENDING'" @click="cancel(scope.row.id)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pagination"
      layout="total, prev, pager, next"
      :current-page="page"
      :page-size="size"
      :total="total"
      @current-change="handlePageChange"
    />
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'MyAppointments',
  setup() {
    const loading = ref(false)
    const appointments = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const loadAppointments = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getMyAppointments({ page: page.value, size: size.value })
        appointments.value = response.data.items || []
        total.value = response.data.total || 0
      } catch (error) {
        ElMessage.error('获取预约失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    const handlePageChange = (nextPage) => {
      page.value = nextPage
      loadAppointments()
    }
    const cancel = async (id) => {
      try {
        await houseAPI.cancelAppointment(id)
        ElMessage.success('预约已取消')
        loadAppointments()
      } catch (error) {
        ElMessage.error('取消失败: ' + (error.response?.data || error.message))
      }
    }
    onMounted(loadAppointments)
    return { loading, appointments, page, size, total, loadAppointments, handlePageChange, cancel }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
