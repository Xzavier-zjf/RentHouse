<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>我的收藏</span>
        <el-button type="primary" size="small" @click="loadFavorites">刷新</el-button>
      </div>
    </template>
    <el-table :data="favorites" stripe v-loading="loading">
      <el-table-column label="封面" width="110">
        <template #default="scope">
          <el-image v-if="coverUrl(scope.row.house)" :src="coverUrl(scope.row.house)" fit="cover" class="cover-image" />
          <span v-else class="no-image">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="房源">
        <template #default="scope">{{ scope.row.house?.title }}</template>
      </el-table-column>
      <el-table-column label="位置">
        <template #default="scope">{{ scope.row.house?.location }}</template>
      </el-table-column>
      <el-table-column label="月租金">
        <template #default="scope">￥{{ scope.row.house?.price }}/月</template>
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
import { computed, ref, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { assetUrl, houseAPI } from '../../utils/api'

export default {
  name: 'FavoriteHouses',
  setup() {
    const store = useStore()
    const loading = ref(false)
    const favorites = ref([])
    const favoriteRefreshKey = computed(() => store.getters.favoriteRefreshKey)

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
    const coverUrl = (house) => assetUrl(house?.coverImageUrl)

    const removeFavorite = async (houseId) => {
      try {
        await houseAPI.removeFavorite(houseId)
        ElMessage.success('已取消收藏')
        loadFavorites()
      } catch (error) {
        ElMessage.error('取消收藏失败: ' + (error.response?.data || error.message))
      }
    }

    watch(favoriteRefreshKey, () => {
      loadFavorites()
    })

    onMounted(loadFavorites)
    return { loading, favorites, loadFavorites, removeFavorite, coverUrl }
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.cover-image { width: 76px; height: 56px; border-radius: 4px; background: #f5f7fa; }
.no-image { color: #909399; font-size: 12px; }
</style>
