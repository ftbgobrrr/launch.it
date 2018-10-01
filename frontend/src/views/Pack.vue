<template>
    <v-container v-if="pack">
        <v-toolbar flat tabs>
            <v-toolbar-title>Pack {{ pack.name }}</v-toolbar-title>
            <v-spacer></v-spacer>

            <v-btn icon @click="openDialog()">
                <v-icon>add</v-icon>
            </v-btn>
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
                    <v-data-table
                        :headers="[
                            { text: 'Name', value: 'name' },
                            { text: 'Size', value: 'size' }, 
                            { text: 'Type', value: 'type' },
                            { text: 'Action', value: 'name', sortable: false }
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
                                <td>
                                    <v-btn color="warning" @click.stop="toggleEnable(props.item, 'library')">{{ props.item.desabled ? 'ENABLE' : 'DISABLE' }}</v-btn>
                                    <v-btn color="error" @click.stop="deleteItem(props.item, 'library')" v-if="props.item.type !== 'MOJANG'">DELETE</v-btn>
                                </td>
                            </tr>
                        </template>
                        <template slot="expand" slot-scope="props">
                            <div class="pa-3 subheading" style=" background-color: #212121">
                                <ul>
                                    <li>Url: <a :href="props.item.downloads.artifact.url">{{ props.item.downloads.artifact.url }}</a></li>
                                    <li>Checksum: {{ props.item.downloads.artifact.sha1 }}</li>
                                    <li>Path: {{ props.item.downloads.artifact.path }}</li>
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
                                <td>
                                    <v-btn color="error" @click.stop="deleteItem(props.item, 'native')" v-if="props.item.type !== 'MOJANG'">DELETE</v-btn>
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
            <v-tab-item>
                <v-card flat>
                    <v-data-table
                        :headers="[
                            { text: 'Name', value: 'name' },
                            { text: 'Size', value: 'size' }, 
                            { text: 'Action', value: 'name', sortable: false }
                        ]"
                        :items="files"
                        hide-actions
                        class="elevation-1"
                        item-key="name"
                    >
                        <template slot="items" slot-scope="props">
                            <tr @click="props.expanded = !props.expanded">
                                <td>{{ props.item.name }}</td>
                                <td>{{ size(props.item.downloads.artifact.size) }}</td>
                                <td>
                                    <v-btn color="error" @click.stop="deleteItem(props.item, 'file')">DELETE</v-btn>
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
        <v-dialog v-model="dialog" max-width="500px">
            <v-card>
                <v-card-title>
                    <span class="headline">Add file</span>
                </v-card-title>

                <v-card-text>
                    <v-container grid-list-md>
                        <v-layout wrap>
                            <v-flex xs12>
                                <v-select v-model="form.type" :items="['library', 'file']" label="Type"></v-select>
                            </v-flex>
                            <template v-if="form.type">
                                <v-flex xs12>
                                    <v-text-field v-model="form.name" label="Name"></v-text-field>
                                </v-flex>
                            </template>
                            <template v-if="form.type == 'library'">
                                <v-flex xs12>
                                    <v-text-field v-model="form.pkg" label="Package"></v-text-field>
                                </v-flex>
                                <v-flex xs12>
                                    <v-text-field v-model="form.version" label="Version"></v-text-field>
                                </v-flex>
                            </template>
                            <template v-if="form.type == 'file'">
                                <v-flex xs12>
                                    <v-text-field v-model="form.folder" label="Folder"></v-text-field>
                                </v-flex>
                            </template>
                            <template v-if="form.type">
                                <v-flex xs8>
                                    <v-text-field :value="path" label="Path (Generated)" readonly></v-text-field>
                                </v-flex>
                                <v-flex offset-xs1 xs3>
                                    <upload-btn blocks color="primary" :selectedCallback="fileSet">Upload</upload-btn>
                                </v-flex>
                            </template>
                            <v-flex offset-xs6 xs3>
                                <v-card-actions>
                                    <v-spacer></v-spacer>
                                    <v-btn color="blue darken-1" flat @click.native="dialog = false">Cancel</v-btn>
                                    <v-btn color="blue darken-1" @click.native="save">Add</v-btn>
                                </v-card-actions>
                            </v-flex>
                        </v-layout>
                    </v-container>
                </v-card-text>
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
            dialog: false,
            form: {},
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
        files() {
            return this.pack.files;
        },
        path() {
            const { type, pkg = 'package', name = 'name', version = 'version', folder = 'folder' } = this.form;
            if (type == 'library')
                return `${pkg.replace(/\./g, '/')}/${name}/${version}/${name}-${version}.jar`;
            else
                return `${folder}/${name}.jar`;
        }
    },
    methods: {
        ...mapActions({
            updatePack: 'packs/updatePack',
            upload: 'packs/upload',
        }),
        size(s) {
            return filesize(s).human();
        },
        openDialog() {
            this.dialog = true;
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
            this.form.file = file;
        },
        save() {
            const { name, type, file } = this.form;
            let upload = {};

            switch (type) {
                case 'library':
                    const { pkg, version } = this.form;

                    if (!name || !pkg || !version || name == '' || pkg == '' ||  version == '') {
                        return this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty fields'})
                    } else if (!file || file == '') {
                        return this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'File not set'})
                    } else {
                        upload = {
                            id: this.pack.id,
                            type: 'library',
                            name,
                            pkg,
                            version,
                            file,
                        };
                    }
                    break;
                case 'file':
                    const { folder } = this.form;

                    if (!name || !folder || name == '' || folder == '') {
                        return this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'Empty fields'})
                    } else if (!file || file == '') {
                        return this.$notify({ group: 'main', title: 'Error', type: 'error', text: 'File not set'})
                    } else {
                        upload = {
                            id: this.pack.id,
                            type: 'file',
                            name,
                            folder,
                            file,
                        };
                    }
                    break;
            }
            this.upload(upload)
                .then(console.log);
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
