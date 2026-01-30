<template>
  <div class="theme-toggle">
    <el-tooltip
      :content="theme === 'light' ? '切换至深色模式' : '切换至浅色模式'"
      placement="bottom"
    >
      <el-button text circle class="toggle-btn" @click="toggleTheme">
        <el-icon :size="20">
          <Moon v-if="theme === 'light'" />
          <Sunny v-else />
        </el-icon>
      </el-button>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const { theme, toggleTheme } = useTheme()
</script>

<style scoped lang="scss">
.theme-toggle {
  display: flex;
  align-items: center;
  margin-right: 12px;

  .toggle-btn {
    color: var(--text-secondary);
    transition: var(--transition-fast);

    &:hover {
      color: var(--primary-color);
      background-color: var(--bg-hover);
    }
  }
}
</style>
