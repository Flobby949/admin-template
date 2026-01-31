<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="父级菜单" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="menuTreeOptions"
          :props="{ label: 'menuName', value: 'id' }"
          placeholder="请选择父级菜单"
          check-strictly
          :render-after-expand="false"
          clearable
          style="width: 100%"
          @change="handleParentChange"
        />
        <el-alert
          v-if="depthWarning"
          :title="depthWarning"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 8px"
        />
      </el-form-item>

      <el-form-item label="菜单类型" prop="menuType">
        <el-radio-group v-model="formData.menuType">
          <el-radio :value="1">目录</el-radio>
          <el-radio :value="2">菜单</el-radio>
          <el-radio :value="3">按钮</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="菜单名称" prop="menuName">
        <el-input
          v-model="formData.menuName"
          placeholder="请输入菜单名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 目录和菜单类型显示图标 -->
      <el-form-item v-if="formData.menuType !== 3" label="图标" prop="icon">
        <IconSelect v-model="formData.icon" placeholder="请选择图标" clearable />
      </el-form-item>

      <!-- 菜单类型显示路由路径和组件路径 -->
      <el-form-item v-if="formData.menuType === 2" label="路由路径" prop="routePath">
        <el-input
          v-model="formData.routePath"
          placeholder="请输入路由路径（如：/system/user）"
          maxlength="200"
        />
      </el-form-item>

      <el-form-item v-if="formData.menuType === 2" label="组件路径" prop="component">
        <el-input
          v-model="formData.component"
          placeholder="请输入组件路径（如：system/user/index）"
          maxlength="200"
        />
      </el-form-item>

      <!-- 按钮类型显示权限标识 -->
      <el-form-item v-if="formData.menuType === 3" label="权限标识" prop="permission">
        <el-input
          v-model="formData.permission"
          placeholder="请输入权限标识（如：system:user:add）"
          maxlength="100"
        />
      </el-form-item>

      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 菜单类型显示是否可见 -->
      <el-form-item v-if="formData.menuType === 2" label="是否可见" prop="visible">
        <el-switch
          v-model="formData.visible"
          :active-value="1"
          :inactive-value="0"
          active-text="显示"
          inactive-text="隐藏"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-switch
          v-model="formData.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit"> 确定 </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createMenu, getMenuById, updateMenu, type MenuDTO, type MenuTreeVO } from '@/api/menu'
import IconSelect from '@/components/IconSelect/index.vue'

// Props
interface Props {
  visible: boolean
  menuId?: number
  menuTreeData: MenuTreeVO[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  menuId: undefined
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val)
})

// 对话框标题
const dialogTitle = computed(() => {
  return props.menuId ? '编辑菜单' : '新增菜单'
})

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<MenuDTO>({
  parentId: 0,
  menuName: '',
  menuType: 1,
  routePath: '',
  component: '',
  permission: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 1
})

// 菜单树选项（添加顶级菜单选项）
const menuTreeOptions = computed(() => {
  return [
    {
      id: 0,
      menuName: '顶级菜单',
      children: props.menuTreeData
    }
  ]
})

// 表单验证规则
const formRules = computed<FormRules>(() => ({
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 1, max: 50, message: '菜单名称长度必须在1-50之间', trigger: 'blur' }
  ],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  routePath: [
    {
      required: formData.menuType === 2,
      message: '请输入路由路径',
      trigger: 'blur'
    }
  ],
  permission: [
    {
      required: formData.menuType === 3,
      message: '请输入权限标识',
      trigger: 'blur'
    }
  ]
}))

// 提交加载状态
const submitLoading = ref(false)

// 层级深度警告
const depthWarning = ref('')

// 最大层级深度
const MAX_DEPTH = 3

// 计算菜单深度
const calculateMenuDepth = (parentId: number, menus: MenuTreeVO[]): number => {
  if (parentId === 0) return 1

  // 递归查找父菜单并计算深度
  const findDepth = (id: number, menuList: MenuTreeVO[], currentDepth: number): number => {
    for (const menu of menuList) {
      if (menu.id === id) {
        // 找到父菜单，继续向上查找
        if (menu.parentId === 0) {
          return currentDepth + 1
        }
        return findDepth(menu.parentId, props.menuTreeData, currentDepth + 1)
      }
      // 递归查找子菜单
      if (menu.children && menu.children.length > 0) {
        const depth = findDepth(id, menu.children, currentDepth)
        if (depth > 0) return depth
      }
    }
    return 0
  }

  return findDepth(parentId, menus, 1)
}

// 处理父菜单变更
const handleParentChange = (parentId: number) => {
  const depth = calculateMenuDepth(parentId, props.menuTreeData)
  if (depth >= MAX_DEPTH) {
    depthWarning.value = `当前选择的父菜单已达到第 ${depth - 1} 级，新增菜单将位于第 ${depth} 级。建议菜单层级不超过 ${MAX_DEPTH} 级。`
  } else {
    depthWarning.value = ''
  }
}

// 监听菜单类型变化，清空相关字段
watch(
  () => formData.menuType,
  (newType, oldType) => {
    if (newType !== oldType) {
      // 切换类型时清空不相关的字段
      if (newType === 1) {
        // 目录类型
        formData.routePath = ''
        formData.component = ''
        formData.permission = ''
        formData.visible = 1
      } else if (newType === 2) {
        // 菜单类型
        formData.permission = ''
      } else if (newType === 3) {
        // 按钮类型
        formData.routePath = ''
        formData.component = ''
        formData.icon = ''
        formData.visible = 1
      }
    }
  }
)

// 监听对话框显示状态，加载数据
watch(
  () => props.visible,
  visible => {
    if (visible && props.menuId) {
      fetchMenuData()
    }
  }
)

// 获取菜单数据（编辑模式）
const fetchMenuData = async () => {
  if (!props.menuId) return

  try {
    const menu: any = await getMenuById(props.menuId)

    // 填充表单数据
    formData.parentId = menu.parentId
    formData.menuName = menu.menuName
    formData.menuType = menu.menuType
    formData.routePath = menu.routePath || ''
    formData.component = menu.component || ''
    formData.permission = menu.permission || ''
    formData.icon = menu.icon || ''
    formData.sortOrder = menu.sortOrder
    formData.visible = menu.visible
    formData.status = menu.status
  } catch (error) {
    console.error('Failed to fetch menu data:', error)
    ElMessage.error('获取菜单数据失败')
  }
}

// 重置表单
const resetForm = () => {
  formData.parentId = 0
  formData.menuName = ''
  formData.menuType = 1
  formData.routePath = ''
  formData.component = ''
  formData.permission = ''
  formData.icon = ''
  formData.sortOrder = 0
  formData.visible = 1
  formData.status = 1
  depthWarning.value = ''
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    // 根据模式调用不同的 API
    if (props.menuId) {
      // 编辑模式
      await updateMenu(props.menuId, formData)
      ElMessage.success('更新菜单成功')
    } else {
      // 新增模式
      await createMenu(formData)
      ElMessage.success('新增菜单成功')
    }

    emit('success')
    handleClose()
  } catch (error: any) {
    if (error !== false) {
      // 不是表单验证错误
      console.error('Failed to save menu:', error)
      ElMessage.error(error.message || '保存菜单失败')
    }
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
:deep(.el-input-number) {
  width: 100%;
}
</style>
