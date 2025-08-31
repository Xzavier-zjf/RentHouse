<template>
  <div class="login">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="register-link">
        还没有账号？<el-button type="text" @click="$router.push('/register')">立即注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { userAPI } from '../utils/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loginFormRef = ref(null)
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            console.log('开始登录请求...')
            const response = await userAPI.login({
              username: loginForm.username,
              password: loginForm.password
            })
            
            console.log('登录响应:', response.data)
            const { token, role } = response.data
            
            // 如果角色为空，设置默认角色
            const userRole = role || 'USER'
            console.log('用户角色:', userRole)
            
            const user = {
              username: loginForm.username,
              role: userRole
            }
            
            // 保存认证信息到store
            store.dispatch('login', { token, user })
            
            ElMessage.success('登录成功')
            
            // 延迟一下再跳转，确保store更新完成
            setTimeout(() => {
              console.log('准备跳转，用户角色:', userRole)
              // 根据用户角色跳转到相应仪表板
              switch(userRole) {
                case 'USER':
                  console.log('跳转到用户仪表板')
                  router.push('/user')
                  break
                case 'LANDLORD':
                  console.log('跳转到房东仪表板')
                  router.push('/landlord')
                  break
                case 'ADMIN':
                  console.log('跳转到管理员仪表板')
                  router.push('/admin')
                  break
                default:
                  console.log('跳转到默认用户仪表板')
                  router.push('/user')
              }
            }, 100)
          } catch (error) {
            console.error('登录错误:', error)
            ElMessage.error('登录失败: ' + (error.response?.data || error.message))
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const resetForm = () => {
      loginFormRef.value.resetFields()
    }
    
    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      handleLogin,
      resetForm
    }
  }
}
</script>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.login-card {
  width: 400px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}
</style>