<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>预约管理</span>
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
      <el-table-column prop="message" label="备注"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope"><el-tag>{{ scope.row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="scope">
          <el-button size="small" type="success" :disabled="scope.row.status !== 'PENDING'" @click="openReply(scope.row, 'CONFIRMED')">确认</el-button>
          <el-button size="small" type="danger" :disabled="scope.row.status !== 'PENDING'" @click="openReply(scope.row, 'CANCELLED')">拒绝</el-button>
          <el-button size="small" :disabled="scope.row.status !== 'CONFIRMED'" @click="openReply(scope.row, 'FINISHED')">完成</el-button>
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

    <el-dialog v-model="dialogVisible" title="处理预约" width="460px">
      <el-form label-width="80px">
        <el-form-item label="处理结果">
          <el-tag>{{ replyForm.status }}</el-tag>
        </el-form-item>
        <el-form-item label="回复">
          <el-input v-model="replyForm.reply" type="textarea" :rows="3" placeholder="填写给租客的回复"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'AppointmentManagement',
  setup() {
    const loading = ref(false)
    const appointments = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const dialogVisible = ref(false)
    const replyForm = reactive({ id: null, status: '', reply: '' })
    const loadAppointments = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getLandlordAppointments({ page: page.value, size: size.value })
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
    const openReply = (row, status) => {
      Object.assign(replyForm, { id: row.id, status, reply: '' })
      dialogVisible.value = true
    }
    const submitReply = async () => {
      try {
        await houseAPI.updateAppointmentStatus(replyForm.id, replyForm.status, replyForm.reply)
        ElMessage.success('预约状态已更新')
        dialogVisible.value = false
        loadAppointments()
      } catch (error) {
        ElMessage.error('处理失败: ' + (error.response?.data || error.message))
      }
    }
    onMounted(loadAppointments)
    return { loading, appointments, page, size, total, dialogVisible, replyForm, loadAppointments, handlePageChange, openReply, submitReply }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
