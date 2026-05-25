<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>租赁申请管理</span>
        <el-button type="primary" size="small" @click="loadApplications">刷新</el-button>
      </div>
    </template>
    <el-table :data="applications" stripe v-loading="loading">
      <el-table-column label="房源">
        <template #default="scope">{{ scope.row.house?.title }}</template>
      </el-table-column>
      <el-table-column prop="applicantName" label="申请人"></el-table-column>
      <el-table-column prop="applicantPhone" label="电话"></el-table-column>
      <el-table-column prop="moveInDate" label="入住日期"></el-table-column>
      <el-table-column prop="leaseMonths" label="租期(月)"></el-table-column>
      <el-table-column prop="message" label="说明"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope"><el-tag>{{ scope.row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" type="success" :disabled="scope.row.status !== 'PENDING'" @click="openReply(scope.row, 'APPROVED')">通过</el-button>
          <el-button size="small" type="danger" :disabled="scope.row.status !== 'PENDING'" @click="openReply(scope.row, 'REJECTED')">拒绝</el-button>
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

    <el-dialog v-model="dialogVisible" title="处理租赁申请" width="460px">
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
  name: 'RentalApplicationManagement',
  setup() {
    const loading = ref(false)
    const applications = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const dialogVisible = ref(false)
    const replyForm = reactive({ id: null, status: '', reply: '' })
    const loadApplications = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getLandlordRentalApplications({ page: page.value, size: size.value })
        applications.value = response.data.items || []
        total.value = response.data.total || 0
      } catch (error) {
        ElMessage.error('获取租赁申请失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    const handlePageChange = (nextPage) => {
      page.value = nextPage
      loadApplications()
    }
    const openReply = (row, status) => {
      Object.assign(replyForm, { id: row.id, status, reply: '' })
      dialogVisible.value = true
    }
    const submitReply = async () => {
      try {
        await houseAPI.updateRentalApplicationStatus(replyForm.id, replyForm.status, replyForm.reply)
        ElMessage.success('租赁申请状态已更新')
        dialogVisible.value = false
        loadApplications()
      } catch (error) {
        ElMessage.error('处理失败: ' + (error.response?.data || error.message))
      }
    }
    onMounted(loadApplications)
    return { loading, applications, page, size, total, dialogVisible, replyForm, loadApplications, handlePageChange, openReply, submitReply }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
