<template>
  <div class="admin-dashboard">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="用户审核" name="userApproval">
        <UserApproval />
      </el-tab-pane>
      <el-tab-pane label="房源审核" name="houseApproval">
        <HouseApproval />
      </el-tab-pane>
      <el-tab-pane label="所有房源" name="allHouses">
        <AllHouses />
      </el-tab-pane>
      <el-tab-pane label="个人信息" name="profile">
        <UserProfile />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref } from 'vue'
import UserApproval from '../components/admin/UserApproval.vue'
import HouseApproval from '../components/admin/HouseApproval.vue'
import AllHouses from '../components/admin/AllHouses.vue'
import UserProfile from '../components/user/UserProfile.vue'

export default {
  name: 'AdminDashboard',
  components: {
    UserApproval,
    HouseApproval,
    AllHouses,
    UserProfile
  },
  setup() {
    const activeTab = ref('userApproval')
    
    return {
      activeTab
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}
</style>