<template>
  <div class="house-upload">
    <el-card>
      <h3>上传房源</h3>
      <el-form :model="houseForm" :rules="rules" ref="houseFormRef" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="houseForm.title" placeholder="请输入房源标题"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租金" prop="price">
              <el-input-number v-model="houseForm.price" :min="1"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="位置" prop="location">
              <el-input v-model="houseForm.location" placeholder="请输入位置"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户型">
              <el-input v-model="houseForm.layout" placeholder="如 两室一厅"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="面积">
              <el-input-number v-model="houseForm.area" :min="0"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="楼层">
              <el-input v-model="houseForm.floor" placeholder="如 6/18层"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="朝向">
              <el-input v-model="houseForm.orientation" placeholder="如 南向"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="houseForm.contactName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="houseForm.contactPhone"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片地址">
          <el-input v-model="houseForm.imageUrl" placeholder="可填写网络图片地址"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="houseForm.description" type="textarea" :rows="4" placeholder="请输入房源描述"></el-input>
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
    const initialForm = () => ({
      title: '', price: 1, location: '', description: '', layout: '', area: null,
      floor: '', orientation: '', imageUrl: '', contactName: '', contactPhone: ''
    })
    const houseForm = reactive(initialForm())
    const rules = {
      title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
      price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
      location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
    }
    const uploadHouse = async () => {
      houseFormRef.value.validate(async (valid) => {
        if (!valid) return
        loading.value = true
        try {
          await houseAPI.uploadHouse(houseForm)
          ElMessage.success('房源上传成功，等待管理员审核')
          resetForm()
        } catch (error) {
          ElMessage.error('上传失败: ' + (error.response?.data || error.message))
        } finally {
          loading.value = false
        }
      })
    }
    const resetForm = () => {
      Object.assign(houseForm, initialForm())
      houseFormRef.value?.clearValidate()
    }
    return { houseForm, rules, houseFormRef, loading, uploadHouse, resetForm }
  }
}
</script>

<style scoped>
.house-upload { max-width: 980px; margin: 0 auto; }
</style>
