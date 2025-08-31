import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 令牌过期或无效，清除本地存储并重定向到登录页
      localStorage.removeItem('authToken')
      localStorage.removeItem('currentUser')
      localStorage.removeItem('userRole')
      
      // 只有在当前不在登录页时才跳转
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// 用户相关API
export const userAPI = {
  // 用户注册
  register(userData) {
    return api.post('/api/user/register', userData)
  },
  
  // 用户登录
  login(credentials) {
    return api.post('/api/user/login', credentials)
  },
  
  // 申请成为房东
  applyLandlord() {
    return api.post('/api/user/apply-landlord')
  },
  
  // 管理员获取所有用户
  getAllUsers() {
    return api.get('/api/user/admin/users')
  },
  
  // 管理员批准房东申请
  approveLandlord(userId) {
    return api.post(`/api/user/admin/approve-landlord?userId=${userId}`)
  },
  
  // 获取个人信息
  getProfile() {
    return api.get('/api/user/profile')
  },
  
  // 更新个人信息
  updateProfile(profileData) {
    return api.put('/api/user/profile', profileData)
  }
}

// 房源相关API
export const houseAPI = {
  // 搜索房源
  searchHouses(location) {
    return api.get(`/api/house/search?location=${encodeURIComponent(location)}`)
  },
  
  // 上传房源
  uploadHouse(houseData) {
    return api.post('/api/house/upload', houseData)
  },
  
  // 获取我的房源
  getMyHouses() {
    return api.get('/api/house/my-houses')
  },
  
  // 管理员获取所有房源
  getAllHouses() {
    return api.get('/api/house/admin/houses')
  },
  
  // 管理员审核房源
  approveHouse(id, status, reason) {
    return api.post(`/api/house/admin/approve-house?id=${id}&status=${status}&reason=${encodeURIComponent(reason)}`)
  }
}

export default api