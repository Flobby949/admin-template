<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-content">
      <div class="login-branding">
        <div class="logo-icon">
          <el-icon :size="32"><ElementPlus /></el-icon>
        </div>
        <h1>Admin System</h1>
        <p>新一代企业级管理平台</p>
      </div>

      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p class="subtitle">请登录您的账户继续</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          size="large"
          class="login-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item class="login-actions">
            <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="hint">默认账号: admin / admin123</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { User, Lock, ElementPlus } from '@element-plus/icons-vue'
import 'element-plus/theme-chalk/el-message.css'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        const redirect = (route.query.redirect as string) || '/'
        router.push(redirect)
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 50%, #1e1b4b 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
  }

  .circle-1 {
    top: -10%;
    right: -5%;
    width: 400px;
    height: 400px;
    background: rgba(37, 99, 235, 0.3);
  }

  .circle-2 {
    bottom: -15%;
    left: -10%;
    width: 500px;
    height: 500px;
    background: rgba(139, 92, 246, 0.25);
  }

  .circle-3 {
    top: 40%;
    left: 30%;
    width: 300px;
    height: 300px;
    background: rgba(59, 130, 246, 0.15);
  }
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-branding {
  text-align: center;
  margin-bottom: 32px;
  color: white;

  .logo-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--primary-color), #3b82f6);
    border-radius: 16px;
    color: white;
    box-shadow: 0 8px 32px rgba(37, 99, 235, 0.4);
  }

  h1 {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 8px;
    background: linear-gradient(to right, #60a5fa, #a78bfa);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    color: #94a3b8;
    font-size: 1rem;
  }
}

.login-card {
  width: 100%;
  border-radius: var(--radius-xl);
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-main);
    margin-bottom: 8px;
  }

  .subtitle {
    color: var(--text-secondary);
    font-size: 0.9rem;
  }
}

.login-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--text-regular);
  }

  :deep(.el-input__wrapper) {
    padding: 4px 12px;
  }

  :deep(.el-input__inner) {
    height: 44px;
  }
}

.login-actions {
  margin-top: 8px;
  margin-bottom: 0;
}

.login-button {
  width: 100%;
  height: 48px;
  font-weight: 600;
  font-size: 1rem;
  border-radius: var(--radius-base);
  background: linear-gradient(135deg, var(--primary-color), #3b82f6);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, var(--primary-hover), #2563eb);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(37, 99, 235, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  margin-top: 24px;
  text-align: center;

  .hint {
    font-size: 13px;
    color: var(--text-placeholder);
  }
}

/* 响应式 */
@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }

  .login-branding h1 {
    font-size: 1.75rem;
  }
}
</style>
