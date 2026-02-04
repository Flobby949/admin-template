import { defineStore } from 'pinia'
import { debounce } from 'lodash-es'
import type { TabItem, TabsState } from '@/types/tabs'
import router from '@/router'

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    tabs: [
      {
        title: 'Dashboard',
        path: '/dashboard',
        name: 'Dashboard',
        fixed: true,
        icon: 'Dashboard',
        keepAlive: true
      }
    ],
    activeTab: '/dashboard',
    cachedViews: ['Dashboard']
  }),

  getters: {
    // 获取当前激活的标签
    currentTab: state => state.tabs.find(tab => tab.path === state.activeTab),

    // 获取固定标签列表
    fixedTabs: state => state.tabs.filter(tab => tab.fixed),

    // 获取非固定标签列表
    normalTabs: state => state.tabs.filter(tab => !tab.fixed),

    // 检查标签是否存在
    hasTab: state => (path: string) => state.tabs.some(tab => tab.path === path),

    // 获取标签索引
    getTabIndex: state => (path: string) => state.tabs.findIndex(tab => tab.path === path)
  },

  actions: {
    /**
     * 添加标签
     */
    addTab(tab: TabItem) {
      // 使用 path 作为唯一标识（忽略查询参数）
      const existingTab = this.tabs.find(t => t.path === tab.path)

      if (existingTab) {
        // 标签已存在，仅激活
        this.setActiveTab(tab.path)
      } else {
        // 标签不存在，添加到末尾
        this.tabs.push(tab)
        this.setActiveTab(tab.path)

        // 更新缓存视图
        if (tab.keepAlive && tab.name) {
          this.cachedViews.push(tab.name)
        }
      }

      // 持久化状态
      this.persistState()
    },

    /**
     * 删除标签
     */
    removeTab(path: string) {
      const index = this.getTabIndex(path)
      if (index === -1) return

      const tab = this.tabs[index]

      // 固定标签不可删除
      if (tab.fixed) {
        console.warn('Cannot remove fixed tab:', path)
        return
      }

      // 删除标签
      this.tabs.splice(index, 1)

      // 移除缓存视图
      if (tab.name) {
        const cacheIndex = this.cachedViews.indexOf(tab.name)
        if (cacheIndex > -1) {
          this.cachedViews.splice(cacheIndex, 1)
        }
      }

      // 如果删除的是当前激活标签，需要切换到相邻标签
      if (this.activeTab === path) {
        let nextTab: TabItem | undefined

        // 优先跳转到右侧标签
        if (index < this.tabs.length) {
          nextTab = this.tabs[index]
        }
        // 无右侧标签，跳转到左侧标签
        else if (index > 0) {
          nextTab = this.tabs[index - 1]
        }
        // 无相邻标签，跳转到首页（固定标签）
        else {
          nextTab = this.fixedTabs[0]
        }

        if (nextTab) {
          this.setActiveTab(nextTab.path)
          router.push(nextTab.path)
        }
      }

      // 持久化状态
      this.persistState()
    },

    /**
     * 删除其他标签
     */
    removeOtherTabs(path: string) {
      // 保留固定标签和当前标签
      this.tabs = this.tabs.filter(tab => tab.fixed || tab.path === path)

      // 更新缓存视图
      this.updateCachedViews()

      // 持久化状态
      this.persistState()
    },

    /**
     * 删除左侧标签
     */
    removeLeftTabs(path: string) {
      const index = this.getTabIndex(path)
      if (index === -1) return

      // 保留固定标签和当前标签及右侧标签
      this.tabs = this.tabs.filter((tab, i) => tab.fixed || i >= index)

      // 更新缓存视图
      this.updateCachedViews()

      // 持久化状态
      this.persistState()
    },

    /**
     * 删除右侧标签
     */
    removeRightTabs(path: string) {
      const index = this.getTabIndex(path)
      if (index === -1) return

      // 保留固定标签和当前标签及左侧标签
      this.tabs = this.tabs.filter((tab, i) => tab.fixed || i <= index)

      // 更新缓存视图
      this.updateCachedViews()

      // 持久化状态
      this.persistState()
    },

    /**
     * 删除所有标签
     */
    removeAllTabs() {
      // 仅保留固定标签
      this.tabs = this.fixedTabs

      // 更新缓存视图
      this.updateCachedViews()

      // 跳转到首页
      const homePage = this.fixedTabs[0]
      if (homePage) {
        this.setActiveTab(homePage.path)
        router.push(homePage.path)
      }

      // 持久化状态
      this.persistState()
    },

    /**
     * 设置激活标签
     */
    setActiveTab(path: string) {
      this.activeTab = path
    },

    /**
     * 更新缓存视图列表
     */
    updateCachedViews() {
      this.cachedViews = this.tabs
        .filter(tab => tab.keepAlive && tab.name)
        .map(tab => tab.name as string)
    },

    /**
     * 从 localStorage 恢复状态
     */
    restoreState() {
      try {
        const savedState = localStorage.getItem('tabs-state')
        if (savedState) {
          const state = JSON.parse(savedState)

          // 验证数据结构
          if (Array.isArray(state.tabs)) {
            this.tabs = state.tabs
          }

          if (typeof state.activeTab === 'string') {
            this.activeTab = state.activeTab
          }

          if (Array.isArray(state.cachedViews)) {
            this.cachedViews = state.cachedViews
          }

          // 确保至少有一个固定标签
          if (!this.tabs.some(tab => tab.fixed)) {
            this.tabs.unshift({
              title: 'Dashboard',
              path: '/dashboard',
              name: 'Dashboard',
              fixed: true,
              icon: 'Dashboard',
              keepAlive: true
            })
          }
        }
      } catch (error) {
        console.error('Failed to restore tabs state:', error)
      }
    },

    /**
     * 持久化状态到 localStorage（防抖 500ms）
     */
    persistState: debounce(function (this: any) {
      try {
        const state = {
          tabs: this.tabs,
          activeTab: this.activeTab,
          cachedViews: this.cachedViews
        }
        localStorage.setItem('tabs-state', JSON.stringify(state))
      } catch (error) {
        console.error('Failed to persist tabs state:', error)
      }
    }, 500)
  }
})
