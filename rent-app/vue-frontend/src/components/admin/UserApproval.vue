<template>
  <div class="user-approval">
    <el-card>
      <h3>用户审核</h3>
      <div class="actions">
        <el-button type="primary" @click="getAllUsers" :loading="loading">刷新用户列表</el-button>
      </div>
      
      <div v-if="users.length > 0" class="users-list">
        <el-table :data="users" stripe style="width: 100%">
          <el-table-column prop="id" label="用户ID"></el-table-column>
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column prop="role" label="角色"></el-table-column>
          <el-table-column prop="landlordApplicationStatus" label="房东申请状态">
            <template #default="scope">
              <el-tag :type="getLandlordStatusTagType(scope.row.landlordApplicationStatus)">
                {{ scope.row.landlordApplicationStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                @click="approveLandlord(scope.row.id)"
                :disabled="scope.row.landlordApplicationStatus !== 'PENDING'"
              >
                批准房东申请
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else class="no-users">
        <el-alert title="暂无用户信息" type="info" show-icon></el-alert>
      </div>
    </el-card>
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
    
    const getAllUsers = async () => {
      loading.value = true
      try {
        const response = await userAPI.getAllUsers()
        users.value = response.data
      } catch (error) {
        ElMessage.error('获取用户列表失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    
    const approveLandlord = async (userId) => {
      try {
        await userAPI.approveLandlord(userId)
        ElMessage.success('房东申请已批准')
        getAllUsers() // 刷新用户列表
      } catch (error) {
        ElMessage.error('批准失败: ' + (error.response?.data || error.message))
      }
    }
    
    const getLandlordStatusTagType = (status) => {
      switch(status) {
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
    
    onMounted(() => {
      getAllUsers()
    })
    
    return {
      loading,
      users,
      getAllUsers,
      approveLandlord,
      getLandlordStatusTagType
    }
  }
}
</script>

<style scoped>
.user-approval {
  max-width: 1200px;
  margin: 0 auto;
}

.actions {
  margin-bottom: 20px;
  text-align: right;
}

.users-list {
  margin-top: 20px;
}

.no-users {
  margin-top: 20px;
}
</style>