import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    drawerExpanded: false,
    logged: false
  },
  mutations: {
    toggleDrawer(state) {
      state.drawerExpanded = !state.drawerExpanded;
    }, loggedOut(state) {
        state.logged = false;
    }, loggedIn(state) {
        state.logged = true;
    }
  }
})
