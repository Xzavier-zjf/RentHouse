<template>
  <div class="landlord-dashboard">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="上传房源" name="upload">
        <HouseUpload />
      </el-tab-pane>
      <el-tab-pane label="我的房源" name="myHouses">
        <MyHouses />
      </el-tab-pane>
      <el-tab-pane label="个人信息" name="profile">
        <UserProfile />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref } from 'vue'
import HouseUpload from '../components/landlord/HouseUpload.vue'
import MyHouses from '../components/landlord/MyHouses.vue'
import UserProfile from '../components/user/UserProfile.vue'

export default {
  name: 'LandlordDashboard',
  components: {
    HouseUpload,
    MyHouses,
    UserProfile
  },
  setup() {
    const activeTab = ref('upload')
    
    return {
      activeTab
    }
  }
}
</script>

<style scoped>
.landlord-dashboard {
  padding: 20px;
}
</style>