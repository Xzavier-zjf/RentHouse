<template>
  <div class="user-dashboard">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="房源搜索" name="search">
        <HouseSearch />
      </el-tab-pane>
      <el-tab-pane label="申请房东" name="apply">
        <ApplyLandlord />
      </el-tab-pane>
      <el-tab-pane label="个人信息" name="profile">
        <UserProfile />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref } from 'vue'
import HouseSearch from '../components/user/HouseSearch.vue'
import ApplyLandlord from '../components/user/ApplyLandlord.vue'
import UserProfile from '../components/user/UserProfile.vue'

export default {
  name: 'UserDashboard',
  components: {
    HouseSearch,
    ApplyLandlord,
    UserProfile
  },
  setup() {
    const activeTab = ref('search')
    
    return {
      activeTab
    }
  }
}
</script>

<style scoped>
.user-dashboard {
  padding: 20px;
}
</style>