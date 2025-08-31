<template>
  <div class="apply-landlord">
    <el-card>
      <h3>申请成为房东</h3>
      <el-alert title="申请提交后需要管理员审核，请耐心等待" type="info" show-icon></el-alert>
      <div class="actions">
        <el-button type="primary" @click="applyLandlord" :loading="loading" :disabled="isApplied">
          {{ isApplied ? '已申请' : '申请成为房东' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '../../utils/api'

export default {
  name: 'ApplyLandlord',
  setup() {
    const loading = ref(false)
    const isApplied = ref(false)
    
    const applyLandlord = async () => {
      loading.value = true
      try {
        console.log('开始申请房东...')
        const token = localStorage.getItem('authToken')
        console.log('当前令牌:', token)
        const userRole = localStorage.getItem('userRole')
        console.log('当前用户角色:', userRole)
        
        await userAPI.applyLandlord()
        ElMessage.success('申请已提交，请等待管理员审核')
        isApplied.value = true
      } catch (error) {
        console.error('申请房东错误:', error)
        console.error('错误响应:', error.response)
        console.error('错误状态码:', error.response?.status)
        console.error('错误数据:', error.response?.data)
        console.error('请求URL:', error.config?.url)
        console.error('请求方法:', error.config?.method)
        console.error('请求头:', error.config?.headers)
        
        let errorMessage = '申请失败'
        if (error.response?.data) {
          errorMessage += ': ' + error.response.data
        } else if (error.message) {
          errorMessage += ': ' + error.message
        }
        
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }
    
    return {
      loading,
      isApplied,
      applyLandlord
    }
  }
}
</script>

<style scoped>
.apply-landlord {
  max-width: 600px;
  margin: 0 auto;
}

.actions {
  margin-top: 20px;
  text-align: center;
}
</style>