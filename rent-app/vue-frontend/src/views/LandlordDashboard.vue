<template>
  <div class="landlord-dashboard">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="上传房源" name="upload">
        <HouseUpload />
      </el-tab-pane>
      <el-tab-pane label="我的房源" name="myHouses">
        <MyHouses />
      </el-tab-pane>
      <el-tab-pane label="预约管理" name="appointments">
        <AppointmentManagement />
      </el-tab-pane>
      <el-tab-pane label="租赁申请" name="applications">
        <RentalApplicationManagement />
      </el-tab-pane>
      <el-tab-pane label="站内通知" name="notifications">
        <Notifications />
      </el-tab-pane>
      <el-tab-pane label="个人信息" name="profile">
        <UserProfile />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HouseUpload from '../components/landlord/HouseUpload.vue'
import MyHouses from '../components/landlord/MyHouses.vue'
import AppointmentManagement from '../components/landlord/AppointmentManagement.vue'
import RentalApplicationManagement from '../components/landlord/RentalApplicationManagement.vue'
import UserProfile from '../components/user/UserProfile.vue'
import Notifications from '../components/user/Notifications.vue'

export default {
  name: 'LandlordDashboard',
  components: {
    HouseUpload,
    MyHouses,
    AppointmentManagement,
    RentalApplicationManagement,
    Notifications,
    UserProfile
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const tabs = ['upload', 'myHouses', 'appointments', 'applications', 'notifications', 'profile']
    const getRouteTab = () => tabs.includes(route.query.tab) ? route.query.tab : 'upload'
    const activeTab = ref(getRouteTab())

    const handleTabChange = (tabName) => {
      router.replace({ path: route.path, query: { ...route.query, tab: tabName } })
    }

    watch(
      () => route.query.tab,
      () => {
        activeTab.value = getRouteTab()
      }
    )
    
    return {
      activeTab,
      handleTabChange
    }
  }
}
</script>

<style scoped>
.landlord-dashboard {
  padding: 20px;
}
</style>
