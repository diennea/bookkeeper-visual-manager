import Vue from "vue";
import Vuex from "vuex";

import auth from "./lib/auth";
import request from "./lib/request";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        drawerExpanded: false,
        token: auth.getSession(),
        status: '',
    },
    mutations: {
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
        isLogged: state => {
            if (!state.token) {
                return false;
            }
            return true;
        },
        authStatus: state => state.status,
    },
    actions: {
        login({ commit }, loginInfo) {
            return new Promise((resolve, reject) => {
                commit('authRequest');
                request.post(auth.LOGIN_ENDPOINT, loginInfo,
                    res => {
                        auth.createSession();
                        commit('authSuccess');
                        resolve(res);
                    },
                    err => {
                        commit('authError');
                        reject(err);
                    }
                );
            })
        },
        logout({ commit }) {
            return new Promise((resolve, reject) => {
                request.post(auth.LOGOUT_ENDPOINT, {},
                    res => {
                        resolve(res);
                    },
                    err => {
                        reject(err);
                    }
                );
                auth.destroySession();
                commit('logout');
                resolve();
            });
        }
    }
});
