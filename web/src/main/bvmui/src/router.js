import Vue from 'vue'
import Router from 'vue-router'

import ErrorPage from "@/views/ErrorPage";
import Bookies from '@/views/Bookies'
import Ledgers from '@/views/Ledgers'
import Clusters from '@/views/Clusters'
import Login from '@/views/Login'
import SystemStatus from '@/views/SystemStatus'

import store from './store'

Vue.use(Router);

const router = new Router({
    mode: 'hash',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'home',
            redirect: '/bookies'
        },
        {
            path: '/error/:code',
            name: 'error',
            component: ErrorPage,
            meta: {
                title: "Error"
            }
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
            path: '/systemstatus',
            name: 'systemstatus',
            component: SystemStatus,
            meta: {
                title: "System status",
                type: "all"
            }
        },
        {
            path: '/clusters',
            name: 'clusters',
            component: Clusters,
            meta: {
                title: "Clusters"
            }
        },
        {
            path: '/ledgers/:clusterId/:bookieId',
            name: 'bookie-ledgers',
            component: Ledgers,
            meta: {
                title: "Bookie Ledgers: ${bookieId}",
                type: "bookie"
            }
        },
        {
            path: "*",
            redirect: "/error",
        }
    ]
})

router.beforeEach(async (to, from, next) => {
    const pageLogin = to.name === 'login';
    const userLogged = store.getters.isLogged;
    if (pageLogin && userLogged) {
        next("/");
        return;
    }
    if (pageLogin) {
        next();
        return;
    }
    if (userLogged) {
        const pageClusters = to.name === 'clusters';
        if (pageClusters) {
            next();
            return;
        }

        const count = await store.dispatch('clusterCount');
        store.commit('showDrawer', count > 0);
        if (count === 0) {
            next('/clusters');
            return;
        }

        next();
        return;
    }
    next("/login");
});

export default router;
