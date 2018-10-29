import { store as vuex } from '@/store'
import Vue from 'vue'

const state = {
    news: [],
};

const actions = {
    updateNews({ commit }) {
        return vuex.dispatch('api/send', {
            path: 'news',
            data: {},
            method: 'GET'
        }).then(news => {
            if (news) {
                commit('set_news', news)
            }
            return news
        })
    },
    addNews({ commit }, { message }) {
        if (!message || message == '') {
            Vue.notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty message'})
            return null
        }

        return vuex.dispatch('api/send', {
            path: 'news/add',
            data: { message }
        }).then(news => {
            if (news) {
                commit('set_news', [news, ...state.news])
                Vue.notify({ group: 'main', title: "Success !", type: 'success', text: 'News has been added'})
            }
            return news;
        })
    },
    delNews({ commit }, { id }) {
        return vuex.dispatch('api/send', {
            path: 'news/del',
            data: { id }
        }).then(news => {
            if (news && news.id) {
                commit('del_news', news.id)
                Vue.notify({ group: 'main', title: "Warning !", type: 'warn', text: 'News has been removed'})
            }
            return news && news.id || null
        })
    }
};

const getters = {
    news: (state) => {
        return state.news
    }
}

const mutations = {
    set_news(state, news) {
        state.news = news
    },
    del_news(state, newsId) {
        state.news.forEach(({ id }, key) => {
            if (id == newsId) {
                state.news.splice(key, 1)
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