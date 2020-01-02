import Vue from 'vue'
import Router from 'vue-router'

import ErrorPage from '@/views/ErrorPage'
import Bookies from '@/views/Bookies'
import Ledgers from '@/views/Ledgers'
import Login from '@/views/Login'
import Cache from '@/views/Cache'

Vue.use(Router);

export default new Router({
    mode: 'hash',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'home',
            redirect: "/login",
        },
        {
            path: '/error',
            name: 'error',
            component: ErrorPage
        },
        {
            path: '/login',
            name: 'login',
            component: Login
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
                title: "Ledgers",
                type: "all"
            }
        },
        {
            path: '/cache',
            name: 'cache',
            component: Cache,
            meta: {
                title: "Cache",
                type: "all"
            }
        },
        {
            path: '/ledgers/:bookieId',
            name: 'bookie-ledgers',
            component: Ledgers,
            meta: {
                title: "Bookie Ledgers: ${bookieId}",
                type: "bookie"
            }
        },
        {
            path: "*",
            redirect: "/error"
        }
    ]
})
