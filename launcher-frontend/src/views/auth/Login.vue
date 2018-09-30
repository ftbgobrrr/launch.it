<template>
   <v-flex xs12 sm5 md4>
        <v-card ref="form">
            <v-card-text>
                <h1 class="text-xs-center">Sign Up</h1>
                <v-text-field
                    label="Login"
                    v-model="login"
                ></v-text-field>
                
                <v-text-field
                    label="Password"
                    v-model="pass"
                    type="password"
                ></v-text-field>
            </v-card-text>
            <v-divider class="mt-5"></v-divider>
            <v-card-actions>
                <v-layout justify-end>
                    <v-btn class="primary" @click.stop="send">
                        Sign Up
                    </v-btn>
                </v-layout>
            </v-card-actions>
        </v-card>
    </v-flex>
</template>
    
<script>
import { mapActions } from 'vuex'

export default {
    name: 'login',
    data() {
        return {
            login: null, 
            pass: null
        }
    },
    methods: {
        ...mapActions({
            doLogin: 'auth/login'
        }),
        send() {
            this.doLogin({ login: this.login, pass: this.pass })
        }
    }
}
</script>
