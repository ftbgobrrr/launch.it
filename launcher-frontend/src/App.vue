<template>
  <v-app dark>
    <notifications group="main" />
    <v-slide-y-transition mode="out-in">
      <layout v-if="!isAuth"></layout>
      <auth-layout v-else></auth-layout>
    </v-slide-y-transition>
  </v-app>
</template>

<script>
import { routes } from '@/router'
import { AuthLayout, Layout } from '@/components/layouts'

export default {
  name: 'App',
  data () {
    return {
      drawer: true,
      title: 'Vuetify.js',
      routes
    }
  },
  components: {
    AuthLayout,
    Layout
  },
  computed: {
    isAuth() {
      return this.$route.path.startsWith('/auth/')
    }
  }
}
</script>
