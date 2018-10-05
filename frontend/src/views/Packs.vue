<template>
    <div>
        <v-container>
            <v-toolbar flat>
                <v-toolbar-title>Packs</v-toolbar-title>
                <v-spacer></v-spacer>
                <v-btn color="primary" dark class="mb-2" @click="addItem">Add Pack</v-btn>
                <v-dialog v-model="dialog.value" max-width="500px">
                    <v-card>
                        <v-card-title>
                            <span class="headline">{{ dialog.title }}</span>
                        </v-card-title>

                        <v-card-text>
                            <v-container grid-list-md>
                            <v-layout wrap>
                                <v-flex xs12>
                                    <v-text-field v-model="dialog.pack.name" label="Name"></v-text-field>
                                </v-flex>
                                <v-flex xs12 v-if="dialog.type == 'ADD'">
                                   <v-autocomplete
                                        v-model="dialog.pack.preset"
                                        :items="presets"
                                        :loading="dialog.presetsLoading"
                                        color="white"
                                        hide-no-data
                                        hide-selected
                                        item-text="id"
                                        item-value="id"
                                        label="Presets"
                                        return-object
                                    ></v-autocomplete>
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
                :items="packs"
                :loading="loading"
                hide-actions
                class="elevation-1"
            >
                <template slot="items" slot-scope="props">
                    <tr @click="open(props.item)">
                        <td>{{ props.item.name }}</td>
                        <td>{{ props.item.preset }}</td>
                        <td>
                            <v-icon v-if="props.item.default">check_circle</v-icon>
                        </td>
                        <td class="text-xs-right">
                            <v-icon small class="mr-2" @click.stop="editItem(props.item)">edit</v-icon>
                            <v-icon small @click.stop="deleteItem(props.item)">delete</v-icon>
                        </td>
                    </tr>
                </template>
            </v-data-table>
        </v-container>
        <v-container>
            <v-toolbar flat>
                <v-toolbar-title>Default Pack</v-toolbar-title>
            </v-toolbar>
            <v-card>
                <v-card-text>
                    <v-layout row wra>
                        <v-flex xs9 class="pa-2">
                            <v-select
                                v-model="defaultPack"
                                :items="packs"
                                item-text="name"
                                item-value="id"
                                label="Standard"
                            ></v-select>
                        </v-flex>
                        <v-flex xs3 class="pa-2 pt-3">
                            <v-btn color="primary" @click="setDefault()" block>SUBMIT</v-btn>
                        </v-flex>
                    </v-layout>
                </v-card-text>
            </v-card>
        </v-container>
    </div>
</template>

<script>

import { mapGetters, mapActions } from 'vuex';

export default {
    name: 'Packs',
    data() {
        return {
            loading: false,
            defaultPack: null,
            dialog: {
                value: false,
                presetsLoading: true,
                pack: {}
            },
            header: [
                { text: 'Name', value: 'name' },
                { text: 'Preset', value: 'preset' },
                { text: 'Default', value: 'default' },
                { text: 'Actions', value: 'name', align: 'right', sortable: false }
            ]
        }
    },
    computed: {
        ...mapGetters({
            packs: 'packs/packs',
            presets: 'presets/presets'
        })
    },
    methods: {
        ...mapActions({
            updatePacks: 'packs/updatePacks',
            updatePresets: 'presets/updatePresets',
            delPack: 'packs/delPack',
            addPack: 'packs/addPack',
            editPack: 'packs/editPack',
            defPack: 'packs/defaultPack'
        }),
        deleteItem({ id }) {
            if (confirm("Are you sure ?")) {
                this.delPack({ id })
            }
        },
        addItem() {
            this.updatePresets().then(() => {
                this.dialog.presetsLoading = false;
            });
            this.dialog = {
                type: 'ADD',
                value: true,
                presetsLoading: true,
                pack: {},
                title: 'Add pack'
            }
        },
        editItem(item) {
            this.dialog = {
                type: 'EDIT',
                value: true,
                presetsLoading: false,
                pack: { ...item },
                title: 'Edit pack'
            }
        },
        open({ id }) {
            this.$router.push({ name: 'Pack', params: { id }})
        },
        setDefault() {
            this.defPack({ id: this.defaultPack })
        },
        save() {
            if (this.dialog.type == 'ADD') {
                const { name, preset: { id: preset = null } = {} } = this.dialog.pack;
                this.addPack({ name, preset }).then((res) => {
                    this.dialog.value = !res;
                })
            } else if (this.dialog.type == 'EDIT') {
                const { id, name } = this.dialog.pack;
                this.editPack({ id, name }).then((res) => {
                    this.dialog.value = !res;
                })
            }
        }
    },
    mounted() {
        this.updatePacks().then(() => {
            this.defaultPack = this.packs.find(({ default: def }) => def)
        })
    }
}
</script>
