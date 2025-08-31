import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserDashboard from '../views/UserDashboard.vue'
import LandlordDashboard from '../views/LandlordDashboard.vue'
import AdminDashboard from '../views/AdminDashboard.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/user',
    name: 'UserDashboard',
    component: UserDashboard,
    meta: { requiresAuth: true, role: 'USER' }
  },
  {
    path: '/landlord',
    name: 'LandlordDashboard',
    component: LandlordDashboard,
    meta: { requiresAuth: true, role: 'LANDLORD' }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫检查:', { to: to.path, from: from.path })
  
  const isAuthenticated = localStorage.getItem('authToken')
  const userRole = localStorage.getItem('userRole')
  
  console.log('认证状态:', { isAuthenticated: !!isAuthenticated, userRole })
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    console.log('需要认证但未登录，跳转到登录页')
    next('/login')
  } else if (to.path === '/login' && isAuthenticated) {
    // 如果已登录且访问登录页，根据角色重定向到对应仪表板
    console.log('已登录用户访问登录页，重定向到仪表板')
    switch(userRole) {
      case 'USER':
        next('/user')
        break
      case 'LANDLORD':
        next('/landlord')
        break
      case 'ADMIN':
        next('/admin')
        break
      default:
        console.log('未知角色，跳转到首页')
        next('/')
    }
  } else if (to.meta.role && isAuthenticated && to.meta.role !== userRole) {
    // 根据用户角色重定向到正确的仪表板
    console.log('角色不匹配，重定向到正确的仪表板')
    switch(userRole) {
      case 'USER':
        next('/user')
        break
      case 'LANDLORD':
        next('/landlord')
        break
      case 'ADMIN':
        next('/admin')
        break
      default:
        console.log('未知角色，跳转到登录页')
        next('/login')
    }
  } else {
    console.log('路由守卫通过，继续导航')
    next()
  }
})

export default router