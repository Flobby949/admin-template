<template>
  <div class="theme-settings">
    <!-- 触发按钮 -->
    <el-tooltip content="主题设置" placement="bottom">
      <el-button text circle class="settings-btn" @click="drawerVisible = true">
        <el-icon :size="20">
          <Setting />
        </el-icon>
      </el-button>
    </el-tooltip>

    <!-- 设置抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="主题设置"
      direction="rtl"
      size="320px"
    >
      <div class="settings-content">
        <!-- 深色模式切换 -->
        <div class="setting-section">
          <div class="section-title">
            <el-icon><Moon /></el-icon>
            <span>外观模式</span>
          </div>
          <div class="theme-mode-selector">
            <div
              class="mode-option"
              :class="{ active: theme === 'light' }"
              @click="setTheme('light')"
            >
              <el-icon :size="24"><Sunny /></el-icon>
              <span>浅色</span>
            </div>
            <div
              class="mode-option"
              :class="{ active: theme === 'dark' }"
              @click="setTheme('dark')"
            >
              <el-icon :size="24"><Moon /></el-icon>
              <span>深色</span>
            </div>
          </div>
        </div>

        <!-- 主题色选择 -->
        <div class="setting-section">
          <div class="section-title">
            <el-icon><Brush /></el-icon>
            <span>主题颜色</span>
          </div>
          <div class="theme-color-selector">
            <div
              v-for="(colorInfo, colorKey) in themeColors"
              :key="colorKey"
              class="color-option"
              :class="{ active: themeColor === colorKey }"
              @click="setThemeColor(colorKey as ThemeColor)"
            >
              <div
                class="color-preview"
                :style="{ backgroundColor: colorInfo.primary }"
              >
                <el-icon v-if="themeColor === colorKey" color="#fff" :size="16">
                  <Check />
                </el-icon>
              </div>
              <span class="color-name">{{ colorInfo.name }}</span>
            </div>
          </div>
        </div>

        <!-- 预览区域 -->
        <div class="setting-section">
          <div class="section-title">
            <el-icon><View /></el-icon>
            <span>效果预览</span>
          </div>
          <div class="preview-area">
            <el-button type="primary" size="small">主要按钮</el-button>
            <el-button size="small">次要按钮</el-button>
            <el-tag type="primary" size="small">标签</el-tag>
          </div>
        </div>

        <!-- 说明文字 -->
        <div class="setting-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>主题设置会自动保存到本地存储</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Setting, Moon, Sunny, Brush, Check, View, InfoFilled } from '@element-plus/icons-vue'
import { useTheme, themeColors, type ThemeColor } from '@/composables/useTheme'

const { theme, themeColor, setTheme, setThemeColor } = useTheme()
const drawerVisible = ref(false)
</script>

<style scoped lang="scss">
.theme-settings {
  display: flex;
  align-items: center;

  .settings-btn {
    color: var(--text-secondary);
    transition: var(--transition-fast);

    &:hover {
      color: var(--primary-color);
      background-color: var(--bg-hover);
    }
  }
}

.settings-content {
  padding: 0 4px;
}

.setting-section {
  margin-bottom: 32px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-main);
    margin-bottom: 16px;

    .el-icon {
      color: var(--primary-color);
    }
  }
}

.theme-mode-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  .mode-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    border: 2px solid var(--border-color);
    border-radius: var(--radius-base);
    cursor: pointer;
    transition: var(--transition-fast);
    background-color: var(--bg-card);

    &:hover {
      border-color: var(--primary-color);
      background-color: var(--bg-hover);
    }

    &.active {
      border-color: var(--primary-color);
      background-color: var(--primary-light);
      color: var(--primary-color);
    }

    .el-icon {
      color: var(--text-secondary);
    }

    &.active .el-icon {
      color: var(--primary-color);
    }

    span {
      font-size: 13px;
      font-weight: 500;
    }
  }
}

.theme-color-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;

  .color-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border: 2px solid var(--border-color);
    border-radius: var(--radius-base);
    cursor: pointer;
    transition: var(--transition-fast);
    background-color: var(--bg-card);

    &:hover {
      border-color: var(--border-color);
      background-color: var(--bg-hover);
      transform: translateY(-2px);
    }

    &.active {
      border-color: var(--primary-color);
      background-color: var(--primary-light);
    }

    .color-preview {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      transition: var(--transition-fast);
    }

    &:hover .color-preview {
      transform: scale(1.1);
    }

    .color-name {
      font-size: 12px;
      color: var(--text-secondary);
      font-weight: 500;
    }

    &.active .color-name {
      color: var(--primary-color);
    }
  }
}

.preview-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px;
  background-color: var(--bg-body);
  border-radius: var(--radius-base);
  border: 1px solid var(--border-light);
}

.setting-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: var(--primary-light);
  border-radius: var(--radius-base);
  font-size: 12px;
  color: var(--text-secondary);

  .el-icon {
    color: var(--primary-color);
    flex-shrink: 0;
  }
}
</style>
