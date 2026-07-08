<template>
  <div class="login-page">
    <!-- 背景暗色渐变 + 顶部光晕 -->
    <div class="bg-glow"></div>

    <div class="login-card">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="brand-icon">
          <van-icon name="smile-o" size="30" color="#1a73e8" />
        </div>
        <h1 class="brand-name">GYM</h1>
        <p class="brand-tag">训练，从这里开始</p>
      </div>

      <!-- 登录 / 注册切换 -->
      <div class="switch-bar">
        <span
          :class="['switch-item', { active: tab === 0 }]"
          @click="tab = 0"
        >登录</span>
        <span
          :class="['switch-item', { active: tab === 1 }]"
          @click="tab = 1"
        >注册</span>
      </div>

      <!-- 登录表单 -->
      <van-form v-if="tab === 0" @submit="handleLogin" class="form-box">
        <div class="input-group">
          <van-icon name="user-o" class="input-icon" />
          <input
            v-model="loginForm.username"
            class="text-input"
            placeholder="用户名"
            autocomplete="username"
          />
        </div>
        <div class="input-group">
          <van-icon name="lock-o" class="input-icon" />
          <input
            v-model="loginForm.password"
            :type="loginPwdVisible ? 'text' : 'password'"
            class="text-input"
            placeholder="密码"
            autocomplete="current-password"
          />
          <van-icon
            :name="loginPwdVisible ? 'eye-o' : 'closed-eye'"
            class="eye-icon"
            @click="loginPwdVisible = !loginPwdVisible"
          />
        </div>
        <van-button round block native-type="submit" :loading="loginLoading" class="submit-btn">
          登 录
        </van-button>
      </van-form>

      <!-- 注册表单 -->
      <van-form v-if="tab === 1" @submit="handleRegister" class="form-box">
        <div class="input-group">
          <van-icon name="user-o" class="input-icon" />
          <input
            v-model="regForm.username"
            class="text-input"
            placeholder="用户名（3-50位）"
            autocomplete="username"
          />
        </div>
        <div class="input-group">
          <van-icon name="label-o" class="input-icon" />
          <input
            v-model="regForm.realName"
            class="text-input"
            placeholder="姓名"
            autocomplete="name"
          />
        </div>
        <div class="input-group">
          <van-icon name="lock-o" class="input-icon" />
          <input
            v-model="regForm.password"
            :type="regPwdVisible ? 'text' : 'password'"
            class="text-input"
            placeholder="密码（6-50位）"
          />
          <van-icon
            :name="regPwdVisible ? 'eye-o' : 'closed-eye'"
            class="eye-icon"
            @click="regPwdVisible = !regPwdVisible"
          />
        </div>
        <div class="input-group">
          <van-icon name="phone-o" class="input-icon" />
          <input
            v-model="regForm.phone"
            class="text-input"
            placeholder="手机号"
            autocomplete="tel"
          />
        </div>
        <van-button round block native-type="submit" :loading="regLoading" class="submit-btn">
          注 册
        </van-button>
      </van-form>

      <p class="footer-tip">默认账号：member01 / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberStore } from '@/stores/user'
import request from '@/api/request'
import { showNotify } from 'vant'

const router = useRouter()
const store = useMemberStore()
const tab = ref(0)

const loginPwdVisible = ref(false)
const regPwdVisible = ref(false)
const loginLoading = ref(false)
const regLoading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', phone: '', realName: '' })

async function handleLogin() {
  loginLoading.value = true
  try {
    const res = await request.post('/api/v1/auth/login', loginForm)
    store.setToken(res.data.token)
    store.setUserInfo(res.data)
    showNotify({ type: 'success', message: '登录成功' })
    router.push('/home')
  } catch (err) {
    const msg = err.response?.data?.msg || '登录失败，请检查账号密码'
    showNotify({ type: 'danger', message: msg })
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  regLoading.value = true
  try {
    await request.post('/api/v1/auth/register', regForm)
    showNotify({ type: 'success', message: '注册成功，请登录' })
    tab.value = 0
    regForm.username = ''
    regForm.password = ''
    regForm.phone = ''
    regForm.realName = ''
  } catch (err) {
    const msg = err.response?.data?.msg || '注册失败，请检查信息'
    showNotify({ type: 'danger', message: msg })
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
/* ====== 页面容器：暗色背景 ====== */
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0b0b14;
  overflow: hidden;
  padding: env(safe-area-inset-top) 20px env(safe-area-inset-bottom);
  box-sizing: border-box;
}

/* 顶部暖色光晕 */
.bg-glow {
  position: absolute;
  top: -120px;
  left: 50%;
  transform: translateX(-50%);
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(26,115,232,0.18) 0%, transparent 70%);
  pointer-events: none;
}

/* ====== 卡片 ====== */
.login-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 360px;
  padding: 40px 28px 28px;
  background: rgba(22,22,38,0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.06);
  box-shadow: 0 24px 64px rgba(0,0,0,0.5);
}

/* ====== 品牌区 ====== */
.brand {
  text-align: center;
  margin-bottom: 28px;
}
.brand-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(26,115,232,0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
}
.brand-name {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  letter-spacing: 6px;
}
.brand-tag {
  font-size: 13px;
  color: rgba(255,255,255,0.4);
  margin: 6px 0 0;
  letter-spacing: 1px;
}

/* ====== 切换栏 ====== */
.switch-bar {
  display: flex;
  margin-bottom: 24px;
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  padding: 4px;
}
.switch-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 15px;
  font-weight: 500;
  color: rgba(255,255,255,0.4);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.switch-item.active {
  background: #1a73e8;
  color: #fff;
  box-shadow: 0 4px 12px rgba(26,115,232,0.35);
}

/* ====== 表单 ====== */
.form-box {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.input-group {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 12px;
  padding: 0 14px;
  height: 50px;
  transition: border-color 0.25s;
}
.input-group:focus-within {
  border-color: rgba(26,115,232,0.5);
  background: rgba(255,255,255,0.07);
}
.input-icon {
  font-size: 18px;
  color: rgba(255,255,255,0.3);
  flex-shrink: 0;
  margin-right: 10px;
}
.text-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: #fff;
  font-size: 15px;
}
.text-input::placeholder {
  color: rgba(255,255,255,0.25);
}
/* 自动填充：背景色与正常输入完全一致 */
.text-input:-webkit-autofill,
.text-input:-webkit-autofill:hover,
.text-input:-webkit-autofill:focus {
  background: transparent !important;
  -webkit-text-fill-color: #fff !important;
  caret-color: #fff;
  transition: background-color 9999s ease-in-out 0s;
}
.eye-icon {
  font-size: 18px;
  color: rgba(255,255,255,0.3);
  cursor: pointer;
  flex-shrink: 0;
}
.eye-icon:active {
  color: #1a73e8;
}

/* ====== 提交按钮 ====== */
.submit-btn {
  margin-top: 6px;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #1a73e8 0%, #4a9af5 100%) !important;
  color: #fff !important;
  border: none !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(26,115,232,0.3);
  transition: all 0.3s ease;
}
.submit-btn:active {
  transform: scale(0.97);
  box-shadow: 0 4px 12px rgba(26,115,232,0.2);
}

/* ====== 底部 ====== */
.footer-tip {
  text-align: center;
  color: rgba(255,255,255,0.2);
  font-size: 12px;
  margin: 24px 0 0;
}

/* ====== 响应式 ====== */
@media (max-width: 380px) {
  .login-card {
    padding: 32px 20px 22px;
    border-radius: 20px;
  }
  .brand-name {
    font-size: 24px;
  }
  .input-group {
    height: 46px;
  }
  .submit-btn {
    height: 46px;
    font-size: 15px;
  }
}
</style>
