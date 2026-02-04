import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/modules/tabs'
import type { TabItem } from '@/types/tabs'

/**
 * 标签操作 Composable
 */
export function useTabs() {
  const router = useRouter()
  const tabsStore = useTabsStore()

  // 计算属性
  const tabs = computed(() => tabsStore.tabs)
  const activeTab = computed(() => tabsStore.activeTab)
  const currentTab = computed(() => tabsStore.currentTab)
  const cachedViews = computed(() => tabsStore.cachedViews)

  /**
   * 添加标签
   */
  const addTab = (tab: TabItem) => {
    tabsStore.addTab(tab)
  }

  /**
   * 删除标签
   */
  const removeTab = (path: string) => {
    tabsStore.removeTab(path)
  }

  /**
   * 删除其他标签
   */
  const removeOtherTabs = (path: string) => {
    tabsStore.removeOtherTabs(path)
  }

  /**
   * 删除左侧标签
   */
  const removeLeftTabs = (path: string) => {
    tabsStore.removeLeftTabs(path)
  }

  /**
   * 删除右侧标签
   */
  const removeRightTabs = (path: string) => {
    tabsStore.removeRightTabs(path)
  }

  /**
   * 删除所有标签
   */
  const removeAllTabs = () => {
    tabsStore.removeAllTabs()
  }

  /**
   * 切换标签
   */
  const switchTab = (path: string) => {
    router.push(path)
  }

  /**
   * 刷新当前标签
   */
  const refreshTab = (path: string) => {
    // 先从缓存中移除
    const tab = tabsStore.tabs.find(t => t.path === path)
    if (tab?.name) {
      const index = tabsStore.cachedViews.indexOf(tab.name)
      if (index > -1) {
        tabsStore.cachedViews.splice(index, 1)
      }
    }

    // 通过 redirect 路由刷新
    router.replace({
      path: '/redirect' + path
    })
  }

  /**
   * 检查标签是否存在
   */
  const hasTab = (path: string) => {
    return tabsStore.hasTab(path)
  }

  /**
   * 获取标签索引
   */
  const getTabIndex = (path: string) => {
    return tabsStore.getTabIndex(path)
  }

  return {
    // 状态
    tabs,
    activeTab,
    currentTab,
    cachedViews,

    // 方法
    addTab,
    removeTab,
    removeOtherTabs,
    removeLeftTabs,
    removeRightTabs,
    removeAllTabs,
    switchTab,
    refreshTab,
    hasTab,
    getTabIndex
  }
}
