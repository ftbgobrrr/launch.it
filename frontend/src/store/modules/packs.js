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
    defaultPack({ commit }, { id }) {
        return vuex.dispatch('api/send', {
            path: `packs/default`,
            data: { id },
        }).then(packs => {
            if (packs) {
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'Pack has been set by default'})
                commit('set_packs', packs)
            }
            return packs
        })
    },
    settingsPack({ commit }, { id, mainClass, args }) {
        return vuex.dispatch('api/send', {
            path: `packs/pack/settings`,
            data: { id, mainClass, args },
        }).then(pack => {
            if (pack) {
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'Settings updated!'})
                commit('set_pack', pack)
            }
            return pack
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
    upload({ commit }, { id, type, pkg, name, version, file, folder }) {
        const data = new FormData();
        data.append('pack', id);
        data.append('type', type);
        data.append('name', type === 'library' ? `${pkg}:${name}:${version}` : `${folder}:${name}`);
        data.append('file', file);

        return vuex.dispatch('api/send', {
            path: 'packs/pack/upload',
            method: 'POST',
            type: 'application/x-www-form-urlencoded',
            head: {
                credentials: 'include',
            },
            data,
        }).then((artifact) => {
            if (artifact) {
                commit('add_file', { id, type, artifact })
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'File has been edited'})
            }
            return artifact;
        })
    },
    delFile({ commit }, { id, type, name }) {
        return vuex.dispatch('api/send', {
            path: 'packs/pack/del',
            data: { id, type, name }
        }).then((file) => {
            if (file) {
                commit('del_file', { id, type, name})
                Vue.notify({ group: 'main', title: "Warning !", type: 'warn', text: 'File has been deleted'})
            }
            return file;
        })
    },
    desableFile({ commit }, { id, type, name, desable }) {
        return vuex.dispatch('api/send', {
            path: 'packs/pack/desable',
            data: { id, type, name, desable }
        }).then((file) => {
            if (file) {
                commit('desable_file', { id, type, name, desable })
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: `File has been ${desable ? 'desabled': 'enabled'}`})
            }
            return file;
        })
    },
    build({}, { id }) {
        return vuex.dispatch('api/send', {
            path: 'packs/pack/build',
            data: { id }
        }).then((file) => {
            if (file) {
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: `Pack has been build and release`})
            }
            return file;
        })
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
    set_pack(state, { id: packId, ...fields }) {
        if (state.packs.find(({ id }) => id == packId)) {
            state.packs.forEach(({ id, ...pack }, key) => {
                if (id == packId)
                    Vue.set(state.packs, key, { ...pack, id: packId, ...fields })
            });
        } else state.packs.push({ id: packId, ...fields });
    },
    add_file(state, { id: packid, type, artifact }) {
        const pack = state.packs.find(({ id }) => id == packid);
        if (type == 'library')
        {
            if(!pack.data.libraries)
                pack.data.libraries = [];
            pack.data.libraries.push(artifact);
        }
        else if (type == 'file') {
            if(!pack.files)
                pack.files = [];
            pack.files.push(artifact);
        }
    },
    desable_file(state, { id: packid, type, name, desable }) {
        const pack = state.packs.find(({ id }) => id == packid);
        if (!pack.desabled)
            Vue.set(pack, 'desabled', []);

        if (desable == true)
            pack.desabled.push({ name, type })
        else {
            pack.desabled.forEach(({ name: n, type: t }, key) => {
                if (n == name && type == t) {
                    pack.desabled.splice(key, 1)
                    return
                }
            });
        }
    },
    del_file(state, { id: packId, type, name }) {
        const pack = state.packs.find(({ id }) => id == packId);
        if (type == 'library')
        {
            pack.data.libraries.forEach(({ name: n }, key) => {
                if (n == name) {
                    pack.data.libraries.splice(key, 1)
                    return
                }
            });
        }
        else if (type == 'file') {
            pack.files.forEach(({ name: n }, key) => {
                if (n == name) {
                    pack.files.splice(key, 1)
                    return
                }
            });
        }
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
