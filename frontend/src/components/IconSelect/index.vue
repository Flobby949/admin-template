<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="bottom-start"
    :width="400"
    trigger="click"
  >
    <template #reference>
      <el-input
        v-model="selectedIcon"
        :placeholder="placeholder"
        readonly
        :clearable="clearable"
        @clear="handleClear"
      >
        <template #prefix>
          <el-icon v-if="selectedIcon" class="icon-preview">
            <component :is="selectedIcon" />
          </el-icon>
        </template>
        <template #suffix>
          <el-icon class="icon-arrow">
            <ArrowDown />
          </el-icon>
        </template>
      </el-input>
    </template>

    <div class="icon-select-container">
      <!-- 搜索框 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索图标..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 图标列表 -->
      <el-scrollbar height="300px" class="icon-list-scrollbar">
        <div class="icon-list">
          <div
            v-for="icon in filteredIcons"
            :key="icon"
            class="icon-item"
            :class="{ active: selectedIcon === icon }"
            @click="handleSelect(icon)"
          >
            <el-icon :size="20">
              <component :is="icon" />
            </el-icon>
            <span class="icon-name">{{ icon }}</span>
          </div>
        </div>
        <el-empty v-if="filteredIcons.length === 0" description="未找到匹配的图标" />
      </el-scrollbar>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowDown, Search } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue?: string
  placeholder?: string
  clearable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请选择图标',
  clearable: true
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string]
  change: [value: string]
}>()

// 状态
const popoverVisible = ref(false)
const searchKeyword = ref('')
const selectedIcon = ref(props.modelValue)

// 获取所有图标名称
const allIcons = computed(() => {
  return Object.keys(ElementPlusIconsVue).filter(
    key => key !== 'default' && typeof (ElementPlusIconsVue as any)[key] === 'object'
  )
})

// 过滤图标
const filteredIcons = computed(() => {
  if (!searchKeyword.value) {
    return allIcons.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return allIcons.value.filter(icon => icon.toLowerCase().includes(keyword))
})

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    selectedIcon.value = newVal
  }
)

// 选择图标
const handleSelect = (icon: string) => {
  selectedIcon.value = icon
  emit('update:modelValue', icon)
  emit('change', icon)
  popoverVisible.value = false
}

// 清空选择
const handleClear = () => {
  selectedIcon.value = ''
  emit('update:modelValue', '')
  emit('change', '')
}
</script>

<style scoped>
.icon-select-container {
  padding: 8px;
}

.search-input {
  margin-bottom: 12px;
}

.icon-list-scrollbar {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-item:hover {
  background-color: var(--el-fill-color-light);
}

.icon-item.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.icon-name {
  font-size: 10px;
  margin-top: 4px;
  text-align: center;
  word-break: break-all;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-preview {
  font-size: 16px;
}

.icon-arrow {
  transition: transform 0.2s;
}

:deep(.el-input__wrapper) {
  cursor: pointer;
}
</style>
