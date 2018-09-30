import '@/utils'

const files = require.context('.', false, /\.js$/)
const modules = {}

files.keys().forEach(key => {
  if (key === './index.js') return
  modules[key.replace(/(\.\/|\.js)/g, '').toCamelCase()] = { ...files(key).default, namespaced: true }
})

export default modules