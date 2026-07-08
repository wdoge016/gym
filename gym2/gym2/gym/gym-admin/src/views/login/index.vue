<template>
  <div class="login-container">
    <!-- 全屏健身场馆背景图 -->
    <div class="bg-image"></div>
    <!-- 半透明遮罩 提升表单可读性 -->
    <div class="bg-mask"></div>

    <div class="login-card">
      <h2>智能健身房管理系统</h2>
      <p class="subtitle">员工后台登录</p>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="手机号/姓名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="tip">默认管理员：13800000001 / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await adminLogin(form)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    const msg = err.response?.data?.msg || '登录失败，请检查账号密码'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #0d0d1a;
}

/* 健身场馆实景背景 —— 高清图片，四周留白 */
.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1920') no-repeat center center;
  background-size: contain;
  background-color: #0d0d1a;
  filter: brightness(0.75) contrast(1.05);
  z-index: 1;
}

/* 遮罩进一步减弱，让背景更透 */
.bg-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.08);
  z-index: 2;
}

/* ===== 卡片：超高透 + 毛玻璃，背景几乎完全可见 ===== */
.login-card {
  position: relative;
  z-index: 3;
  width: min(640px, 92%);
  padding: 52px 48px 44px;
  /* 透明度极低的橙色渐变，几乎只保留色调氛围 */
  background: linear-gradient(150deg, rgba(26, 115, 232, 0.18) 0%, rgba(74, 154, 245, 0.22) 35%, rgba(100, 180, 255, 0.28) 65%, rgba(160, 210, 255, 0.30) 100%);
  backdrop-filter: blur(8px) saturate(1.1);
  -webkit-backdrop-filter: blur(8px) saturate(1.1);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow:
    0 30px 80px rgba(0, 0, 0, 0.30),
    0 0 0 1px rgba(255, 255, 255, 0.04) inset;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.4s ease;
}

.login-card:hover {
  transform: translateY(-4px) scale(1.004);
  box-shadow:
    0 40px 100px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 4px;
  color: #ffffff;
  font-weight: 700;
  font-size: 28px;
  letter-spacing: 1.5px;
  /* 加深文字阴影，在透明卡片上依然清晰 */
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.60), 0 0 60px rgba(0, 0, 0, 0.30);
}

.subtitle {
  text-align: center;
  color: rgba(255, 255, 255, 0.92);
  margin-bottom: 34px;
  font-size: 16px;
  letter-spacing: 1px;
  font-weight: 400;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.50);
}

/* ===== 表单输入框 ===== */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 0 transparent;
  background: rgba(255, 255, 255, 0.75);
  transition: box-shadow 0.25s ease, background 0.25s ease, transform 0.2s ease;
  padding: 0 18px;
  height: 50px;
  backdrop-filter: blur(4px);
}

:deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.88);
  transform: translateY(-1px);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.25), 0 4px 16px rgba(0, 0, 0, 0.10);
  background: rgba(255, 255, 255, 0.92);
  transform: translateY(-2px);
}

:deep(.el-input__inner) {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

:deep(.el-input__prefix) {
  margin-right: 10px;
  color: #1a5cb0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* ===== 登录按钮 ===== */
.login-btn {
  width: 100%;
  height: 54px;
  font-size: 18px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.85);
  color: #1a5cb0;
  border: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  letter-spacing: 3px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(4px);
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.96);
  transform: translateY(-3px) scale(1.01);
  box-shadow: 0 10px 36px rgba(0, 0, 0, 0.18);
}

.login-btn:active {
  transform: translateY(0) scale(0.97);
}

.login-btn.is-loading {
  background: rgba(255, 255, 255, 0.85);
  color: #1a5cb0;
}

/* ===== 底部提示 ===== */
.tip {
  text-align: center;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  margin-top: 24px;
  letter-spacing: 0.5px;
  background: rgba(0, 0, 0, 0.20);
  padding: 8px 20px;
  border-radius: 30px;
  display: inline-block;
  width: auto;
  margin-left: auto;
  margin-right: auto;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  text-shadow: 0 1px 12px rgba(0, 0, 0, 0.40);
}

/* ===== 响应式适配 ===== */
@media (max-width: 700px) {
  .login-card {
    padding: 40px 28px 34px;
    width: min(540px, 94%);
    backdrop-filter: blur(6px);
    -webkit-backdrop-filter: blur(6px);
  }

  .login-card h2 {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
    margin-bottom: 28px;
  }

  :deep(.el-input__wrapper) {
    height: 46px;
  }

  :deep(.el-input__inner) {
    height: 46px;
    line-height: 46px;
    font-size: 15px;
  }

  .login-btn {
    height: 48px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 20px 28px;
    width: 96%;
    border-radius: 16px;
    backdrop-filter: blur(6px);
    -webkit-backdrop-filter: blur(6px);
  }

  .login-card h2 {
    font-size: 20px;
    letter-spacing: 0.5px;
  }

  .subtitle {
    font-size: 13px;
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    height: 42px;
    padding: 0 14px;
  }

  :deep(.el-input__inner) {
    height: 42px;
    line-height: 42px;
    font-size: 14px;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .login-btn {
    height: 44px;
    font-size: 15px;
    letter-spacing: 2px;
  }

  .tip {
    font-size: 12px;
    padding: 6px 14px;
    margin-top: 18px;
  }
}

@media (max-width: 380px) {
  .login-card {
    padding: 24px 14px 20px;
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
  }

  .login-card h2 {
    font-size: 18px;
  }

  .subtitle {
    font-size: 12px;
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    height: 38px;
    padding: 0 10px;
  }

  :deep(.el-input__inner) {
    height: 38px;
    line-height: 38px;
    font-size: 13px;
  }

  .login-btn {
    height: 40px;
    font-size: 14px;
  }
}
</style>