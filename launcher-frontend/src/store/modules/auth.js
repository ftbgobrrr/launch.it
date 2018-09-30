import { store as vuex } from '@/store'
import { router } from '@/router'
import Vue from 'vue'

const onLoad = (store) => {
    
}

const state = {
    me: null
}

const actions = {
    login(_, { login, pass }) {
        vuex.dispatch('api/send', {
            path: 'auth/login',
            data: { pass, login }
        }).then(({ token }) => {
            localStorage.setItem('auth_token', token)
            router.push({ name: 'Home' })
        })
    },
    updateMe({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'auth/me',
            data: {},
            method: 'GET'
        }).then(me => {
            if (me)
                commit('set_me', me)
            return me;
        })
    },
    changePass({}, { pass }) {
        return vuex.dispatch('api/send', {
            path: 'auth/me/pass',
            data: { pass }
        }).then(({ id }) => {
            if (id)
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'Password has been changed'})
            return id;
        })
    },
    logout({ commit }) {
        vuex.dispatch('api/send', {
            path: 'auth/logout',
            data: {},
            method: 'GET'
        }).then(() => {
            localStorage.removeItem('auth_token');
            router.push({ name: 'Login' })
            commit('set_me', null);
        })
    }
}

const mutations = {
    set_me(state, me) {
        state.me = me;
    }
}

const getters = {
    me: (state, getters) => {
        return state.me || {}
    }
}

export default {
    state,
    actions,
    mutations,
    getters
}