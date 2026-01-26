<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px" class="aside-container">
        <div class="logo">Admin System</div>
        <el-menu
          default-active="1"
          class="el-menu-vertical-demo"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="1">
            <el-icon><Menu /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header-container">
          <div class="header-content">
            <h3>Admin Dashboard</h3>
            <el-button type="danger" size="small" @click="handleLogout">Logout</el-button>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { Menu } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
}
.el-container {
  height: 100%;
}
.aside-container {
  background-color: #545c64;
  color: white;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: bold;
  font-size: 20px;
  background-color: #434a50;
}
.header-container {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
}
.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
