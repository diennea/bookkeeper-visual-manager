import Vue from "vue";
import Vuex from "vuex";

import auth from "./lib/auth";
import request from "./lib/request";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        drawerExpanded: false,
        showDrawer: false,
        token: '',
        status: '',
        clusterCount: null
    },
    mutations: {
        updateClusterCount(state, count) {
            state.clusterCount = count;
        },
        incrementClusterCount(state) {
            state.clusterCount++;
        },
        decrementClusterCount(state) {
            state.clusterCount--;
        },
        showDrawer(state, show) {
            state.showDrawer = show;
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
        },
        clusterCount({ commit, state }) {
            return new Promise((resolve, reject) => {
                if (state.clusterCount === null) {
                    request.get("api/cluster/count")
                        .then(res => {
                            commit('updateClusterCount', res);
                            resolve(res);
                        })
                        .catch(err => {
                            commit('updateClusterCount', null);
                            reject(err);
                        });
                } else {
                    resolve(state.clusterCount);
                }
            });
        }
    }
});
