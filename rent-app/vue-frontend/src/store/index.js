import { createStore } from 'vuex'

export default createStore({
  state: {
    authToken: localStorage.getItem('authToken') || '',
    currentUser: JSON.parse(localStorage.getItem('currentUser')) || null,
    userRole: localStorage.getItem('userRole') || '',
    houses: [],
    favoriteRefreshKey: 0,
    appointmentRefreshKey: 0,
    rentalApplicationRefreshKey: 0
  },
  mutations: {
    SET_AUTH_TOKEN(state, token) {
      state.authToken = token
      localStorage.setItem('authToken', token)
    },
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
      localStorage.setItem('currentUser', JSON.stringify(user))
    },
    SET_USER_ROLE(state, role) {
      state.userRole = role
      localStorage.setItem('userRole', role)
    },
    CLEAR_AUTH(state) {
      state.authToken = ''
      state.currentUser = null
      state.userRole = ''
      localStorage.removeItem('authToken')
      localStorage.removeItem('currentUser')
      localStorage.removeItem('userRole')
    },
    SET_HOUSES(state, houses) {
      state.houses = houses
    },
    REFRESH_FAVORITES(state) {
      state.favoriteRefreshKey += 1
    },
    REFRESH_APPOINTMENTS(state) {
      state.appointmentRefreshKey += 1
    },
    REFRESH_RENTAL_APPLICATIONS(state) {
      state.rentalApplicationRefreshKey += 1
    }
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('SET_AUTH_TOKEN', token)
      commit('SET_CURRENT_USER', user)
      commit('SET_USER_ROLE', user.role)
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    },
    setHouses({ commit }, houses) {
      commit('SET_HOUSES', houses)
    },
    refreshFavorites({ commit }) {
      commit('REFRESH_FAVORITES')
    },
    refreshAppointments({ commit }) {
      commit('REFRESH_APPOINTMENTS')
    },
    refreshRentalApplications({ commit }) {
      commit('REFRESH_RENTAL_APPLICATIONS')
    }
  },
  getters: {
    isLoggedIn: state => !!state.authToken,
    currentUser: state => state.currentUser,
    userRole: state => state.userRole,
    authToken: state => state.authToken,
    houses: state => state.houses,
    favoriteRefreshKey: state => state.favoriteRefreshKey,
    appointmentRefreshKey: state => state.appointmentRefreshKey,
    rentalApplicationRefreshKey: state => state.rentalApplicationRefreshKey
  }
})
