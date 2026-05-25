<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>我的租赁申请</span>
        <el-button type="primary" size="small" @click="loadApplications">刷新</el-button>
      </div>
    </template>
    <el-table :data="applications" stripe v-loading="loading">
      <el-table-column label="房源">
        <template #default="scope">{{ scope.row.house?.title }}</template>
      </el-table-column>
      <el-table-column prop="applicantName" label="申请人"></el-table-column>
      <el-table-column prop="moveInDate" label="入住日期"></el-table-column>
      <el-table-column prop="leaseMonths" label="租期(月)"></el-table-column>
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
  name: 'MyRentalApplications',
  setup() {
    const loading = ref(false)
    const applications = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const loadApplications = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getMyRentalApplications({ page: page.value, size: size.value })
        applications.value = response.data.items || []
        total.value = response.data.total || 0
      } catch (error) {
        ElMessage.error('获取申请失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    const handlePageChange = (nextPage) => {
      page.value = nextPage
      loadApplications()
    }
    const cancel = async (id) => {
      try {
        await houseAPI.cancelRentalApplication(id)
        ElMessage.success('申请已取消')
        loadApplications()
      } catch (error) {
        ElMessage.error('取消失败: ' + (error.response?.data || error.message))
      }
    }
    onMounted(loadApplications)
    return { loading, applications, page, size, total, loadApplications, handlePageChange, cancel }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
