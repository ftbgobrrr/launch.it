<template>
    <v-container>
        <v-toolbar flat>
            <v-toolbar-title>Launcher</v-toolbar-title>
        </v-toolbar>
        <v-card>
            <v-container>
                <v-card-text>
                    <v-layout row wrap>
                        <v-flex xs8>
                            <v-subheader>Bootloader</v-subheader>
                        </v-flex>
                        <v-flex xs4>
                            <v-flex xs12>
                                <upload-btn block color="primary" :selectedCallback="fileSetWin">
                                    Windows
                                    <v-spacer></v-spacer>
                                    <i class="v-icon fab fa-windows"></i>
                                </upload-btn>
                            </v-flex>
                            <v-flex xs12>
                                <upload-btn block color="primary" :selectedCallback="fileSetMac">
                                    Mac
                                    <v-spacer></v-spacer>
                                    <i class="v-icon fab fa-apple"></i>
                                </upload-btn>
                            </v-flex>
                            <v-flex xs12>
                                <upload-btn block color="primary" :selectedCallback="fileSetLinux">
                                    Linux
                                    <v-spacer></v-spacer>
                                    <i class="v-icon fab fa-linux"></i>
                                </upload-btn>
                            </v-flex>
                        </v-flex>
                        <v-flex xs8>
                            <v-subheader>Launcher</v-subheader>
                        </v-flex>
                        <v-flex xs4>
                            <upload-btn icon block color="primary" :selectedCallback="fileSetLauncher">
                                Upload
                                <v-spacer></v-spacer>
                                <i class="v-icon fas fa-upload"></i>
                            </upload-btn>
                        </v-flex>
                    </v-layout>
                </v-card-text>
            </v-container>
        </v-card>
    </v-container>
</template>

<script>
import UploadBtn from '@/components/UploadBtn'
import { mapActions } from 'vuex';

export default {
    name: 'Home',
    data() {
        return {
            files: {}
        }
    },
    methods: {
        ...mapActions({
            uploadLauncher: 'home/uploadLauncher'
        }),
        fileSetWin(file) {
            this.files.win = file;
            if (file)
                this.uploadLauncher({ type: 'bootloader', arch: 'win', file }).then(() => delete this.files.win)
        },
        fileSetMac(file) {
            this.files.mac = file; 
            if (file)
                this.uploadLauncher({ type: 'bootloader', arch: 'mac', file }).then(() => delete this.files.mac)
        },
        fileSetLinux(file) {
            this.files.linux = file; 
            if (file)
                this.uploadLauncher({ type: 'bootloader', arch: 'linux', file }).then(() => delete this.files.linux)
        },
        fileSetLauncher(file) {
            this.files.launcher = file; 
            if (file)
                this.uploadLauncher({ type: 'launcher', arch: null, file }).then(() => delete this.files.launcher)
        }
    },
    components: {
        UploadBtn
    }
}
</script>
