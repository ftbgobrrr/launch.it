<template>
    <div>
        <v-navigation-drawer
            persistent
            v-model="drawer"
            enable-resize-watcher
            fixed
            clipped
            app
        >
        <v-list>
            <v-list-tile
            v-for="(item, i) in routes.filter(({ hide, group }) => !hide && hasLevel(group))"
            :key="i"
            :to="item.path"
            >
                <v-list-tile-action>
                    <v-icon v-html="item.icon"></v-icon>
                </v-list-tile-action>
                <v-list-tile-content>
                    <v-list-tile-title v-text="item.dName"></v-list-tile-title>
                </v-list-tile-content>
            </v-list-tile>
        </v-list>
        </v-navigation-drawer>
        <v-toolbar app clipped-left>
            <v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
            <v-toolbar-title>Panel</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-menu :nudge-width="50">
                <v-toolbar-title slot="activator">
                    <span>{{ me.login }}</span>
                    <v-icon dark>arrow_drop_down</v-icon>
                </v-toolbar-title>

                <v-list dark>
                    <v-list-tile>
                        <v-list-tile-title @click="logout">Logout</v-list-tile-title>
                    </v-list-tile>
                    
                        <v-list-tile>
                            <v-dialog
                                v-model="editpassword.value"
                                width="500"
                            >
                                <v-list-tile-title  slot="activator">Change password</v-list-tile-title>
                                <v-card>
                                    <v-card-title>
                                        <span class="headline">Change Password</span>
                                    </v-card-title>

                                    <v-card-text>
                                        <v-container grid-list-md>
                                        <v-layout wrap>
                                            <v-flex xs12>
                                                <v-text-field type="password" v-model="editpassword.pass" label="Password"></v-text-field>
                                            </v-flex>
                                            <v-flex xs12>
                                                <v-text-field type="password" v-model="editpassword.confirm" label="Confirm"></v-text-field>
                                            </v-flex>
                                        </v-layout>
                                        </v-container>
                                    </v-card-text>

                                    <v-card-actions>
                                        <v-spacer></v-spacer>
                                        <v-btn color="blue darken-1" flat @click.native="editpassword.value = false">Cancel</v-btn>
                                        <v-btn color="blue darken-1" @click.native="savePassword">Save</v-btn>
                                    </v-card-actions>
                                </v-card>
                            </v-dialog>
                        </v-list-tile>
                   
                </v-list>
            </v-menu>
        </v-toolbar>
         <v-content>
            <v-slide-y-transition mode="out-in">
                <router-view></router-view>
            </v-slide-y-transition>
         </v-content>     
    </div>
</template>

<script>
import { routes } from '@/router'
import { mapActions, mapGetters } from 'vuex';
export default {
    name: 'layout',
    data() {
        return {
            routes,
            editpassword: {
                value: false,
                pass: null,
                confirm: null
            },
            drawer: true
        }
    },
    computed: {
        ...mapGetters({
            me: 'auth/me',
            groups: 'user/groups'
        })
    },
    methods: {
        ...mapActions({
            updateMe: 'auth/updateMe',
            updateGroups: 'user/updateGroups',
            logout: 'auth/logout',
            changePass: 'auth/changePass'
        }),
        savePassword() {
            const { pass, confirm } = this.editpassword;
            if (!pass || !confirm || confirm == '' || pass == '')
                return this.$notify({ group: 'main', title: 'Error !', type: 'error', text: 'Password empty'})
            if (pass !== confirm)
                return this.$notify({ group: 'main', title: 'Error !', type: 'error', text: 'Password and Confirm are not identical!'})
            this.changePass({ pass }).then((res) => {
                this.editpassword.value = !res;
            })
        },
        hasLevel(group) {
            return this.$store.getters['user/hasLevel'](group);
        }
    },
    mounted() {
        this.updateMe(),
        this.updateGroups()
    }
}
</script>
