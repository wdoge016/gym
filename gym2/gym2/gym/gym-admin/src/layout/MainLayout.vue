<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="24" color="#1a73e8"><TrendCharts /></el-icon>
        <span>健身房管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#ffffff"
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/members">
          <el-icon><User /></el-icon>
          <span>会员管理</span>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/employees">
           <el-icon><UserFilled /></el-icon>
            <span>员工管理</span>
          </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/coaches">
          <el-icon><Avatar /></el-icon>
          <span>教练管理</span>
        </el-menu-item>
        <el-menu-item index="/equipment">
           <el-icon><SetUp /></el-icon>
            <span>器材管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <el-icon><Calendar /></el-icon>
          <span>排课管理</span>
        </el-menu-item>
        <el-menu-item index="/reservations">
          <el-icon><Tickets /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/orders">
          <el-icon><ShoppingCart /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/cards">
          <el-icon><CreditCard /></el-icon>
          <span>会员卡</span>
        </el-menu-item>
        <el-menu-item index="/attendance">
          <el-icon><AlarmClock /></el-icon>
          <span>考勤管理</span>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-tag :type="roleTagType" class="role-tag">{{ roleLabel }}</el-tag>
          <span class="user-name">{{ userStore.userInfo?.username || '管理员' }}</span>
          <el-button type="danger" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const role = computed(() => userStore.userInfo?.role || 'STAFF')
const isAdmin = computed(() => role.value === 'SUPER_ADMIN' || role.value === 'STORE_ADMIN')
const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')
const roleLabel = computed(() => {
  const map = { SUPER_ADMIN: '超管', STORE_ADMIN: '店长', COACH: '教练', STAFF: '前台' }
  return map[role.value] || role.value
})
const roleTagType = computed(() => role.value === 'SUPER_ADMIN' ? 'danger' : role.value === 'STORE_ADMIN' ? 'warning' : role.value === 'COACH' ? 'success' : '')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }

/* 侧边栏：浅色背景 */
.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.logo {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a73e8;
  font-size: 22px;
  font-weight: bold;
  gap: 10px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.logo .el-icon {
  font-size: 30px;
}

/* 菜单选中后橙色高亮 */
.side-menu {
  border-right: none;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #1a73e8 0%, #4a9af5 100%) !important;
  color: #ffffff !important;
}
:deep(.el-menu-item:hover) {
  background-color: #e8f0fe !important;
  color: #1a73e8 !important;
}
:deep(.el-menu-item.is-active:hover) {
  color: #ffffff !important;
}

/* 顶部头部 */
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-bottom: 1px solid #e6e6e6;
  height: 64px;
  padding: 0 24px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 角色标签放大 */
.role-tag {
  font-size: 14px;
  padding: 5px 14px;
  height: auto;
  border-radius: 6px;
}

/* 用户名放大 */
.user-name {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

/* 退出登录按钮放大 */
.logout-btn {
  height: 38px;
  padding: 0 22px;
  font-size: 15px;
  border-radius: 8px;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>