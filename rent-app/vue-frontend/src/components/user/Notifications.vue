<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>站内通知</span>
        <el-button type="primary" size="small" @click="loadNotifications">刷新</el-button>
      </div>
    </template>
    <el-table :data="notifications" stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="140"></el-table-column>
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip></el-table-column>
      <el-table-column prop="type" label="类型" width="150"></el-table-column>
      <el-table-column prop="readStatus" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.readStatus === 'READ' ? 'info' : 'warning'">{{ scope.row.readStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" min-width="170"></el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button size="small" :disabled="scope.row.readStatus === 'READ'" @click="markRead(scope.row.id)">已读</el-button>
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
import { userAPI } from '../../utils/api'

export default {
  name: 'Notifications',
  setup() {
    const loading = ref(false)
    const notifications = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)

    const loadNotifications = async () => {
      loading.value = true
      try {
        const response = await userAPI.getNotifications({ page: page.value, size: size.value })
        notifications.value = response.data.items || []
        total.value = response.data.total || 0
      } catch (error) {
        ElMessage.error('获取通知失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (nextPage) => {
      page.value = nextPage
      loadNotifications()
    }

    const markRead = async (id) => {
      try {
        await userAPI.markNotificationRead(id)
        ElMessage.success('已标记为已读')
        loadNotifications()
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.response?.data || error.message))
      }
    }

    onMounted(loadNotifications)
    return { loading, notifications, page, size, total, loadNotifications, handlePageChange, markRead }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
