import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    drawerExpanded: false
  },
  mutations: {
    toggleDrawer(state) {
      state.drawerExpanded = !state.drawerExpanded;
    }
  }
})
