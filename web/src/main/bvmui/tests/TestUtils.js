import { mount as mountVue, createLocalVue } from '@vue/test-utils';
import axios from "axios";
import MockAdapter from "axios-mock-adapter";
import request from "@/lib/request";
import library from "@/lib/library";
import store from '@/store'
import router from '@/router'
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import Vue from 'vue';

Vue.use(Vuetify);
const axiosMockAdapter = new MockAdapter(axios);

export function mount(component, options = {}) {
    const localVue = createLocalVue();
    localVue.use(Vuex);
    localVue.use(Vuetify);

    store.replaceState({
        drawerExpanded: true,
        showDrawer: true,
        clusterCount: 1
    });

    const componentInstance = mountVue(component, {
        store,
        vuetify: new Vuetify(),
        localVue,
        router,
        mocks: {
            $library: library,
            $request: request
        },
        ...options
    });
    return componentInstance;
}

export function createGetResponse(url, response, code = 200) {
    axiosMockAdapter.onGet(url).reply(code, response);
}

export function flushPromises() {
    const scheduler = typeof setImmediate === 'function' ? setImmediate : setTimeout;
    return new Promise(function (resolve) {
        scheduler(resolve);
    })
}
