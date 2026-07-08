<template>
  <div class="app">
    <div class="content">
      <router-view />
    </div>

    <van-tabbar v-model="active" route class="custom-tabbar">
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="description-o" to="/course">课程</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/ai">AI</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">个人主页</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberStore } from '@/stores/user'

const active = ref(0)
const router = useRouter()
const store = useMemberStore()
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  background: #f5f7fa;
}

.app {
  padding-bottom: 50px;
  min-height: 100vh;
  position: relative;
}

.content {
  padding: 12px;
}

/* 底部导航：橙色主题定制 */
:deep(.custom-tabbar) {
  background: #ffffff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  border-top: none;
  padding-top: 4px;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 选中态：统一主题橙色 */
:deep(.custom-tabbar .van-tabbar-item--active) {
  color: #1a73e8;
}

:deep(.custom-tabbar .van-tabbar-item__icon) {
  font-size: 20px;
  margin-bottom: 2px;
}

:deep(.custom-tabbar .van-tabbar-item__text) {
  font-size: 12px;
}
</style>