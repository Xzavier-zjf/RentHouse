import { createStore } from 'vuex'

export default createStore({
  state: {
    authToken: localStorage.getItem('authToken') || '',
    currentUser: JSON.parse(localStorage.getItem('currentUser')) || null,
    userRole: localStorage.getItem('userRole') || '',
    houses: []
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
    }
  },
  getters: {
    isLoggedIn: state => !!state.authToken,
    currentUser: state => state.currentUser,
    userRole: state => state.userRole,
    authToken: state => state.authToken,
    houses: state => state.houses
  }
})