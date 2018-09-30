<template>
    <v-container v-if="pack">
        <v-toolbar flat tabs>
            <v-toolbar-title>Pack {{ pack.name }}</v-toolbar-title>
            <v-spacer></v-spacer>
        </v-toolbar>
        <v-tabs
            v-model="tab"
            color="#212121"
        >
            <v-tab v-for="item in tabs" :key="item">
                {{ item }}
            </v-tab>
            <v-tab-item>
                <v-card flat>
                    <v-btn color="primary" dark class="mb-2" @click="addItem('library')">Add Library</v-btn>
                    <v-divider></v-divider>
                    <v-data-table
                        :headers="[
                            { text: 'Name', value: 'name' },
                            { text: 'Size', value: 'size' }, 
                            { text: 'Type', value: 'type' },
                            { text: 'Action', value: 'name', align: 'right', sortable: false }
                        ]"
                        :items="libraries"
                        hide-actions
                        class="elevation-1"
                        item-key="name"
                    >
                        <template slot="items" slot-scope="props">
                            <tr @click="props.expanded = !props.expanded">
                                <td>{{ props.item.name }}</td>
                                <td>{{ size(props.item.size) }}</td>
                                <td>{{ props.item.type }}</td>
                                <td class="text-xs-right">
                                    <v-icon small @click.stop="deleteItem(props.item, 'library')" v-if="props.item.type !== 'MOJANG'">delete</v-icon>
                                    <v-icon small @click.stop="toggleEnable(props.item, 'library')">{{ props.item.desabled ? 'cloud_off' : 'cloud' }}</v-icon>
                                </td>
                            </tr>
                        </template>
                        <template slot="expand" slot-scope="props">
                            <div class="pa-3 subheading" style=" background-color: #212121">
                                <ul>
                                    <li>Url: <a :href="props.item.downloads.artifact.url">{{ props.item.downloads.artifact.url }}</a></li>
                                    <li>Checksum: {{ props.item.downloads.artifact.sha1 }}</li>
                                    <li>path: {{ props.item.downloads.artifact.path }}</li>
                                </ul>
                            </div>
                        </template>
                    </v-data-table>
                </v-card>
            </v-tab-item>
            <v-tab-item>
                <v-card flat>
                    
                    <v-data-table
                        :headers="[
                            { text: 'Name', value: 'name' },
                            { text: 'Type', value: 'type' }, 
                            { text: 'Action', value: 'name', align: 'right', sortable: false }
                        ]"
                        :items="natives"
                        hide-actions
                        class="elevation-1"
                        item-key="name"
                    >
                        <template slot="items" slot-scope="props">
                            <tr @click="props.expanded = !props.expanded">
                                <td>{{ props.item.name }}</td>
                                <td>{{ props.item.type }}</td>
                                <td class="text-xs-right">
                                    <v-icon small @click="deleteItem(props.item)" v-if="props.item.type !== 'MOJANG'">delete</v-icon>
                                </td>
                            </tr>
                        </template>
                        <template slot="expand" slot-scope="props">
                            <div
                                v-for="(n, i) in props.item.natives"
                                :key="i"
                                class="pa-3 subheading" 
                                style=" background-color: #212121"
                            >
                                {{ n.name }}
                                <ul>
                                    <li>Url: <a :href="n.url">{{ n.url }}</a></li>
                                    <li>Checksum: {{ n.sha1 }}</li>
                                    <li>Size: {{ size(n.size) }}</li>
                                    <li>path: {{ n.path }}</li>
                                </ul>
                            </div>
                        </template>
                    </v-data-table>
                </v-card>
            </v-tab-item>
        </v-tabs>
        <v-dialog v-model="dialog.value" max-width="500px">
            <v-card>
                <v-card-title>
                    <span class="headline">{{ dialog.title }}</span>
                </v-card-title>

                <v-card-text>
                    <v-container grid-list-md>
                    <v-layout wrap v-if="dialog.file">
                        <template v-if="dialog.type == 'library'">
                            <v-flex xs12>
                                <v-text-field v-model="dialog.file.package" label="Package"></v-text-field>
                            </v-flex>
                            <v-flex xs12>
                                <v-text-field v-model="dialog.file.name" label="Name"></v-text-field>
                            </v-flex>
                            <v-flex xs12>
                                <v-text-field v-model="dialog.file.version" label="Version"></v-text-field>
                            </v-flex>
                        </template>
                        <template v-if="dialog.type == 'file'">
                            <v-flex xs12>
                                <v-text-field v-model="dialog.file.name" label="Name"></v-text-field>
                            </v-flex>
                            <v-flex xs12>
                                <v-text-field v-model="dialog.file.folder" label="Folder"></v-text-field>
                            </v-flex>
                        </template>
                        
                        <v-flex xs6>
                            <upload-btn
                                blocks
                                color="primary"
                                :selectedCallback="fileSet"
                            >Upload</upload-btn>
                        </v-flex>
                        <v-flex xs6>
                        </v-flex>
                        <v-flex xs12>
                            <v-text-field :value="path" label="Path (Generated)" readonly></v-text-field>
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
    </v-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import filesize from 'file-size'
import UploadBtn from '@/components/UploadBtn'

export default {
    name: 'Pack',
    props: ['id'],
    data() {
        return {
            tab: null,
            pack: null,
            dialog: {
                value: false,
                file: null,
            },
            tabs: [ "Libraries", "Natives", "Files" ]
        }
    },
    components: {
        UploadBtn
    },
    computed: {
        ...mapGetters({
            packs: 'packs/packs'
        }),
        libraries() {
            return this.pack.data.libraries
                .filter(({ natives }) => !natives)
                .map((l) => ({ size: l.downloads.artifact.size, ...l}))
        },
        natives() {
            return this.pack.data.libraries
                .filter(({ natives }) => natives)
                .map((n) => {
                    const natives = [];
                    Object.keys(n.natives)
                        .forEach(k => {
                            if (n.natives[k].includes("${arch}")) {
                                natives.push({ name: `${k}-32`, ...n.downloads.classifiers[n.natives[k].replace('${arch}', "32")] })
                                natives.push({ name: `${k}-64`, ...n.downloads.classifiers[n.natives[k].replace('${arch}', "64")] })
                            }
                            else if (n.natives[k] && n.downloads.classifiers[n.natives[k]])
                                natives.push({ name: k, ...n.downloads.classifiers[n.natives[k]] })
                        })
                        console.log(natives)
                    return { ...n, natives };
                })
        },
        path() {
            const { type, file: { package: pkg = 'package', name = 'name', version = 'version' } = {}} = this.dialog;
            if (type == 'library')
                return `${pkg.replace(/\./g, '/')}/${name}/${version}/${name}-${version}.jar`;
            else
                return `${folder}/${name}.jar`;
        }
    },
    methods: {
        ...mapActions({
            updatePack: 'packs/updatePack'
        }),
        size(s) {
            return filesize(s).human();
        },
        addItem(type) {
            if (type == 'library') {
                this.dialog = {
                    value: true,
                    title: 'Add library',
                    type,
                    file: {
                        format: 'FILE'
                    }
                }
            }
        },
        deleteItem(item, type) {
            if (confirm("Are you sure ?"))
            {

            }
        },
        toggleEnable(item, type) {
            const library =  this.libraries.find(({ name: n}) => n == item.name);
            if (!library.desabled)
                library.desabled = false;
            library.desabled = !library.desabled;
            this.$forceUpdate();
        },
        fileSet(file) {
            console.log(file);
            this.dialog.file.file = file;
        },
        save() {
            if (this.dialog.type == 'library') {
                const { file: { name, package: pkg, version, file } = {} } = this.dialog;
                if (!name || !pkg || !version || name == '' || pkg == '' ||  version == '')
                {
                    this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty fields'})
                    return;
                }

                if (!file || file == '') {
                    this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'File not set'})
                    return;
                }

                const n = `${pkg}:${name}:${version}`
                console.log(pkg, n, version, file)

            }
        }
    },
    mounted() {
        this.updatePack({ id: this.id }).then((pack) => {
            if(!pack)
                this.router.push({ name: 'Home' });
            else this.pack = pack;
        })
    }
}
</script>
