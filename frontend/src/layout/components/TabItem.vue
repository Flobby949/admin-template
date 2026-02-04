<template>
  <div
    :class="['tab-item', { 'is-active': active }]"
    @click="handleClick"
    @contextmenu.prevent="handleContextMenu"
  >
    <el-icon v-if="tab.icon" class="tab-icon">
      <component :is="tab.icon" />
    </el-icon>
    <span class="tab-title">{{ tab.title }}</span>
    <el-icon v-if="!tab.fixed" class="tab-close" @click.stop="handleClose">
      <Close />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
import { Close } from '@element-plus/icons-vue'
import type { TabItem } from '@/types/tabs'

interface Props {
  tab: TabItem
  active?: boolean
}

interface Emits {
  (e: 'click', tab: TabItem): void
  (e: 'close', tab: TabItem): void
  (e: 'contextmenu', event: MouseEvent, tab: TabItem): void
}

const props = withDefaults(defineProps<Props>(), {
  active: false
})

const emit = defineEmits<Emits>()

const handleClick = () => {
  emit('click', props.tab)
}

const handleClose = () => {
  emit('close', props.tab)
}

const handleContextMenu = (event: MouseEvent) => {
  emit('contextmenu', event, props.tab)
}
</script>

<style scoped lang="scss">
.tab-item {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 12px;
  margin-right: 4px;
  font-size: 14px;
  line-height: 32px;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  &.is-active {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
  }

  .tab-icon {
    margin-right: 6px;
    font-size: 14px;
  }

  .tab-title {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .tab-close {
    margin-left: 6px;
    font-size: 14px;
    border-radius: 50%;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-danger);
      background-color: var(--el-fill-color);
    }
  }
}
</style>
