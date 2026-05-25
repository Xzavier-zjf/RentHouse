<template>
  <div class="statistics-overview">
    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="item in cards" :key="item.key" :span="6">
        <el-card class="metric-card">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ statistics[item.key] || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="applications-card">
      <template #header>
        <div class="header">
          <span>租赁申请总览</span>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </div>
      </template>
      <el-table :data="applications" stripe>
        <el-table-column label="房源">
          <template #default="scope">{{ scope.row.house?.title }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人"></el-table-column>
        <el-table-column prop="applicantPhone" label="电话"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope"><el-tag>{{ scope.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="160"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'StatisticsOverview',
  setup() {
    const loading = ref(false)
    const statistics = ref({})
    const applications = ref([])
    const cards = computed(() => [
      { key: 'userCount', label: '用户总数' },
      { key: 'landlordCount', label: '房东数量' },
      { key: 'pendingLandlordCount', label: '待审房东' },
      { key: 'houseCount', label: '房源总数' },
      { key: 'pendingHouseCount', label: '待审房源' },
      { key: 'appointmentCount', label: '预约数量' },
      { key: 'rentalApplicationCount', label: '租赁申请' },
      { key: 'pendingRentalApplicationCount', label: '待处理申请' }
    ])
    const loadData = async () => {
      loading.value = true
      try {
        const [statisticsResponse, applicationsResponse] = await Promise.all([
          houseAPI.getStatistics(),
          houseAPI.getAllRentalApplications()
        ])
        statistics.value = statisticsResponse.data
        applications.value = applicationsResponse.data
      } catch (error) {
        ElMessage.error('获取统计数据失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    onMounted(loadData)
    return { loading, statistics, applications, cards, loadData }
  }
}
</script>

<style scoped>
.statistics-overview { max-width: 1200px; margin: 0 auto; }
.metric-card { margin-bottom: 16px; }
.metric-label { color: #666; font-size: 14px; }
.metric-value { margin-top: 8px; font-size: 28px; font-weight: 700; color: #303133; }
.applications-card { margin-top: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
