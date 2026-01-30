<template>
  <div class="common-layout">
    <el-container>
      <!-- 移动端遮罩层 -->
      <div
        v-if="isMobile && !isCollapsed"
        class="sidebar-overlay"
        @click="toggleSidebar"
      />

      <el-aside
        :width="sidebarWidth"
        class="aside-container"
        :class="{ 'is-collapsed': isCollapsed, 'is-mobile': isMobile }"
      >
        <router-link to="/dashboard" class="logo">
          <div class="logo-icon">
            <el-icon :size="24"><ElementPlus /></el-icon>
          </div>
          <span v-show="!isCollapsed" class="logo-text">Admin System</span>
        </router-link>
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isCollapsed && !isMobile"
            :unique-opened="true"
            :collapse-transition="false"
            router
          >
            <!-- Dashboard 固定菜单 -->
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <template #title><span>Dashboard</span></template>
            </el-menu-item>
            <!-- 动态菜单 -->
            <sidebar-item
              v-for="route in menuRoutes"
              :key="route.path"
              :item="route"
              :is-collapse="isCollapsed && !isMobile"
            />
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-container class="main-container">
        <el-header class="header-container">
          <div class="header-left">
            <el-button
              class="hamburger-btn"
              :icon="isCollapsed ? Expand : Fold"
              @click="toggleSidebar"
              text
            />
            <h3 class="page-title">{{ currentRouteName }}</h3>
          </div>
          <div class="header-right">
            <ThemeSettings />
            <ThemeToggle />
            <el-dropdown trigger="click">
              <span class="user-dropdown" role="button" tabindex="0">
                <el-avatar
                  :size="36"
                  src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
                  alt="用户头像"
                />
                <span class="username">Admin</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showChangePassword = true">
                    <el-icon><Lock /></el-icon>
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <!-- 修改密码对话框 -->
    <ChangePassword v-model="showChangePassword" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/permission'
import { HomeFilled, ElementPlus, ArrowDown, Lock, SwitchButton, Fold, Expand } from '@element-plus/icons-vue'
import SidebarItem from './components/SidebarItem.vue'
import ThemeToggle from '@/components/ThemeToggle/index.vue'
import ThemeSettings from '@/components/ThemeSettings/index.vue'
import ChangePassword from '@/components/ChangePassword/index.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

// 响应式状态
const isCollapsed = ref(false)
const windowWidth = ref(window.innerWidth)
const showChangePassword = ref(false)

// 移动端断点
const MOBILE_BREAKPOINT = 768

// 计算属性
const isMobile = computed(() => windowWidth.value < MOBILE_BREAKPOINT)
const sidebarWidth = computed(() => {
  if (isMobile.value) {
    return isCollapsed.value ? '0px' : '260px'
  }
  return isCollapsed.value ? '64px' : '260px'
})

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta?.title || 'Dashboard')

// 获取动态菜单路由（过滤掉隐藏的）
const menuRoutes = computed(() => {
  return permissionStore.dynamicRoutes.filter(route => !route.meta?.hidden)
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
  // 移动端默认折叠
  if (isMobile.value && !isCollapsed.value) {
    isCollapsed.value = true
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  // 初始化时检查是否为移动端
  if (isMobile.value) {
    isCollapsed.value = true
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.common-layout {
  height: 100vh;
  display: flex;
}

.el-container {
  height: 100%;
}

/* 移动端遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 998;
  transition: opacity 0.3s;
}

.aside-container {
  background-color: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease;
  z-index: 999;

  &.is-mobile {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;

    &.is-collapsed {
      transform: translateX(-100%);
    }
  }

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid var(--border-light);
    text-decoration: none;
    transition: var(--transition-fast);

    &:hover {
      background-color: var(--bg-hover);
    }

    .logo-icon {
      width: 40px;
      height: 40px;
      min-width: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, var(--primary-color), #3b82f6);
      border-radius: var(--radius-base);
      color: white;
      margin-right: 12px;
    }

    .logo-text {
      font-weight: 700;
      font-size: 18px;
      color: var(--text-main);
      letter-spacing: -0.5px;
      white-space: nowrap;
      overflow: hidden;
    }
  }

  &.is-collapsed:not(.is-mobile) .logo {
    padding: 0 12px;
    justify-content: center;

    .logo-icon {
      margin-right: 0;
    }
  }

  .menu-scrollbar {
    flex: 1;
    padding: 12px 0;
  }
}

.sidebar-menu {
  background-color: transparent !important;
  border-right: none !important;

  &.el-menu--collapse {
    width: 64px;

    .el-menu-item,
    :deep(.el-sub-menu__title) {
      padding: 0 20px !important;
      margin: 4px 8px;
    }
  }
}

.main-container {
  background-color: var(--bg-body);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
}

.header-container {
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 9;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .hamburger-btn {
      font-size: 20px;
      color: var(--text-secondary);
      padding: 8px;

      &:hover {
        color: var(--primary-color);
        background-color: var(--bg-hover);
      }
    }

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-main);
      margin: 0;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: var(--radius-base);
      transition: var(--transition-fast);

      &:hover {
        background-color: var(--bg-hover);
      }

      &:focus {
        outline: 2px solid var(--primary-color);
        outline-offset: 2px;
      }

      .username {
        margin: 0 8px 0 12px;
        font-weight: 500;
        color: var(--text-main);

        @media (max-width: 768px) {
          display: none;
        }
      }

      .dropdown-icon {
        color: var(--text-secondary);
        font-size: 12px;

        @media (max-width: 768px) {
          display: none;
        }
      }
    }
  }
}

.main-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;

  @media (max-width: 768px) {
    padding: 16px;
  }
}

/* Dropdown Menu Style */
:deep(.el-dropdown-menu__item) {
  padding: 10px 16px;

  .el-icon {
    margin-right: 8px;
  }
}
</style>
