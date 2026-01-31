<template>
  <div class="dashboard-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Dashboard</h1>
        <p class="page-subtitle">系统概览与数据统计</p>
      </div>
      <div class="header-right">
        <span class="update-time">最后更新: {{ currentTime }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col v-for="(card, index) in statsCards" :key="index" :xs="12" :sm="12" :md="6">
        <div class="stat-card" :style="{ '--accent-color': card.color }">
          <div class="stat-icon" :style="{ background: card.bg }">
            <el-icon :color="card.color" :size="24">
              <component :is="card.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <div class="stat-trend" :class="card.trend > 0 ? 'up' : 'down'">
            <el-icon><component :is="card.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
            <span>{{ Math.abs(card.trend) }}%</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :md="16">
        <div class="chart-card">
          <div class="card-header">
            <h3>收入趋势</h3>
            <el-radio-group v-model="chartPeriod" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-container">
            <div class="chart-placeholder">
              <div
                v-for="(bar, i) in chartData"
                :key="i"
                class="bar"
                :style="{ height: bar.value + '%' }"
              >
                <span class="bar-label">{{ bar.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :md="8">
        <div class="activity-card">
          <div class="card-header">
            <h3>最近活动</h3>
            <el-button type="primary" link size="small">查看全部</el-button>
          </div>
          <ul class="activity-list">
            <li v-for="(activity, i) in activities" :key="i">
              <div class="activity-dot" :style="{ background: activity.color }"></div>
              <div class="activity-content">
                <span class="activity-text">{{ activity.text }}</span>
                <span class="activity-time">{{ activity.time }}</span>
              </div>
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions-row">
      <el-col :span="24">
        <div class="quick-actions-card">
          <div class="card-header">
            <h3>快捷操作</h3>
          </div>
          <div class="actions-grid">
            <router-link
              v-for="action in quickActions"
              :key="action.path"
              :to="action.path"
              class="action-item"
            >
              <div class="action-icon" :style="{ background: action.bg }">
                <el-icon :color="action.color" :size="20">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <span class="action-label">{{ action.label }}</span>
            </router-link>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  User,
  View,
  Money,
  Goods,
  Top,
  Bottom,
  Setting,
  Document,
  UserFilled,
  List
} from '@element-plus/icons-vue'

// Register dynamic components for template usage
const _dynamicIcons = { Top, Bottom }

// 当前时间
const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 统计卡片数据
const statsCards = [
  { label: '总用户数', value: '1,234', icon: User, color: '#2563EB', bg: '#EFF6FF', trend: 12 },
  { label: '访问量', value: '45.2k', icon: View, color: '#8B5CF6', bg: '#F5F3FF', trend: 8 },
  { label: '总收入', value: '¥12,340', icon: Money, color: '#10B981', bg: '#ECFDF5', trend: -3 },
  { label: '订单数', value: '892', icon: Goods, color: '#F59E0B', bg: '#FFFBEB', trend: 15 }
]

// 图表周期
const chartPeriod = ref('month')

// 图表数据
const chartData = [
  { label: '一月', value: 40 },
  { label: '二月', value: 70 },
  { label: '三月', value: 50 },
  { label: '四月', value: 90 },
  { label: '五月', value: 60 },
  { label: '六月', value: 80 },
  { label: '七月', value: 45 }
]

// 活动列表
const activities = [
  { text: '用户 admin 登录系统', time: '2分钟前', color: '#10B981' },
  { text: '新增用户 zhangsan', time: '15分钟前', color: '#2563EB' },
  { text: '角色权限已更新', time: '1小时前', color: '#F59E0B' },
  { text: '系统配置已修改', time: '2小时前', color: '#8B5CF6' },
  { text: '数据备份完成', time: '3小时前', color: '#10B981' }
]

// 快捷操作
const quickActions = [
  { label: '用户管理', path: '/system/user', icon: UserFilled, color: '#2563EB', bg: '#EFF6FF' },
  { label: '角色管理', path: '/system/role', icon: User, color: '#8B5CF6', bg: '#F5F3FF' },
  { label: '菜单管理', path: '/system/menu', icon: List, color: '#10B981', bg: '#ECFDF5' },
  { label: '系统设置', path: '/system/dict', icon: Setting, color: '#F59E0B', bg: '#FFFBEB' },
  { label: '操作日志', path: '/monitor/operlog', icon: Document, color: '#EF4444', bg: '#FEF2F2' }
]
</script>

<style scoped lang="scss">
.dashboard-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .header-left {
    .page-title {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-main);
      margin: 0 0 4px 0;
    }

    .page-subtitle {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .header-right {
    .update-time {
      font-size: 13px;
      color: var(--text-placeholder);
    }
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: var(--transition-base);
  margin-bottom: 20px;

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-2px);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-base);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    flex: 1;

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-main);
      line-height: 1.2;
    }

    .stat-label {
      font-size: 13px;
      color: var(--text-secondary);
      margin-top: 2px;
    }
  }

  .stat-trend {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    font-weight: 500;

    &.up {
      color: #10b981;
    }

    &.down {
      color: #ef4444;
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card,
.activity-card,
.quick-actions-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  padding: 24px;
  height: 100%;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-main);
      margin: 0;
    }
  }
}

.chart-card {
  min-height: 400px;
  margin-bottom: 20px;

  .chart-container {
    height: 300px;
  }

  .chart-placeholder {
    height: 100%;
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    background: var(--bg-body);
    border-radius: var(--radius-base);
    padding: 20px;

    .bar {
      width: 40px;
      background: linear-gradient(180deg, var(--primary-color), #60a5fa);
      border-radius: 4px 4px 0 0;
      transition: height 0.5s ease;
      position: relative;
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }

      .bar-label {
        position: absolute;
        bottom: -24px;
        left: 50%;
        transform: translateX(-50%);
        font-size: 12px;
        color: var(--text-secondary);
        white-space: nowrap;
      }
    }
  }
}

.activity-card {
  margin-bottom: 20px;
}

.activity-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }

    .activity-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      margin-top: 6px;
      flex-shrink: 0;
    }

    .activity-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .activity-text {
        font-size: 14px;
        color: var(--text-main);
      }

      .activity-time {
        font-size: 12px;
        color: var(--text-placeholder);
      }
    }
  }
}

.quick-actions-card {
  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 16px;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px;
    border-radius: var(--radius-base);
    text-decoration: none;
    transition: var(--transition-fast);

    &:hover {
      background: var(--bg-hover);
    }

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-base);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .action-label {
      font-size: 13px;
      color: var(--text-regular);
      font-weight: 500;
    }
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 8px;
  }

  .stat-card {
    padding: 16px;

    .stat-info .stat-value {
      font-size: 20px;
    }
  }

  .chart-card .chart-placeholder .bar {
    width: 24px;

    .bar-label {
      font-size: 10px;
    }
  }
}
</style>
