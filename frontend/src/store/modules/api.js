import Vue from 'vue'
import { router } from '@/router'
import { isLoggedIn } from '@/utils'

const state = {
    users: []
}

console.log("API URL", process.env.API_URL);

const actions = {
    send(
        { commit }, 
        { 
            path, 
            data,
            method = 'POST',
            type = 'application/json',
            head = {
                credentials: 'include',
                headers: {
                    'Content-Type': type,
                }
            }
        }
    ) {
        let body = method == 'POST' && JSON.stringify(data) || undefined
        if (method == 'POST' && (type == 'application/x-www-form-urlencoded' || type == 'multipart/form-data'))
            body = data;
        return fetch(
                    `${process.env.API_URL}${path}`,
                    { body, ...head, method }
                ).then(async res => {
                    const json = await res.json()
                    if (!res.ok) {
                        const { error, message } = json

                        if (error === 'INVALID_TOKEN') {
                            if (isLoggedIn()) {
                                localStorage.removeItem('auth_token');
                                Vue.notify({ group: 'main', title: 'Error !', type: 'error', text: message})
                            }
                            router.push({ name: 'Login' })
                        }
                        else Vue.notify({ group: 'main', title: 'Error !', type: 'error', text: message})

                        throw json
                    }

                    return json
                })
                .catch(console.error)
    }
}

const getters = {}

const mutations = {}

export default {
    state,
    actions,
    mutations,
    getters
}
