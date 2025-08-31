<template>
  <div class="house-upload">
    <el-card>
      <h3>上传房源</h3>
      <el-form :model="houseForm" :rules="rules" ref="houseFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="houseForm.title" placeholder="请输入房源标题"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="houseForm.price" :min="0" placeholder="请输入价格"></el-input-number>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="houseForm.location" placeholder="请输入位置"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="houseForm.description" type="textarea" placeholder="请输入房源描述"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="uploadHouse" :loading="loading">上传房源</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseUpload',
  setup() {
    const houseFormRef = ref(null)
    const loading = ref(false)
    
    const houseForm = reactive({
      title: '',
      price: 0,
      location: '',
      description: ''
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入房源标题', trigger: 'blur' }
      ],
      price: [
        { required: true, message: '请输入价格', trigger: 'blur' }
      ],
      location: [
        { required: true, message: '请输入位置', trigger: 'blur' }
      ]
    }
    
    const uploadHouse = async () => {
      houseFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await houseAPI.uploadHouse({
              title: houseForm.title,
              price: houseForm.price,
              location: houseForm.location,
              description: houseForm.description
            })
            
            ElMessage.success('房源上传成功')
            resetForm()
          } catch (error) {
            ElMessage.error('上传失败: ' + (error.response?.data || error.message))
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const resetForm = () => {
      houseFormRef.value.resetFields()
    }
    
    return {
      houseForm,
      rules,
      houseFormRef,
      loading,
      uploadHouse,
      resetForm
    }
  }
}
</script>

<style scoped>
.house-upload {
  max-width: 600px;
  margin: 0 auto;
}
</style>