<template>
  <div class="user-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" size="small" @click="editMode = !editMode">
            {{ editMode ? '取消编辑' : '编辑信息' }}
          </el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="profile" class="profile-content">
        <!-- 基本信息 -->
        <div class="info-section">
          <h3>基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">{{ profile.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="getRoleTagType(profile.role)">{{ getRoleText(profile.role) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              <span v-if="!editMode">{{ profile.email || '未设置' }}</span>
              <el-input v-else v-model="editForm.email" placeholder="请输入邮箱" />
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 角色特定信息 -->
        <div v-if="profile.role === 'USER'" class="info-section">
          <h3>用户状态</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="房东申请状态">
              <el-tag :type="getApplyStatusTagType(profile.landlordApplyStatus)">
                {{ getApplyStatusText(profile.landlordApplyStatus) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="profile.role === 'LANDLORD'" class="info-section">
          <h3>房东信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="认证状态">
              <el-tag type="success">已认证房东</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="房源管理">
              <el-button type="text" @click="$router.push('/landlord')">查看我的房源</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="profile.role === 'ADMIN'" class="info-section">
          <h3>管理员权限</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="权限级别">
              <el-tag type="danger">系统管理员</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="管理功能">
              <el-button type="text" @click="$router.push('/admin')">进入管理后台</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 编辑模式下的保存按钮 -->
        <div v-if="editMode" class="edit-actions">
          <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '../../utils/api'

export default {
  name: 'UserProfile',
  setup() {
    const loading = ref(true)
    const saving = ref(false)
    const editMode = ref(false)
    const profile = ref(null)
    
    const editForm = reactive({
      email: ''
    })

    const loadProfile = async () => {
      try {
        loading.value = true
        console.log('开始获取个人信息...')
        const response = await userAPI.getProfile()
        console.log('个人信息响应:', response.data)
        profile.value = response.data
        editForm.email = profile.value.email || ''
      } catch (error) {
        console.error('获取个人信息错误:', error)
        console.error('错误详情:', error.response)
        ElMessage.error('获取个人信息失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }

    const saveProfile = async () => {
      try {
        saving.value = true
        await userAPI.updateProfile({
          email: editForm.email
        })
        
        // 更新本地数据
        profile.value.email = editForm.email
        editMode.value = false
        
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        ElMessage.error('更新失败: ' + (error.response?.data || error.message))
      } finally {
        saving.value = false
      }
    }

    const cancelEdit = () => {
      editForm.email = profile.value.email || ''
      editMode.value = false
    }

    const getRoleText = (role) => {
      const roleMap = {
        'USER': '普通用户',
        'LANDLORD': '房东',
        'ADMIN': '管理员'
      }
      return roleMap[role] || role
    }

    const getRoleTagType = (role) => {
      const typeMap = {
        'USER': 'info',
        'LANDLORD': 'warning',
        'ADMIN': 'danger'
      }
      return typeMap[role] || 'info'
    }

    const getApplyStatusText = (status) => {
      const statusMap = {
        'NOT_APPLIED': '未申请',
        'PENDING': '审核中',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || status
    }

    const getApplyStatusTagType = (status) => {
      const typeMap = {
        'NOT_APPLIED': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return typeMap[status] || 'info'
    }

    onMounted(() => {
      loadProfile()
    })

    return {
      loading,
      saving,
      editMode,
      profile,
      editForm,
      saveProfile,
      cancelEdit,
      getRoleText,
      getRoleTagType,
      getApplyStatusText,
      getApplyStatusTagType
    }
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  padding: 20px 0;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.edit-actions {
  margin-top: 20px;
  text-align: center;
}

.edit-actions .el-button {
  margin: 0 10px;
}

.loading {
  padding: 20px;
}
</style>
