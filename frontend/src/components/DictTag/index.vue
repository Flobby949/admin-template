<template>
  <el-tag
    v-if="currentDict"
    :type="currentDict.listClass === 'primary' || currentDict.listClass === 'success' || currentDict.listClass === 'info' || currentDict.listClass === 'warning' || currentDict.listClass === 'danger' ? currentDict.listClass : ''"
    :class="currentDict.cssClass"
  >
    {{ currentDict.dictLabel }}
  </el-tag>
  <span v-else>{{ value }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DictDataVO } from '@/api/system/dict/data'

const props = defineProps({
  options: {
    type: Array as () => DictDataVO[],
    default: () => []
  },
  value: {
    type: [Number, String],
    default: null
  }
})

const currentDict = computed(() => {
  if (!props.options || !props.value) return null
  return props.options.find(item => item.dictValue == props.value.toString())
})
</script>

<style scoped>
.el-tag {
  margin-left: 2px;
  margin-right: 2px;
}
</style>
