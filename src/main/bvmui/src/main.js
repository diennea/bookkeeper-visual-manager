import Vue from 'vue'

import App from './App.vue'
import router from './router'
import store from './store'
import request from './lib/request'

import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

Vue.config.productionTip = false
Vue.prototype.$request = request;

// Requiring all components in the /components folder
const requireComponent = require.context(
    './components', false, /[A-Z]\w+\.(vue|js)$/
)

// Importing globally each one of them
requireComponent.keys().forEach(fileName => {
    const componentConfig = requireComponent(fileName)
    const componentName = upperFirst(
        camelCase(
            fileName.split('/').pop().replace(/\.\w+$/, '')
        )
    )
    Vue.component(
        componentName,
        componentConfig.default || componentConfig
    )
})

// New instance of Vue with Router and Store
new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')
