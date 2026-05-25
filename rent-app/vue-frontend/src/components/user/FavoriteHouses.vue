<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>我的收藏</span>
        <el-button type="primary" size="small" @click="loadFavorites">刷新</el-button>
      </div>
    </template>
    <el-table :data="favorites" stripe v-loading="loading">
      <el-table-column label="房源">
        <template #default="scope">{{ scope.row.house?.title }}</template>
      </el-table-column>
      <el-table-column label="位置">
        <template #default="scope">{{ scope.row.house?.location }}</template>
      </el-table-column>
      <el-table-column label="租金">
        <template #default="scope">￥{{ scope.row.house?.price }}</template>
      </el-table-column>
      <el-table-column label="户型">
        <template #default="scope">{{ scope.row.house?.layout || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button size="small" type="danger" @click="removeFavorite(scope.row.houseId)">取消收藏</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../../utils/api'

export default {
  name: 'FavoriteHouses',
  setup() {
    const loading = ref(false)
    const favorites = ref([])

    const loadFavorites = async () => {
      loading.value = true
      try {
        const response = await houseAPI.getFavorites()
        favorites.value = response.data
      } catch (error) {
        ElMessage.error('获取收藏失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }

    const removeFavorite = async (houseId) => {
      try {
        await houseAPI.removeFavorite(houseId)
        ElMessage.success('已取消收藏')
        loadFavorites()
      } catch (error) {
        ElMessage.error('取消收藏失败: ' + (error.response?.data || error.message))
      }
    }

    onMounted(loadFavorites)
    return { loading, favorites, loadFavorites, removeFavorite }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
