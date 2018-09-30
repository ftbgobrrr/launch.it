<template>
    <v-content class="auth-content">
        <v-layout align-center justify-center row fill-height>
            <router-view></router-view>
        </v-layout>
    </v-content>
</template>

<script>
export default {
    name: 'auth-layout'
}
</script>
