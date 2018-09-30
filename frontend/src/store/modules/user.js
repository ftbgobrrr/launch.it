import { store as vuex } from '@/store'
import Vue from 'vue'

const state = {
    users: [],
    groups: [],
};

const actions = {
    updateUsers({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'users',
            data: {},
            method: 'GET'
        }).then(users => {
            console.log(users)
            if (users) {
                commit('set_users', users)
            }
            return users
        })
    },
    addUser({ commit }, { login, pass, group }) {
        if (!login || login == '') {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty user name'})
            return null
        }

        return vuex.dispatch('api/send', {
            path: 'users/addUser',
            data: { login, pass, group }
        }).then(user => {
            if (user) {
                commit('set_users', [user, ...state.users])
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'User has been added'})
            }
            return user;
        })
    },
    delUser({ commit }, { id }) {
        return vuex.dispatch('api/send', {
            path: 'users/delUser',
            data: { id }
        }).then(user => {
            if (user && user.id) {
                commit('del_user', user.id)
                Vue.notify({ group: 'main', title: "Warning !", type: 'warn', text: 'User has been removed'})
            }
            return user && user.id || null
        })
    },
    editUser({ commit }, { id, login, group }) {
        if (!login || login == '') {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty user name'})
            return null
        }
        return vuex.dispatch('api/send', {
            path: 'users/editUser',
            data: { id, login, group }
        }).then(user => {
            if (user && user.id) {
                commit('set_user', { id, login, group })
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'User has been edited'})
            }
            return user;
        })
    },
    updateGroups({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'users/groups',
            data: {},
            method: 'GET'
        }).then(groups => {
            if (groups) {
                commit('set_groups', groups)
            }
            return groups
        })
    }
};

const getters = {
    users: (state) => {
        return state.users
    },
    groups: (state) => {
        return state.groups
    },
    hasLevel: (state) => (group) => {
        if (!group)
                return true
        const me = vuex.getters['auth/me']
        if (!me || !me.group || state.groups.length == 0)
            return false
        return state.groups.find(({ group: g }) => g == me.group).level >= state.groups.find(({ group: g }) => g == group).level
    }
}

const mutations = {
    set_users(state, users) {
        state.users = users
    },
    set_groups(state, groups) {
        state.groups = groups
    },
    set_user(state, { id: userId, ...fields }) {
        state.users.forEach(({ id }, key) => {
            if (id == userId) {
                Vue.set(state.users, key, { id: userId, ...fields })
            }
        });
    },
    del_user(state, userId) {
        state.users.forEach(({ id }, key) => {
            if (id == userId) {
                state.users.splice(key, 1)
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