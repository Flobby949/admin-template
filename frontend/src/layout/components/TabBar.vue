<template>
  <div class="tab-bar">
    <el-scrollbar ref="scrollContainer" class="scroll-container">
      <div class="tabs-wrapper">
        <TabItem
          v-for="tab in tabs"
          :key="tab.path"
          :tab="tab"
          :active="tab.path === activeTab"
          @click="handleTabClick"
          @close="handleTabClose"
          @contextmenu="handleContextMenu"
        />
      </div>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenuVisible"
        :style="contextMenuStyle"
        class="tab-context-menu"
      >
        <ul class="context-menu-list">
          <li class="context-menu-item" @click="handleMenuCommand('refresh')">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </li>
          <li
            class="context-menu-item"
            :class="{ disabled: selectedTab?.fixed }"
            @click="!selectedTab?.fixed && handleMenuCommand('close-current')"
          >
            <el-icon><Close /></el-icon>
            <span>关闭当前</span>
          </li>
          <li class="context-menu-item" @click="handleMenuCommand('close-other')">
            <el-icon><CircleClose /></el-icon>
            <span>关闭其他</span>
          </li>
          <li
            class="context-menu-item"
            :class="{ disabled: isLeftmost }"
            @click="!isLeftmost && handleMenuCommand('close-left')"
          >
            <el-icon><Back /></el-icon>
            <span>关闭左侧</span>
          </li>
          <li
            class="context-menu-item"
            :class="{ disabled: isRightmost }"
            @click="!isRightmost && handleMenuCommand('close-right')"
          >
            <el-icon><Right /></el-icon>
            <span>关闭右侧</span>
          </li>
          <li class="context-menu-item" @click="handleMenuCommand('close-all')">
            <el-icon><Delete /></el-icon>
            <span>关闭所有</span>
          </li>
        </ul>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/modules/tabs'
import { Refresh, Close, CircleClose, Back, Right, Delete } from '@element-plus/icons-vue'
import TabItem from './TabItem.vue'
import type { TabItem as TabItemType } from '@/types/tabs'

const router = useRouter()
const tabsStore = useTabsStore()

const tabs = computed(() => tabsStore.tabs)
const activeTab = computed(() => tabsStore.activeTab)

// 右键菜单相关
const contextMenuVisible = ref(false)
const selectedTab = ref<TabItemType | null>(null)
const contextMenuPosition = ref({ x: 0, y: 0 })

const contextMenuStyle = computed(() => ({
  position: 'fixed' as const,
  left: `${contextMenuPosition.value.x}px`,
  top: `${contextMenuPosition.value.y}px`,
  zIndex: 9999
}))

const isLeftmost = computed(() => {
  if (!selectedTab.value) return true
  const index = tabsStore.getTabIndex(selectedTab.value.path)
  return index <= 0 || tabs.value.slice(0, index).every(tab => tab.fixed)
})

const isRightmost = computed(() => {
  if (!selectedTab.value) return true
  const index = tabsStore.getTabIndex(selectedTab.value.path)
  return index >= tabs.value.length - 1 || tabs.value.slice(index + 1).every(tab => tab.fixed)
})

// 滚动容器引用
const scrollContainer = ref()

// 标签点击
const handleTabClick = (tab: TabItemType) => {
  if (tab.path !== activeTab.value) {
    router.push(tab.path)
  }
}

// 标签关闭
const handleTabClose = (tab: TabItemType) => {
  tabsStore.removeTab(tab.path)
}

// 右键菜单
const handleContextMenu = (event: MouseEvent, tab: TabItemType) => {
  contextMenuVisible.value = true
  selectedTab.value = tab
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }

  // 点击其他地方关闭菜单
  const closeMenu = () => {
    contextMenuVisible.value = false
    document.removeEventListener('click', closeMenu)
  }
  nextTick(() => {
    document.addEventListener('click', closeMenu)
  })
}

// 菜单命令处理
const handleMenuCommand = (command: string) => {
  if (!selectedTab.value) return

  switch (command) {
    case 'refresh':
      // 刷新当前页面
      router.replace({
        path: '/redirect' + selectedTab.value.path
      })
      break
    case 'close-current':
      tabsStore.removeTab(selectedTab.value.path)
      break
    case 'close-other':
      tabsStore.removeOtherTabs(selectedTab.value.path)
      break
    case 'close-left':
      tabsStore.removeLeftTabs(selectedTab.value.path)
      break
    case 'close-right':
      tabsStore.removeRightTabs(selectedTab.value.path)
      break
    case 'close-all':
      tabsStore.removeAllTabs()
      break
  }

  contextMenuVisible.value = false
}

// 自动滚动到激活的标签
const scrollToActiveTab = () => {
  nextTick(() => {
    const activeTabElement = document.querySelector('.tab-item.is-active')
    if (activeTabElement && scrollContainer.value) {
      activeTabElement.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'center'
      })
    }
  })
}

// 监听激活标签变化，自动滚动
import { watch } from 'vue'
watch(activeTab, () => {
  scrollToActiveTab()
})
</script>

<style scoped lang="scss">
.tab-bar {
  position: relative;
  height: 40px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.scroll-container {
  height: 100%;

  :deep(.el-scrollbar__wrap) {
    overflow-x: auto;
    overflow-y: hidden;
  }

  :deep(.el-scrollbar__view) {
    display: flex;
    align-items: center;
    height: 100%;
  }
}

.tabs-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 10px;
  white-space: nowrap;
}

/* 右键菜单样式 */
.tab-context-menu {
  position: fixed;
  z-index: 9999;
  /* 修复 1: 使用项目定义的卡片背景变量，确保黑白主题都有背景 */
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-shadow: var(--shadow-card); /* 使用项目定义的阴影 */
  padding: 6px 0; /* 稍微增加上下内边距 */
  min-width: 140px;

  .context-menu-list {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  .context-menu-item {
    display: flex;
    align-items: center;
    /* 修复 2: 增加内边距，扩大点击区域，解决间隔太小问题 */
    padding: 10px 20px;
    margin: 0;
    font-size: 14px;
    /* 使用项目通用文字颜色变量 */
    color: var(--text-regular);
    cursor: pointer;
    transition: all 0.2s;

    .el-icon {
      margin-right: 8px;
      font-size: 16px;
    }

    &:hover:not(.disabled) {
      /* 使用项目定义的悬停背景色 */
      background-color: var(--bg-hover);
      /* 使用主色调变量 */
      color: var(--primary-color);
    }

    &.disabled {
      color: var(--text-secondary);
      cursor: not-allowed;
      opacity: 0.5;
    }
  }
}
</style>
