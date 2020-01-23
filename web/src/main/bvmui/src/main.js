import Vue from 'vue'

import App from './App.vue'
import router from './router'
import store from './store'
import request from './lib/request'
import library from './lib/library'

import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'
import vuetify from './plugins/vuetify';

import '@mdi/font/css/materialdesignicons.css';
import "@/styles/main.scss";

import axios from "axios";
import auth from './lib/auth'

Vue.config.productionTip = false
Vue.prototype.$request = request;
Vue.prototype.$library = library;

// New instance of Vue with Router and Store
new Vue({
    router,
    store,
    vuetify,
    render: h => h(App)
}).$mount('#app')
