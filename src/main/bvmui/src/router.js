import Vue from 'vue'
import Router from 'vue-router'

import ErrorPage from '@/views/ErrorPage'
import BookieLedgers from '@/views/BookieLedgers'
import Bookies from '@/views/Bookies'

Vue.use(Router);

export default new Router({
    mode: 'hash',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'home',
            redirect: "/bookies",
        },
        {
            path: '/404',
            name: 'error',
            component: ErrorPage
        },
        {
            path: '/bookies',
            name: 'bookies',
            component: Bookies,
            meta: {
                title: "Bookies"
            }
        },
        {
            path: '/ledgers/:bookieId',
            name: 'ledgers',
            component: BookieLedgers,
            meta: {
                title: "Ledgers"
            }
        }
    ]
})
