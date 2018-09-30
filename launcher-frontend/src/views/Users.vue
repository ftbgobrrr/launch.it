<template>
    <v-container>
        <v-toolbar flat>
            <v-toolbar-title>Users</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-btn color="primary" dark class="mb-2" @click="addItem">Add User</v-btn>
            <v-dialog v-model="dialog.value" max-width="500px">
                <v-card>
                    <v-card-title>
                        <span class="headline">{{ dialog.title }}</span>
                    </v-card-title>

                    <v-card-text>
                        <v-container grid-list-md>
                        <v-layout wrap>
                            <v-flex xs12>
                                <v-text-field v-model="dialog.user.login" label="Login"></v-text-field>
                            </v-flex>
                            <v-flex xs12>
                                <v-select :items="groups.map(({ group }) => group)" v-model="dialog.user.group" label="Group"></v-select>
                            </v-flex>
                            <v-flex xs12 v-if="dialog.type == 'ADD'">
                                <v-text-field 
                                    v-model="dialog.user.pass" 
                                    type="text"
                                    label="Password"
                                    readonly
                                ></v-text-field>
                            </v-flex>
                        </v-layout>
                        </v-container>
                    </v-card-text>

                    <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-btn color="blue darken-1" flat @click.native="dialog.value = false">Cancel</v-btn>
                        <v-btn color="blue darken-1" @click.native="save">Save</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>
        </v-toolbar>
        <v-data-table
            :headers="header"
            :items="users"
            :loading="loading"
            hide-actions
            class="elevation-1"
        >
            <template slot="items" slot-scope="props">
                <td>{{ props.item.login }}</td>
                <td>{{ props.item.group }}</td>
                <td class="text-xs-right">
                    <v-icon
                        small
                        class="mr-2"
                        @click="editItem(props.item)"
                    >
                        edit
                    </v-icon>
                    <v-icon
                        small
                        @click="deleteItem(props.item)"
                    >
                        delete
                    </v-icon>
                </td>
            </template>
        </v-data-table>
    </v-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import { generate } from 'generate-password'

export default {
    name: 'users',
    data() {
        return {
            loading: false,
            dialog: {
                title: 'Add user',
                value: false,
                user: {},
                type: 'ADD'
            },
            header: [
                { text: 'Name', value: 'login' },
                 { text: 'Group', value: 'group' },
                { text: 'Actions', value: 'login', align: 'right', sortable: false }
            ]
        }
    },
    computed: {
        ...mapGetters({
            users: 'user/users',
            groups: 'user/groups',
        })
    },
    methods: {
        ...mapActions({
            updateUsers: 'user/updateUsers',
            updateGroups: 'user/updateGroups',
            addUser: 'user/addUser',
            delUser: 'user/delUser',
            editUser: 'user/editUser'
        }),
        addItem() {
            const sorted = this.groups.slice(0).sort(({level: a}, {level: b}) => a - b);
            this.dialog = {
                type: 'ADD',
                value: true,
                user: {
                    pass: generate({ length: 10, numbers: true }),
                    group: sorted.length > 0 && sorted[0].group || ''
                },
                title: 'Add user'
            }
            console.log(this.dialog.user.group);
        },
        editItem(item) {
            this.dialog = {
                type: 'EDIT',
                value: true,
                user: { ...item },
                title: 'Edit user'
            }
        },
        deleteItem(item) {
            if(confirm("Are you sure ?")) {
                this.delUser({ id: item.id })
            }
        },
        save() {
            if (this.dialog.type == 'ADD') {
                const { login, pass, group } = this.dialog.user;
                this.addUser({ login, pass, group }).then((res) => {
                    this.dialog.value = !res;
                })
            } else if (this.dialog.type == 'EDIT') {
                const { id, login, group } = this.dialog.user;
                this.editUser({ id, login, group }).then(() => {
                    this.dialog.value = false;
                })
            }
        }
    },
    mounted() {
        this.loading = true;
        Promise.all([this.updateGroups(), this.updateUsers()]).then(() => {
            this.loading = false;
        });
    }
}
</script>
