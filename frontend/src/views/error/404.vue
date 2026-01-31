<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-icon">
        <el-icon :size="120" color="#409EFF">
          <WarningFilled />
        </el-icon>
      </div>
      <h1 class="error-title">404</h1>
      <p class="error-message">抱歉，您访问的页面不存在</p>
      <p class="error-description">请检查您输入的网址是否正确，或者点击下面的按钮返回首页</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome"> 返回首页 </el-button>
        <el-button size="large" @click="goBack"> 返回上一页 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  padding: 40px 20px;

  .error-content {
    text-align: center;
    max-width: 600px;

    .error-icon {
      margin-bottom: 30px;
      animation: bounce 2s infinite;
    }

    .error-title {
      font-size: 72px;
      font-weight: bold;
      color: #409eff;
      margin: 0 0 20px;
      line-height: 1;
    }

    .error-message {
      font-size: 24px;
      color: #303133;
      margin: 0 0 15px;
    }

    .error-description {
      font-size: 16px;
      color: #606266;
      margin: 0 0 40px;
      line-height: 1.6;
    }

    .error-actions {
      display: flex;
      gap: 15px;
      justify-content: center;
    }
  }
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}
</style>
