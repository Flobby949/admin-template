<template>
  <div class="menu-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="菜单名称">
          <el-input
            v-model="queryParams.menuName"
            placeholder="请输入菜单名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 菜单列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>菜单列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <!-- 树形表格 -->
      <el-table
        v-loading="loading"
        :data="filteredMenuList"
        row-key="id"
        border
        stripe
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMenuTypeTag(row.menuType)">
              {{ getMenuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由路径" min-width="150" />
        <el-table-column prop="permission" label="权限标识" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="visible" label="可见" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 菜单编辑对话框 -->
    <MenuDialog
      v-model:visible="dialogVisible"
      :menu-id="currentMenuId"
      :menu-tree-data="menuList"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { listMenuTree, deleteMenu, updateMenuStatus, type MenuTreeVO } from '@/api/menu'
import MenuDialog from './components/MenuDialog.vue'

// 查询参数
const queryParams = reactive({
  menuName: ''
})

// 状态
const loading = ref(false)
const menuList = ref<MenuTreeVO[]>([])
const dialogVisible = ref(false)
const currentMenuId = ref<number | undefined>(undefined)

// 菜单类型映射
const menuTypeMap: Record<number, { label: string; tag: string }> = {
  1: { label: '目录', tag: 'primary' },
  2: { label: '菜单', tag: 'success' },
  3: { label: '按钮', tag: 'warning' }
}

const getMenuTypeLabel = (type: number) => menuTypeMap[type]?.label || '未知'
const getMenuTypeTag = (type: number) => menuTypeMap[type]?.tag || 'info'

// 过滤菜单树（前端搜索）
const filterMenuTree = (menus: MenuTreeVO[], keyword: string): MenuTreeVO[] => {
  if (!keyword) return menus

  const result: MenuTreeVO[] = []
  for (const menu of menus) {
    if (menu.menuName.includes(keyword)) {
      // 如果当前节点匹配，包含所有子节点
      result.push(menu)
    } else if (menu.children && menu.children.length > 0) {
      // 如果当前节点不匹配，递归过滤子节点
      const filteredChildren = filterMenuTree(menu.children, keyword)
      if (filteredChildren.length > 0) {
        result.push({
          ...menu,
          children: filteredChildren
        })
      }
    }
  }
  return result
}

// 计算过滤后的菜单列表
const filteredMenuList = computed(() => {
  return filterMenuTree(menuList.value, queryParams.menuName)
})

// 获取菜单列表
const fetchMenuList = async () => {
  loading.value = true
  try {
    const res: any = await listMenuTree()
    menuList.value = res || []
  } catch (error) {
    console.error('Failed to fetch menu list:', error)
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  // 前端过滤，无需重新请求
}

// 重置
const resetQuery = () => {
  queryParams.menuName = ''
}

// 打开新增对话框
const handleAdd = () => {
  currentMenuId.value = undefined
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row: MenuTreeVO) => {
  currentMenuId.value = row.id
  dialogVisible.value = true
}

// 删除菜单
const handleDelete = async (row: MenuTreeVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单"${row.menuName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMenu(row.id)
    ElMessage.success('删除菜单成功')
    fetchMenuList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete menu:', error)
      // 显示后端返回的错误提示
      const errorMessage = error.response?.data?.message || error.message || '删除菜单失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 切换菜单状态
const handleStatusChange = async (row: MenuTreeVO) => {
  const oldStatus = row.status === 1 ? 0 : 1
  try {
    await updateMenuStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    console.error('Failed to update menu status:', error)
    // 恢复原状态
    row.status = oldStatus
    ElMessage.error(error.message || '状态更新失败')
  }
}

// 对话框成功回调
const handleDialogSuccess = () => {
  fetchMenuList()
}

onMounted(() => {
  fetchMenuList()
})
</script>

<style scoped>
.menu-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
