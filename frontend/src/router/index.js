import Vue from 'vue'
import Router from 'vue-router'
import { store } from '@/store'
import { isLoggedIn } from '@/utils'

Vue.use(Router)

const routes = [
  {
    path: '/',
    name: 'Home',
    icon: "home",
    dName: "Home",
    component: () => import ('@/views/Home')
  },
  {
    path: '/users',
    name: 'Users',
    icon: "people",
    dName: "Manage Users",
    group: 'ADMIN',
    component: () => import ('@/views/Users')
  },
  {
    path: '/packs',
    name: 'Packs',
    icon: "all_inbox",
    dName: "Packs",
    component: () => import ('@/views/Packs')
  },
  {
    path: '/news',
    name: 'News',
    icon: "forum",
    dName: "News",
    component: () => import ('@/views/News')
  },
  {
    path: '/packs/:id',
    name: 'Pack',
    hide: true,
    props: true,
    component: () => import ('@/views/Pack')
  },
  {
    path: '/auth/login',
    name: 'Login',
    hide: true,
    component: () => import ('@/views/auth/Login')
  }
];

const router = new Router({
  routes,
  mode: 'history'
})

router.beforeEach((to, from, next) => {
  const needAuth = !to.path.startsWith('/auth/');
  if (needAuth && !isLoggedIn()) {
    next({ name: 'Login' })
  } else {
    if (to.path.startsWith('/auth/') && isLoggedIn())
      next({ name: 'Home' })
    else next()
  }
})

export {
  router,
  routes
}