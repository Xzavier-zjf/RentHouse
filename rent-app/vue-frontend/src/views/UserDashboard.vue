<template>
  <div class="user-dashboard">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="房源搜索" name="search">
        <HouseSearch />
      </el-tab-pane>
      <el-tab-pane label="我的收藏" name="favorites">
        <FavoriteHouses />
      </el-tab-pane>
      <el-tab-pane label="我的预约" name="appointments">
        <MyAppointments />
      </el-tab-pane>
      <el-tab-pane label="我的申请" name="applications">
        <MyRentalApplications />
      </el-tab-pane>
      <el-tab-pane label="站内通知" name="notifications">
        <Notifications />
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
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HouseSearch from '../components/user/HouseSearch.vue'
import ApplyLandlord from '../components/user/ApplyLandlord.vue'
import UserProfile from '../components/user/UserProfile.vue'
import FavoriteHouses from '../components/user/FavoriteHouses.vue'
import MyAppointments from '../components/user/MyAppointments.vue'
import MyRentalApplications from '../components/user/MyRentalApplications.vue'
import Notifications from '../components/user/Notifications.vue'

export default {
  name: 'UserDashboard',
  components: {
    HouseSearch,
    ApplyLandlord,
    UserProfile,
    FavoriteHouses,
    MyAppointments,
    MyRentalApplications,
    Notifications
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const tabs = ['search', 'favorites', 'appointments', 'applications', 'notifications', 'apply', 'profile']
    const getRouteTab = () => tabs.includes(route.query.tab) ? route.query.tab : 'search'
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
.user-dashboard {
  padding: 20px;
}
</style>
