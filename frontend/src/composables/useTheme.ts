import { ref } from 'vue'

export type Theme = 'light' | 'dark'
export type ThemeColor = 'blue' | 'green' | 'purple' | 'orange' | 'red' | 'cyan'

const THEME_KEY = 'app-theme'
const THEME_COLOR_KEY = 'app-theme-color'

// 预设主题色方案（包含 Element Plus 所需的所有变体）
export const themeColors: Record<
  ThemeColor,
  {
    name: string
    primary: string
    hover: string
    light: string
    // Element Plus 颜色变体（浅色模式）
    light3: string
    light5: string
    light7: string
    light8: string
    light9: string
    dark2: string
    // 深色模式专用变体
    darkMode: string
    darkLight3: string // 深色模式下的 light-3
    darkLight5: string // 深色模式下的 light-5
    darkLight7: string // 深色模式下的 light-7
    darkLight8: string // 深色模式下的 light-8
    darkLight9: string // 深色模式下的 light-9
  }
> = {
  blue: {
    name: '科技蓝',
    primary: '#2563EB',
    hover: '#1d4ed8',
    light: '#EFF6FF',
    light3: '#6B9EF8',
    light5: '#A0C4FA',
    light7: '#C7DFFE',
    light8: '#D5E7FF',
    light9: '#E3EFFF',
    dark2: '#1D4ED8',
    darkMode: '#3B82F6',
    darkLight3: '#1E3A8A',
    darkLight5: '#1E40AF',
    darkLight7: '#1D4ED8',
    darkLight8: '#2563EB',
    darkLight9: '#2D3B52' // 比 Slate 800 (#1E293B) 稍亮，用于悬浮/选中
  },
  green: {
    name: '自然绿',
    primary: '#10B981',
    hover: '#059669',
    light: '#ECFDF5',
    light3: '#4ACA9A',
    light5: '#7DDBB3',
    light7: '#B0EBCD',
    light8: '#C2F0DC',
    light9: '#D5F5E6',
    dark2: '#059669',
    darkMode: '#34D399',
    darkLight3: '#064E3B',
    darkLight5: '#065F46',
    darkLight7: '#047857',
    darkLight8: '#059669',
    darkLight9: '#2D3B52'
  },
  purple: {
    name: '优雅紫',
    primary: '#8B5CF6',
    hover: '#7C3AED',
    light: '#F5F3FF',
    light3: '#A78BFA',
    light5: '#C4B5FD',
    light7: '#DDD6FE',
    light8: '#E6E0FE',
    light9: '#EDE9FE',
    dark2: '#7C3AED',
    darkMode: '#A78BFA',
    darkLight3: '#4C1D95',
    darkLight5: '#5B21B6',
    darkLight7: '#6D28D9',
    darkLight8: '#7C3AED',
    darkLight9: '#2D3B52'
  },
  orange: {
    name: '活力橙',
    primary: '#F59E0B',
    hover: '#D97706',
    light: '#FFF7ED',
    light3: '#F8B84E',
    light5: '#FBCF7C',
    light7: '#FDE3AA',
    light8: '#FEEBC4',
    light9: '#FEF3D8',
    dark2: '#D97706',
    darkMode: '#FBBF24',
    darkLight3: '#78350F',
    darkLight5: '#92400E',
    darkLight7: '#B45309',
    darkLight8: '#D97706',
    darkLight9: '#2D3B52'
  },
  red: {
    name: '热情红',
    primary: '#EF4444',
    hover: '#DC2626',
    light: '#FEF2F2',
    light3: '#F47171',
    light5: '#F89E9E',
    light7: '#FBCBCB',
    light8: '#FCD8D8',
    light9: '#FDE5E5',
    dark2: '#DC2626',
    darkMode: '#F87171',
    darkLight3: '#7F1D1D',
    darkLight5: '#991B1B',
    darkLight7: '#B91C1C',
    darkLight8: '#DC2626',
    darkLight9: '#2D3B52'
  },
  cyan: {
    name: '清新青',
    primary: '#06B6D4',
    hover: '#0891B2',
    light: '#ECFEFF',
    light3: '#3FCAE0',
    light5: '#72DBEA',
    light7: '#A5EBF3',
    light8: '#BBF0F7',
    light9: '#D1F5F9',
    dark2: '#0891B2',
    darkMode: '#22D3EE',
    darkLight3: '#164E63',
    darkLight5: '#155E75',
    darkLight7: '#0E7490',
    darkLight8: '#0891B2',
    darkLight9: '#2D3B52'
  }
}

// 应用主题色到 CSS 变量
const applyThemeColor = (color: ThemeColor) => {
  const colorScheme = themeColors[color]
  const root = document.documentElement
  const isDark = document.documentElement.classList.contains('dark')

  // 自定义变量（根据深色/浅色模式使用不同的值）
  root.style.setProperty('--primary-color', isDark ? colorScheme.darkMode : colorScheme.primary)
  root.style.setProperty('--primary-hover', colorScheme.hover)
  root.style.setProperty('--primary-light', isDark ? colorScheme.darkLight9 : colorScheme.light)

  // 深色模式的主题色
  root.style.setProperty('--primary-color-dark', colorScheme.darkMode)

  // Element Plus 主色变量（关键！）
  const primaryColor = isDark ? colorScheme.darkMode : colorScheme.primary
  root.style.setProperty('--el-color-primary', primaryColor)

  // 根据深色/浅色模式使用不同的颜色变体
  if (isDark) {
    root.style.setProperty('--el-color-primary-light-3', colorScheme.darkLight3)
    root.style.setProperty('--el-color-primary-light-5', colorScheme.darkLight5)
    root.style.setProperty('--el-color-primary-light-7', colorScheme.darkLight7)
    root.style.setProperty('--el-color-primary-light-8', colorScheme.darkLight8)
    root.style.setProperty('--el-color-primary-light-9', colorScheme.darkLight9)
  } else {
    root.style.setProperty('--el-color-primary-light-3', colorScheme.light3)
    root.style.setProperty('--el-color-primary-light-5', colorScheme.light5)
    root.style.setProperty('--el-color-primary-light-7', colorScheme.light7)
    root.style.setProperty('--el-color-primary-light-8', colorScheme.light8)
    root.style.setProperty('--el-color-primary-light-9', colorScheme.light9)
  }

  root.style.setProperty('--el-color-primary-dark-2', colorScheme.dark2)

  // 文字按钮颜色（Element Plus 使用 --el-color-primary）
  root.style.setProperty('--el-button-text-color', primaryColor)
  root.style.setProperty('--el-button-hover-text-color', colorScheme.hover)

  // 标签边框颜色（Tag 组件）- 移除，使用 Element Plus 默认变量
  // Tag 会自动使用 --el-color-primary-light-8 和 --el-color-primary-light-9
}

// 立即初始化主题（避免 FOUC）
const initTheme = (): Theme => {
  const storedTheme = localStorage.getItem(THEME_KEY) as Theme | null
  const systemTheme = window.matchMedia('(prefers-color-scheme: dark)')

  const initialTheme = storedTheme || (systemTheme.matches ? 'dark' : 'light')

  const html = document.documentElement
  if (initialTheme === 'dark') {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }

  return initialTheme
}

// 初始化主题色
const initThemeColor = (): ThemeColor => {
  const storedColor = localStorage.getItem(THEME_COLOR_KEY) as ThemeColor | null
  const initialColor = storedColor || 'blue'
  applyThemeColor(initialColor)
  return initialColor
}

// 全局主题状态（在模块加载时立即初始化）
const theme = ref<Theme>(initTheme())
const themeColor = ref<ThemeColor>(initThemeColor())

export function useTheme() {
  const applyTheme = (newTheme: Theme) => {
    const html = document.documentElement
    if (newTheme === 'dark') {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
    theme.value = newTheme
    localStorage.setItem(THEME_KEY, newTheme)

    // 重新应用主题色以适配新的深色/浅色模式
    applyThemeColor(themeColor.value)
  }

  const toggleTheme = () => {
    applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  const setThemeColor = (color: ThemeColor) => {
    applyThemeColor(color)
    themeColor.value = color
    localStorage.setItem(THEME_COLOR_KEY, color)
  }

  return {
    theme,
    themeColor,
    toggleTheme,
    setTheme: applyTheme,
    setThemeColor
  }
}
