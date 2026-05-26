<template>
  <div class="all-houses">
    <el-card>
      <h3>所有房源</h3>
      <div class="actions">
        <el-button type="primary" @click="getAllHouses" :loading="loading">刷新</el-button>
      </div>
      
      <div v-if="houses.length > 0" class="houses-list">
        <el-table :data="houses" stripe style="width: 100%">
          <el-table-column prop="id" label="房源ID"></el-table-column>
          <el-table-column label="封面" width="110">
            <template #default="scope">
              <el-image v-if="coverUrl(scope.row)" :src="coverUrl(scope.row)" fit="cover" class="cover-image" />
              <span v-else class="no-image">暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="price" label="月租金" :formatter="priceFormatter"></el-table-column>
          <el-table-column prop="location" label="位置"></el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getHouseStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="房东"></el-table-column>
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
import { assetUrl, houseAPI } from '../../utils/api'

export default {
  name: 'AllHouses',
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
    
    const priceFormatter = (row, column, cellValue) => {
      return cellValue ? `￥${cellValue}/月` : '-'
    }
    const coverUrl = (house) => assetUrl(house.coverImageUrl)
    
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
      priceFormatter,
      coverUrl,
      getHouseStatusTagType
    }
  }
}
</script>

<style scoped>
.all-houses {
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

.cover-image {
  width: 76px;
  height: 56px;
  border-radius: 4px;
  background: #f5f7fa;
}

.no-image {
  color: #909399;
  font-size: 12px;
}
</style>
