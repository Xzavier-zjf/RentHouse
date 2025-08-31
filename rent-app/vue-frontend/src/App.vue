<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>极简租房网站</h1>
        <div v-if="isLoggedIn" class="user-info">
          欢迎, {{ currentUser.username }} ({{ currentUser.role }})
          <el-button @click="logout" type="danger" size="small">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { useStore } from 'vuex'
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'App',
  setup() {
    const store = useStore()
    const router = useRouter()
    
    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    const currentUser = computed(() => store.getters.currentUser)
    
    // 页面加载时同步localStorage到store
    onMounted(() => {
      const token = localStorage.getItem('authToken')
      const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
      const role = localStorage.getItem('userRole')
      
      if (token && user && role) {
        store.commit('SET_AUTH_TOKEN', token)
        store.commit('SET_CURRENT_USER', user)
        store.commit('SET_USER_ROLE', role)
      }
    })
    
    const logout = () => {
      store.dispatch('logout')
      router.push('/login')
    }
    
    return {
      isLoggedIn,
      currentUser,
      logout
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.el-header {
  background-color: #409EFF;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 添加过渡动画样式 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>