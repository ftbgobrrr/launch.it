import { store as vuex } from '@/store'
import Vue from 'vue'

const state = {
    packs: [],
};

const actions = {
    updatePacks({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'packs',
            data: {},
            method: 'GET'
        }).then(packs => {
            console.log("PACKS --- > ", packs)
            if (packs) {
                commit('set_packs', packs)
            }
            return packs
        })
    },
    updatePack({ commit }, { id }) {
        return vuex.dispatch('api/send', {
            path: `packs/pack`,
            data: { id },
        }).then(pack => {
            console.log("GET FULL PACK --- > ", pack)
            if (pack) {
                commit('set_pack', pack)
            }
            return pack
        })
    },
    addPack({ commit, state }, { name, preset }) {
        if (!name || name == '') {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty pack name'})
            return null
        }

        if (!preset) {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Preset not set'})
            return null
        }

        return vuex.dispatch('api/send', {
            path: 'packs/add',
            data: { name, preset }
        }).then((pack) => {
            if (pack) {
                commit('set_packs', [pack, ...state.packs])
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'Pack has been added'})
            }
            return pack;
        })
    },
    delPack({ commit }, { id }) {
        return vuex.dispatch('api/send', {
            path: 'packs/del',
            data: { id }
        }).then(({ id }) => {
            if (id) {
                commit('del_pack', id)
                Vue.notify({ group: 'main', title: "Warning !", type: 'warn', text: 'Pack has been removed'})
            }
        })
    },
    editPack({ commit }, { id, name }) {
        if (!name || name == '') {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty pack name'})
            return null
        }

        return vuex.dispatch('api/send', {
            path: 'packs/edit',
            data: { id, name }
        }).then(user => {
            if (user && user.id) {
                commit('set_pack', { id, name })
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'Pack has been edited'})
            }
            return user;
        })
    },
    upload({ }, { id, type, pkg, name, version, file }) {
        const data = new FormData();
        data.append('pack', id);
        data.append('type', type);
        data.append('name', `${pkg}:${name}:${version}`);
        data.append('file', file);

        return vuex.dispatch('api/send', {
            path: 'packs/pack/upload',
            method: 'POST',
            type: 'multipart/form-data',
            data,
        }).then(console.log)
    }
};

const getters = {
    packs: (state) => {
        return state.packs
    }
}

const mutations = {
    set_packs(state, packs) {
        state.packs = packs
    },
    set_pack(state, { id: userId, ...fields }) {
        state.packs.forEach(({ id, ...user }, key) => {
            if (id == userId) {
                Vue.set(state.packs, key, { ...user, id: userId, ...fields })
            }
        });
    },
    del_pack(state, packId) {
        state.packs.forEach(({ id }, key) => {
            if (id == packId) {
                state.packs.splice(key, 1)
                return
            }
        })
    },
}

export default {
    state,
    actions,
    mutations,
    getters
}
