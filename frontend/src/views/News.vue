<template>
    <div>
        <v-container>
            <v-toolbar flat>
                <v-toolbar-title>News</v-toolbar-title>
                <v-spacer></v-spacer>
                <v-btn color="primary" dark class="mb-2" @click="addItem">Add News</v-btn>
                <v-dialog v-model="dialog.value" max-width="500px">
                    <v-card>
                        <v-card-title>
                            <span class="headline">{{ dialog.title }}</span>
                        </v-card-title>

                        <v-card-text>
                            <v-container grid-list-md>
                            <v-layout wrap>
                                <v-flex xs12>
                                    <v-text-field v-model="dialog._new.message" label="Message"></v-text-field>
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
                :items="news"
                :loading="loading"
                hide-actions
                class="elevation-1"
            >
                <template slot="items" slot-scope="props">
                    <tr>
                        <td>{{ dateformat(props.item.date) }}</td>
                        <td>{{ props.item.message }}</td>
                        <td class="text-xs-right">
                            <v-icon small @click.stop="deleteItem(props.item)">delete</v-icon>
                        </td>
                    </tr>
                </template>
            </v-data-table>
        </v-container>
    </div>
</template>


<script>
import { mapGetters, mapActions } from 'vuex';
import dateformat from 'dateformat'

export default {
    name: 'News',
    data() {
        return {
            header: [
                { text: 'Date', value: 'date' },
                { text: 'Message', value: 'message' },
                { text: 'Actions', value: 'date', align: 'right', sortable: false }
            ],
            dialog: {
                value: false,
                _new: {}
            },
            loading: true
        }
    },
    computed: {
        ...mapGetters({
            news: 'news/news'
        })
    },
    methods: {
        ...mapActions({
            updateNews: 'news/updateNews',
            addNews: 'news/addNews',
            delNews: 'news/delNews',
        }),
        addItem() {
            this.dialog = {
                value: true,
                title: 'Add news',
                _new: {}
            }
        },
        deleteItem({ id }) {
            if (confirm("Are you sure ?")) {
                this.delNews({ id })
            }
        },
        save() {
            const { message } = this.dialog._new;
            this.addNews({ message }).then((res) => {
                this.dialog.value = !res;
            })
        },
        dateformat
    },
    mounted() {
        this.loading = true
        this.updateNews().then(() => {
            this.loading = false
        })
    }
}
</script>
