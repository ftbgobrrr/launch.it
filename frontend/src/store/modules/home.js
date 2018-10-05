import { store as vuex } from '@/store'
import Vue from 'vue'

const state = {
    presets: [],
};

const actions = {
    uploadLauncher({ }, { type, arch, file }) {
        const data = new FormData();
        data.append('type', type);
        data.append('arch', arch);
        data.append('file', file);
        return vuex.dispatch('api/send', {
            path: 'uploadLauncher',
            method: 'POST',
            type: 'application/x-www-form-urlencoded',
            head: {
                credentials: 'include',
            },
            data
        }).then(file => {
            if (file) {
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'File has been uploaded'})
            }
            return file
        })
    }
};

const getters = {}

const mutations = {}

export default {
    state,
    actions,
    mutations,
    getters
}