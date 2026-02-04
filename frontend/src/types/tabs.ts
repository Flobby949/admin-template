/**
 * 标签项
 */
export interface TabItem {
  /** 标签标题（从 route.meta.title 获取） */
  title: string

  /** 路由路径（唯一标识） */
  path: string

  /** 路由名称 */
  name?: string

  /** 查询参数 */
  query?: Record<string, any>

  /** 路由参数 */
  params?: Record<string, any>

  /** 是否固定标签（固定标签不可关闭） */
  fixed?: boolean

  /** 图标（可选，从 route.meta.icon 获取） */
  icon?: string

  /** 是否需要缓存（用于 keep-alive） */
  keepAlive?: boolean
}

/**
 * 标签状态
 */
export interface TabsState {
  /** 已打开的标签列表 */
  tabs: TabItem[]

  /** 当前激活的标签路径 */
  activeTab: string

  /** 需要缓存的视图名称列表（用于 keep-alive） */
  cachedViews: string[]
}

/**
 * 右键菜单选项
 */
export enum ContextMenuAction {
  CLOSE_CURRENT = 'close-current',
  CLOSE_OTHER = 'close-other',
  CLOSE_LEFT = 'close-left',
  CLOSE_RIGHT = 'close-right',
  CLOSE_ALL = 'close-all',
  REFRESH = 'refresh'
}

/**
 * 右键菜单项
 */
export interface ContextMenuItem {
  action: ContextMenuAction
  label: string
  icon?: string
  disabled?: boolean
}
