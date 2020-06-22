import Vue from "vue";
import Vuex from "vuex";

import auth from "./lib/auth";
import request from "./lib/request";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        clusterName: '',
        drawerExpanded: false,
        token: '',
        status: '',
    },
    mutations: {
        updateCluster(state, payload) {
            state.clusterName = payload.name
        },
        toggleDrawer(state) {
            state.drawerExpanded = !state.drawerExpanded;
        },
        authRequest(state) {
            state.status = 'loading';
        },
        authSuccess(state, token) {
            state.status = 'success';
            state.token = token;
        },
        authError(state) {
            state.status = 'error';
        },
        logout(state) {
            state.status = '';
            state.token = '';
        },
    },
    getters: {
        isLogged: state => !state.token ? false : true,
        clusterName: state => state.clusterName
    },
    actions: {
        login({ commit }, loginInfo) {
            return new Promise((resolve, reject) => {
                commit('authRequest');
                request.post(auth.LOGIN_ENDPOINT, loginInfo)
                    .then(res => {
                        auth.createSession('dummy');
                        commit('authSuccess', 'dummy');
                        resolve(res);
                    })
                    .catch(err => {
                        commit('authError');
                        reject(err);
                    });
            })
        },
        logout({ commit }) {
            return new Promise((resolve, reject) => {
                request.post(auth.LOGOUT_ENDPOINT)
                    .then(res => {
                        resolve(res);
                    })
                    .catch(err => {
                        reject(err);
                    })
                    .finally(() => {
                        auth.destroySession();
                        commit('logout');
                        resolve();
                    });
            });
        }
    }
});
