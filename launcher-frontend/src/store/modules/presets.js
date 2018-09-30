import { store as vuex } from '@/store'
import Vue from 'vue'

const state = {
    presets: [],
};

const actions = {
    updatePresets({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'presets',
            data: {},
            method: 'GET'
        }).then(presets => {
            if (presets) {
                commit('set_presets', presets)
            }
            return presets
        })
    }
};

const getters = {
    presets: (state) => {
        return state.presets
    }
}

const mutations = {
    set_presets(state, presets) {
        state.presets = presets
    }
}

export default {
    state,
    actions,
    mutations,
    getters
}