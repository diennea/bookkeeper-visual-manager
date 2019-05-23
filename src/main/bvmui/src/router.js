import Vue from 'vue'
import Router from 'vue-router'

import ErrorPage from '@/views/ErrorPage'
import Bookies from '@/views/Bookies'
import Ledgers from '@/views/Ledgers'
import BookieLedgers from '@/views/BookieLedgers'

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
            path: '/ledgers',
            name: 'ledgers',
            component: Ledgers,
            meta: {
                title: "Ledgers"
            }
        },
        {
            path: '/ledgers/:bookieId',
            name: 'bookie-ledgers',
            component: BookieLedgers,
            meta: {
                title: "Bookie Ledgers: ${bookieId}"
            }
        }
    ]
})
