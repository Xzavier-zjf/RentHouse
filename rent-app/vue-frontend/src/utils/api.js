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
  getAllUsers(params = {}) {
    return api.get('/api/user/admin/users', { params })
  },
  
  // 管理员批准房东申请
  approveLandlord(userId) {
    return api.post(`/api/user/admin/approve-landlord?userId=${userId}`)
  },

  rejectLandlord(userId, reason) {
    return api.post('/api/user/admin/reject-landlord', null, { params: { userId, reason } })
  },

  getNotifications(params = {}) {
    return api.get('/api/user/notifications', { params })
  },

  markNotificationRead(id) {
    return api.post(`/api/user/notifications/${id}/read`)
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
  searchHouses(params = {}) {
    return api.get('/api/house/search', { params })
  },

  // 房源详情
  getHouseDetail(id) {
    return api.get(`/api/house/${id}`)
  },
  
  // 上传房源
  uploadHouse(houseData) {
    return api.post('/api/house/upload', houseData)
  },
  
  // 获取我的房源
  getMyHouses(params = {}) {
    return api.get('/api/house/my-houses', { params })
  },

  // 编辑房源
  updateHouse(id, houseData) {
    return api.put(`/api/house/${id}`, houseData)
  },

  // 下架房源
  offlineHouse(id) {
    return api.post(`/api/house/${id}/offline`)
  },
  
  // 管理员获取所有房源
  getAllHouses(status, params = {}) {
    return api.get('/api/house/admin/houses', { params: { ...params, ...(status ? { status } : {}) } })
  },
  
  // 管理员审核房源
  approveHouse(id, status, reason) {
    return api.post('/api/house/admin/approve-house', null, { params: { id, status, reason } })
  },

  addFavorite(id) {
    return api.post(`/api/house/${id}/favorite`)
  },

  removeFavorite(id) {
    return api.delete(`/api/house/${id}/favorite`)
  },

  isFavorite(id) {
    return api.get(`/api/house/${id}/favorite`)
  },

  getFavorites() {
    return api.get('/api/house/favorites')
  },

  createAppointment(data) {
    return api.post('/api/house/appointments', data)
  },

  getMyAppointments(params = {}) {
    return api.get('/api/house/appointments/my', { params })
  },

  getLandlordAppointments(params = {}) {
    return api.get('/api/house/appointments/landlord', { params })
  },

  updateAppointmentStatus(id, status, reply) {
    return api.post(`/api/house/appointments/${id}/status`, null, { params: { status, reply } })
  },

  cancelAppointment(id) {
    return api.post(`/api/house/appointments/${id}/cancel`)
  },

  createRentalApplication(data) {
    return api.post('/api/house/rental-applications', data)
  },

  getMyRentalApplications(params = {}) {
    return api.get('/api/house/rental-applications/my', { params })
  },

  getLandlordRentalApplications(params = {}) {
    return api.get('/api/house/rental-applications/landlord', { params })
  },

  getAllRentalApplications(params = {}) {
    return api.get('/api/house/admin/rental-applications', { params })
  },

  updateRentalApplicationStatus(id, status, reply) {
    return api.post(`/api/house/rental-applications/${id}/status`, null, { params: { status, reply } })
  },

  cancelRentalApplication(id) {
    return api.post(`/api/house/rental-applications/${id}/cancel`)
  },

  getStatistics() {
    return api.get('/api/house/admin/statistics')
  }
}

export default api
