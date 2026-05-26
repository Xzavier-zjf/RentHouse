<template>
  <div class="register">
    <el-card class="register-card">
      <div class="card-header">
        <el-button type="text" @click="$router.push('/')">返回首页</el-button>
        <h2>用户注册</h2>
      </div>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="login-link">
        已有账号？<el-button type="text" @click="$router.push('/login')">立即登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userAPI } from '../utils/api'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)
    
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      email: ''
    })
    
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (registerForm.confirmPassword !== '') {
          registerFormRef.value.validateField('confirmPassword')
        }
        callback()
      }
    }
    
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, validator: validatePass, trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePass2, trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }
    
    const handleRegister = async () => {
      registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await userAPI.register({
              username: registerForm.username,
              password: registerForm.password,
              email: registerForm.email
            })
            
            ElMessage.success('注册成功')
            router.push('/login')
          } catch (error) {
            ElMessage.error('注册失败: ' + (error.response?.data || error.message))
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const resetForm = () => {
      registerFormRef.value.resetFields()
    }
    
    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      handleRegister,
      resetForm
    }
  }
}
</script>

<style scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.register-card {
  width: 400px;
}

.card-header {
  position: relative;
  margin-bottom: 18px;
}

.card-header h2 {
  margin: 0;
  text-align: center;
}

.card-header .el-button {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
}

.login-link {
  text-align: center;
  margin-top: 20px;
}
</style>
