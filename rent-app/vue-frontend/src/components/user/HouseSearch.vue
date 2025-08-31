<template>
  <div class="house-search">
    <el-card>
      <h3>房源搜索</h3>
      <el-form :model="searchForm" @submit.prevent="searchHouses">
        <el-form-item label="位置">
          <el-input v-model="searchForm.location" placeholder="请输入位置"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchHouses" :loading="loading">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="searchResults.length > 0" class="search-results">
        <h4>搜索结果</h4>
        <el-table :data="searchResults" stripe style="width: 100%">
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="price" label="价格" :formatter="priceFormatter"></el-table-column>
          <el-table-column prop="location" label="位置"></el-table-column>
          <el-table-column prop="status" label="状态"></el-table-column>
        </el-table>
      </div>
      
      <div v-if="searchResults.length === 0 && searched" class="no-results">
        <el-alert title="未找到符合条件的房源" type="info" show-icon></el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'HouseSearch',
  setup() {
    const searchForm = reactive({
      location: ''
    })
    
    const loading = ref(false)
    const searchResults = ref([])
    const searched = ref(false)
    
    const searchHouses = async () => {
      loading.value = true
      try {
        const response = await houseAPI.searchHouses(searchForm.location)
        searchResults.value = response.data
        searched.value = true
      } catch (error) {
        ElMessage.error('搜索失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = () => {
      searchForm.location = ''
      searchResults.value = []
      searched.value = false
    }
    
    const priceFormatter = (row, column, cellValue) => {
      return `￥${cellValue}`
    }
    
    return {
      searchForm,
      loading,
      searchResults,
      searched,
      searchHouses,
      resetForm,
      priceFormatter
    }
  }
}
</script>

<style scoped>
.house-search {
  max-width: 800px;
  margin: 0 auto;
}

.search-results {
  margin-top: 20px;
}

.no-results {
  margin-top: 20px;
}
</style>