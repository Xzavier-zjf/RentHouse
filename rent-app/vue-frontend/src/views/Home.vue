<template>
  <div class="home">
    <section class="topbar">
      <div>
        <h2>RentHouse 租房平台</h2>
        <p>浏览已审核房源，登录后可收藏、预约看房并提交租赁申请。</p>
      </div>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/login')">登录</el-button>
        <el-button type="success" @click="$router.push('/register')">注册</el-button>
      </div>
    </section>

    <el-card>
      <template #header>
        <div class="header">
          <span>推荐房源</span>
          <el-input v-model="location" placeholder="按位置搜索" class="search-input" clearable @keyup.enter="loadHouses">
            <template #append>
              <el-button @click="loadHouses">搜索</el-button>
            </template>
          </el-input>
        </div>
      </template>
      <el-table :data="houses" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="150"></el-table-column>
        <el-table-column prop="price" label="租金" width="110">
          <template #default="scope">￥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="150"></el-table-column>
        <el-table-column prop="layout" label="户型" width="120"></el-table-column>
        <el-table-column prop="area" label="面积" width="100">
          <template #default="scope">{{ scope.row.area ? `${scope.row.area}㎡` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { houseAPI } from '../utils/api'

export default {
  name: 'Home',
  setup() {
    const loading = ref(false)
    const houses = ref([])
    const location = ref('')
    const loadHouses = async () => {
      loading.value = true
      try {
        houses.value = (await houseAPI.searchHouses({ location: location.value })).data
      } catch (error) {
        ElMessage.error('获取房源失败: ' + (error.response?.data || error.message))
      } finally {
        loading.value = false
      }
    }
    onMounted(loadHouses)
    return { loading, houses, location, loadHouses }
  }
}
</script>

<style scoped>
.home { max-width: 1180px; margin: 0 auto; padding: 20px; }
.topbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.topbar h2 { margin: 0 0 8px; }
.topbar p { margin: 0; color: #606266; }
.actions { display: flex; gap: 10px; }
.header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.search-input { width: 360px; }
</style>
