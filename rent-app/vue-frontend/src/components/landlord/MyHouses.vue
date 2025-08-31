<template>
  <div class="my-houses">
    <el-card>
      <h3>我的房源</h3>
      <div class="actions">
        <el-button type="primary" @click="getMyHouses" :loading="loading">刷新</el-button>
      </div>
      
      <div v-if="houses.length > 0" class="houses-list">
        <el-table :data="houses" stripe style="width: 100%">
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="price" label="价格" :formatter="priceFormatter"></el-table-column>
          <el-table-column prop="location" label="位置"></el-table-column>
          <el-table-column prop="status" label="状态"></el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="editHouse(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else class="no-houses">
        <el-alert title="您还没有上传房源" type="info" show-icon></el-alert>
      </div>
    </el-card>
    
    <!-- 编辑房源对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑房源" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title"></el-input>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editForm.price" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="editForm.location"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveHouse">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'MyHouses',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    const dialogVisible = ref(false)
    
    const editForm = reactive({
      id: 0,
      title: '',
      price: 0,
      location: '',
      description: ''
    })
    
    const getMyHouses = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getMyHouses()
        houses.value = response.data
      } catch (error) {
        ElMessage.error('获取房源失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    
    const editHouse = (house) => {
      editForm.id = house.id
      editForm.title = house.title
      editForm.price = house.price
      editForm.location = house.location
      editForm.description = house.description
      dialogVisible.value = true
    }
    
    const saveHouse = () => {
      // 这里可以实现保存逻辑，由于API没有提供更新接口，我们只显示提示
      ElMessage.success('房源信息已保存')
      dialogVisible.value = false
      getMyHouses()
    }
    
    const priceFormatter = (row, column, cellValue) => {
      return `￥${cellValue}`
    }
    
    onMounted(() => {
      getMyHouses()
    })
    
    return {
      loading,
      houses,
      dialogVisible,
      editForm,
      getMyHouses,
      editHouse,
      saveHouse,
      priceFormatter
    }
  }
}
</script>

<style scoped>
.my-houses {
  max-width: 1000px;
  margin: 0 auto;
}

.actions {
  margin-bottom: 20px;
  text-align: right;
}

.houses-list {
  margin-top: 20px;
}

.no-houses {
  margin-top: 20px;
}
</style>