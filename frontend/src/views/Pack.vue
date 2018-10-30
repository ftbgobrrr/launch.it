<template>
    <v-container v-if="pack">
        <v-toolbar flat tabs>
            <v-toolbar-title>Pack {{ pack.name }}</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-btn icon :loading="building" @click="buildVersion()">
                <v-icon>build</v-icon>
            </v-btn>
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
                                    <v-btn small flat color="error" @click.stop="deleteItem(props.item, 'library')" v-if="props.item.type !== 'MOJANG'">DELETE</v-btn>
                                    <v-btn small flat color="warning" @click.stop="toggleEnable(props.item, 'library')">{{ isDesalbed(props.item, 'library') ? 'ENABLE' : 'DISABLE' }}</v-btn>
                                    
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
                            { text: 'Type', value: 'type' }  
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
                            { text: 'Action', value: 'name', align: 'right', sortable: false }
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
                                <td class="text-xs-right">
                                    <v-btn flat small color="error" @click.stop="deleteItem(props.item, 'file')">DELETE</v-btn>
                                    <v-btn small flat color="warning" @click.stop="toggleEnable(props.item, 'file')">{{ isDesalbed(props.item, 'file') ? 'ENABLE' : 'DISABLE' }}</v-btn>
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
                    <v-container>
                        <v-card-text>
                            <v-layout row wrap align-center>
                                <v-flex xs4>
                                    <v-subheader>Main class</v-subheader>
                                </v-flex>
                                <v-flex xs8>
                                    <v-text-field
                                        label="Main class"
                                        v-model="pack.mainClass"
                                    ></v-text-field>
                                </v-flex>
                                <v-flex xs4>
                                    <v-subheader>Additonal Arguments</v-subheader>
                                </v-flex>
                                <v-flex xs8>
                                    <v-textarea
                                        label="Arguments"
                                        v-model="pack.args"
                                    ></v-textarea>
                                </v-flex>
                            </v-layout>
                        </v-card-text>
                        <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-btn @click="saveSettings" color="primary">
                                Save
                            </v-btn>
                        </v-card-actions>
                    </v-container>
                </v-card>
            </v-tab-item>
        </v-tabs>
        <v-dialog persistent v-model="dialog" max-width="500px">
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
                                    <v-btn color="blue darken-1" :disabled="uploading" :loading="uploading" @click.native="save">Add</v-btn>
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
            building: false,
            uploading: false,
            form: {},
            tabs: [ "Libraries", "Natives", "Files", "Arguments"]
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
                return `${folder}/${name}`;
        }
    },
    methods: {
        ...mapActions({
            updatePack: 'packs/updatePack',
            upload: 'packs/upload',
            delFile: 'packs/delFile',
            desableFile: 'packs/desableFile',
            build: 'packs/build',
            settingsPack: 'packs/settingsPack',
        }),
        size(s) {
            return filesize(s).human();
        },
        openDialog() {
            this.dialog = true;
            this.form = {};
        },
        deleteItem({ name }, type) {
            if (confirm("Are you sure ?"))
                this.delFile({ id: this.pack.id, type, name })
        },
        toggleEnable({ name }, type) {
            let desable = this.isDesalbed({ name }, type);
            console.log(desable);
            desable = !desable;
            this.desableFile({ id: this.pack.id, name, type, desable })
        },
        isDesalbed({ name }, type) {
            return this.pack.desabled && !!this.pack.desabled.find(({ type: t, name: n }) => name == n && type == t)
        },
        fileSet(file) {
            this.form.file = file;
        },
        buildVersion() {
            this.building = true
            this.build({ id: this.pack.id }).then(() => {
                this.building = false;
            })
        },
        saveSettings() {
            const mainClass = this.pack.mainClass || this.pack.data.mainClass;
            this.settingsPack({ id: this.pack.id, mainClass, args: this.pack.args })
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
            this.uploading = true;
            this.upload(upload)
                .then((artifact) => {
                    this.dialog = !artifact;
                    this.uploading = false;
                });
        }
    },
    mounted() {
        this.updatePack({ id: this.id }).then((pack) => {
            if(!pack)
                this.router.push({ name: 'Home' });
            else {
                this.pack = pack;
                this.pack.mainClass = this.pack.mainClass || this.pack.data.mainClass;
            }
        })
    }
}
</script>
