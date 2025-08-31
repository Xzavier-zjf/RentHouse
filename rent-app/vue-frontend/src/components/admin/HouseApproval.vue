<template>
  <div class="house-approval">
    <el-card>
      <h3>房源审核</h3>
      <div class="actions">
        <el-button type="primary" @click="getAllHouses" :loading="loading">刷新房源列表</el-button>
      </div>
      
      <div v-if="houses.length > 0" class="houses-list">
        <el-table :data="houses" stripe style="width: 100%">
          <el-table-column prop="id" label="房源ID"></el-table-column>
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="price" label="价格" :formatter="priceFormatter"></el-table-column>
          <el-table-column prop="location" label="位置"></el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getHouseStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <div v-if="scope.row.status === 'PENDING'">
                <el-button size="small" type="success" @click="approveHouse(scope.row.id, 'APPROVED')">批准</el-button>
                <el-button size="small" type="danger" @click="approveHouse(scope.row.id, 'REJECTED')">拒绝</el-button>
              </div>
              <div v-else>
                <el-button size="small" disabled>已处理</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else class="no-houses">
        <el-alert title="暂无房源信息" type="info" show-icon></el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseApproval',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    
    const getAllHouses = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getAllHouses()
        houses.value = response.data
      } catch (error) {
        ElMessage.error('获取房源列表失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    
    const approveHouse = async (houseId, status) => {
      try {
        const reason = status === 'REJECTED' ? '不符合要求' : ''
        await houseAPI.approveHouse(houseId, status, reason)
        
        ElMessage.success(`房源已${status === 'APPROVED' ? '批准' : '拒绝'}`)
        getAllHouses() // 刷新房源列表
      } catch (error) {
        ElMessage.error('审核失败: ' + (error.response?.data || error.message))
      }
    }
    
    const priceFormatter = (row, column, cellValue) => {
      return `￥${cellValue}`
    }
    
    const getHouseStatusTagType = (status) => {
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
      getAllHouses()
    })
    
    return {
      loading,
      houses,
      getAllHouses,
      approveHouse,
      priceFormatter,
      getHouseStatusTagType
    }
  }
}
</script>

<style scoped>
.house-approval {
  max-width: 1200px;
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