import Vue from "vue";

import App from "./App.vue";
import router from "./router";
import store from "./store";
import request from "./lib/request";
import library from "./lib/library";

import vuetify from "./plugins/vuetify";

import "@mdi/font/css/materialdesignicons.css";
import "@/styles/main.scss";

import axios from "axios";
import auth from "./lib/auth";

axios.interceptors.request.use(
    request => {
        return request;
    },
    err => {
        return new Promise(() => {
            throw err;
        });
    }
);

axios.interceptors.response.use(
    response => {
        return response;
    },
    err => {
        return new Promise(() => {
            if (
                // Unauthorized
                err.response.status === 401 &&
                err.response.config &&
                !err.response.config.__isRetryRequest
            ) {
                store.dispatch("logout").finally(() => {
                    router.push("/login");
                });
            } else if (err.response.status > 400) {
                const code = err.response.status;
                router.push({ name: "error", params: { code } });
            }
            throw err;
        });
    }
);

Vue.config.productionTip = false;
Vue.prototype.$request = request;
Vue.prototype.$library = library;

// On refresh check if was logged
if (auth.isLogged()) {
    store.commit('authSuccess', 'dummy');
}

// New instance of Vue with Router and Store
new Vue({
    router,
    store,
    vuetify,
    render: h => h(App)
}).$mount("#app");
