<template>
  <el-select
    v-model="modelValueComputed"
    :placeholder="placeholder"
    :clearable="clearable"
    :multiple="multiple"
    :disabled="disabled"
    style="width: 100%"
  >
    <el-option
      v-for="item in options"
      :key="item.dictValue"
      :label="item.dictLabel"
      :value="item.dictValue"
      :disabled="item.status === '0'"
    />
  </el-select>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import useDictStore from '@/stores/dict'
import { DictDataVO } from '@/api/system/dict/data'

const props = defineProps({
  dictType: {
    type: String,
    required: true
  },
  modelValue: {
    type: [String, Number, Array],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  multiple: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const dictStore = useDictStore()
const options = ref<DictDataVO[]>([])

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: val => {
    emit('update:modelValue', val)
    emit('change', val)
  }
})

onMounted(async () => {
  if (props.dictType) {
    options.value = await dictStore.getDict(props.dictType)
  }
})

// watch dictType in case it changes
watch(
  () => props.dictType,
  async newVal => {
    if (newVal) {
      options.value = await dictStore.getDict(newVal)
    } else {
      options.value = []
    }
  }
)
</script>
