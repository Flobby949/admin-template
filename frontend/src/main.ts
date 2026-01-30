import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import pinia from './stores' // Import the pinia instance from stores/index.ts
import './router/guard' // Import routing guards

import { permission } from './directives/permission'

import './styles/index.scss'

const app = createApp(App)

// Register all icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('permission', permission)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')