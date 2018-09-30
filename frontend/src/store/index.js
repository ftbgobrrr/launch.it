import Vue from 'vue'
import Vuex from 'vuex'
import modules from './modules'

Vue.use(Vuex)

export const store = new Vuex.Store({
    modules,
    strict: process.env.NODE_ENV !== 'production'
});

Object.keys(modules).forEach(key => {
    modules[key].onLoad && modules[key].onLoad(store)
    console.log('loaded module :', key, modules[key]);
});
