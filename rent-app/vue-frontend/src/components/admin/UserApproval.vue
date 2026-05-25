<template>
  <div class="user-approval">
    <el-card>
      <template #header>
        <div class="header">
          <span>用户审核</span>
          <el-button type="primary" @click="getAllUsers" :loading="loading">刷新用户列表</el-button>
        </div>
      </template>

      <el-table :data="users" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="用户ID" width="90"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="role" label="角色" width="110"></el-table-column>
        <el-table-column prop="landlordApplyStatus" label="房东申请状态" width="150">
          <template #default="scope">
            <el-tag :type="getLandlordStatusTagType(scope.row.landlordApplyStatus)">
              {{ scope.row.landlordApplyStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="landlordApplyReason" label="申请原因/拒绝原因" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="scope">
            <el-button size="small" type="success" @click="approveLandlord(scope.row.id)" :disabled="scope.row.landlordApplyStatus !== 'PENDING'">
              批准
            </el-button>
            <el-button size="small" type="danger" @click="openReject(scope.row)" :disabled="scope.row.landlordApplyStatus !== 'PENDING'">
              拒绝
            </el-button>
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

    <el-dialog v-model="rejectVisible" title="拒绝房东申请" width="460px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="rejectLandlord">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '../../utils/api'

export default {
  name: 'UserApproval',
  setup() {
    const loading = ref(false)
    const users = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const rejectVisible = ref(false)
    const rejectUserId = ref(null)
    const rejectReason = ref('')

    const getAllUsers = async () => {
      loading.value = true
      try {
        const response = await userAPI.getAllUsers({ page: page.value, size: size.value })
        users.value = response.data.items || []
        total.value = response.data.total || 0
      } catch (error) {
        ElMessage.error('获取用户列表失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (nextPage) => {
      page.value = nextPage
      getAllUsers()
    }

    const approveLandlord = async (userId) => {
      try {
        await userAPI.approveLandlord(userId)
        ElMessage.success('房东申请已批准')
        getAllUsers()
      } catch (error) {
        ElMessage.error('批准失败: ' + (error.response?.data || error.message))
      }
    }

    const openReject = (user) => {
      rejectUserId.value = user.id
      rejectReason.value = '申请资料不符合房东入驻要求'
      rejectVisible.value = true
    }

    const rejectLandlord = async () => {
      try {
        await userAPI.rejectLandlord(rejectUserId.value, rejectReason.value)
        ElMessage.success('房东申请已拒绝')
        rejectVisible.value = false
        getAllUsers()
      } catch (error) {
        ElMessage.error('拒绝失败: ' + (error.response?.data || error.message))
      }
    }

    const getLandlordStatusTagType = (status) => {
      switch (status) {
        case 'APPROVED':
          return 'success'
        case 'REJECTED':
          return 'danger'
        case 'PENDING':
          return 'warning'
        default:
          return 'info'
      }
    }

    onMounted(getAllUsers)

    return {
      loading, users, page, size, total, rejectVisible, rejectReason,
      getAllUsers, handlePageChange, approveLandlord, openReject, rejectLandlord, getLandlordStatusTagType
    }
  }
}
</script>

<style scoped>
.user-approval { max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
