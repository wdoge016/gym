<template>
  <div class="profile-page">
    <!-- 和首页一致全局居中容器 -->
    <div class="page-inner">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="avatar">
          <van-icon name="user-o" size="28" />
        </div>
        <div class="user-info">
          <div class="user-name">{{ userInfo.realName || userInfo.username || '会员用户' }}</div>
          <div class="user-phone">{{ userInfo.phone || '未绑定手机号' }}</div>
          <van-tag size="small" class="member-tag">健身会员</van-tag>
        </div>
        <van-button size="small" plain type="primary" @click="openEdit">编辑</van-button>
      </div>
      <!-- 功能菜单 -->
      <van-cell-group inset class="menu-group">
        <van-cell title="我的课程预约" is-link to="/reservations" />
        <van-cell title="我的会员卡" is-link to="/card" />
        <van-cell title="我的教练预约" is-link @click="showCoachBookings = true" />
        <van-cell title="修改密码" is-link @click="openPwdDialog" />
        <van-cell title="关于我们" is-link @click="showAboutDialog = true" />
      </van-cell-group>
      <!-- 退出登录按钮 -->
      <van-button
        block
        type="danger"
        class="logout-btn"
        @click="handleLogout"
      >
        退出登录
      </van-button>
      <!-- 教练预约列表弹窗 -->
      <van-popup v-model:show="showCoachBookings" position="bottom" :style="{ height: '60%' }" round>
        <div class="coach-bookings-wrap">
          <h4 class="coach-booking-title">我的教练预约</h4>
          <van-empty v-if="coachBookings.length === 0" description="暂无教练预约" />
          <van-cell
            v-for="b in coachBookings"
            :key="b.id"
            :title="b.coachName || ('教练ID: ' + b.coachId)"
            :label="(b.appointmentDate || '') + ' ' + (b.startTime || '') + '-' + (b.endTime || '')"
            :value="b.status === 'BOOKED' ? '已约' : b.status === 'CANCELLED' ? '已取消' : '已完成'"
          />
        </div>
      </van-popup>
      <!-- 编辑资料弹窗 -->
      <van-dialog
        v-model:show="editVisible"
        title="编辑个人资料"
        show-cancel-button
        @confirm="handleSave"
      >
        <van-form class="edit-form">
          <van-field
            v-model="editForm.realName"
            label="姓名"
            placeholder="请输入真实姓名"
            clearable
          />
          <van-field
            v-model="editForm.phone"
            label="手机号"
            placeholder="请输入手机号"
            clearable
          />
          <div class="gender-row">
            <span class="gender-label">性别</span>
            <van-radio-group v-model="editForm.gender" direction="horizontal">
              <van-radio :name="1">男</van-radio>
              <van-radio :name="2">女</van-radio>
            </van-radio-group>
          </div>
        </van-form>
      </van-dialog>
      <!-- 修改密码弹窗 -->
      <van-dialog
        v-model:show="pwdVisible"
        title="修改密码"
        show-cancel-button
        @confirm="handleChangePwd"
      >
        <van-form class="edit-form">
          <van-field
            v-model="pwdForm.oldPassword"
            type="password"
            label="原密码"
            placeholder="请输入原密码"
            clearable
          />
          <van-field
            v-model="pwdForm.newPassword"
            type="password"
            label="新密码"
            placeholder="请输入新密码（6-20位）"
            clearable
          />
          <van-field
            v-model="pwdForm.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入新密码"
            clearable
          />
        </van-form>
      </van-dialog>
      <!-- 关于我们弹窗 -->
      <van-dialog
        v-model:show="showAboutDialog"
        title="关于我们"
        closeable
        show-footer
        width="85%"
      >
        <div class="about-content">
          <p>智能健身房管理系统，致力于为广大健身爱好者提供便捷、专业的健身服务。</p>
          <p>场馆配备专业健身器械、持证健身教练，提供私教课程、团体课、预约健身等一站式服务。</p>
          <p>我们以科学健身、贴心服务为宗旨，陪伴每一位用户完成健身目标。</p>
        </div>
        <template #footer>
          <van-button block type="primary" @click="showAboutDialog = false">确认</van-button>
        </template>
      </van-dialog>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMemberStore } from '@/stores/user'
import { showToast, showDialog } from 'vant'
import request from '@/api/request'
const router = useRouter()
const store = useMemberStore()
const userInfo = computed(() => store.userInfo || {})
const editVisible = ref(false)
const showAboutDialog = ref(false)
const showCoachBookings = ref(false)
const coachBookings = ref([])
const pwdVisible = ref(false)
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const editForm = ref({
  realName: '',
  phone: '',
  gender: 1
})
onMounted(async () => {
  // 从后端拉取完整用户信息（含手机号、性别等登录时未返回的字段）
  if (store.token) {
    try {
      const res = await request.get('/api/v1/auth/userinfo')
      store.setUserInfo({ ...store.userInfo, ...res.data })
    } catch { /* 网络异常忽略 */ }
  }
})

// 打开教练预约时拉取数据
watch(showCoachBookings, async (val) => {
  if (val) {
    try {
      const res = await request.get('/api/v1/coach-bookings/my')
      coachBookings.value = res.data || []
    } catch { /* 静默 */ }
  }
})
function openEdit() {
  editForm.value = {
    realName: userInfo.value.realName || '',
    phone: userInfo.value.phone || '',
    gender: userInfo.value.gender || 1,
  }
  editVisible.value = true
}

async function handleSave() {
  if (!editForm.value.realName.trim()) {
    showToast('请输入姓名')
    return false
  }
  if (!editForm.value.phone.trim()) {
    showToast('请输入手机号')
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(editForm.value.phone)) {
    showToast('请输入正确的手机号')
    return false
  }
  try {
    await request.put(`/api/v1/members/${userInfo.value.id}`, editForm.value)
    showToast('修改成功')
    store.setUserInfo({ ...userInfo.value, ...editForm.value })
    editVisible.value = false
  } catch (err) {
    const msg = err.response?.data?.msg || '修改失败'
    showToast(msg)
    return false
  }
}
function openPwdDialog() {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdVisible.value = true
}

async function handleChangePwd() {
  const { oldPassword, newPassword, confirmPassword } = pwdForm.value
  if (!oldPassword) { showToast('请输入原密码'); return false }
  if (!newPassword || newPassword.length < 6) { showToast('新密码至少6位'); return false }
  if (newPassword !== confirmPassword) { showToast('两次密码不一致'); return false }
  try {
    await request.put('/api/v1/auth/password', { oldPassword, newPassword })
    pwdVisible.value = false
    await showDialog({
      title: '提示',
      message: '密码修改成功，请重新登录',
      confirmButtonText: '确定'
    })
    store.logout()
    router.replace('/login')
  } catch (err) {
    const msg = err.response?.data?.msg || err.response?.data?.message || '修改失败，请重试'
    showDialog({ title: '修改失败', message: msg, confirmButtonText: '确定' })
    return false
  }
}

function handleLogout() {
  store.logout()
  router.replace('/login')
}
</script>
<style scoped>
/* 和首页完全统一页面背景 */
.profile-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}
/* 全局居中容器 同步首页max-width */
.page-inner {
  max-width: 520px;
  margin: 0 auto;
}
/* 卡片统一首页圆角、阴影、内边距 */
.profile-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 16px;
}
/* 头像 */
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8 0%, #4a9af5 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #222;
}
.user-phone {
  font-size: 14px;
  color: #888;
}
.member-tag {
  width: fit-content;
  background: #e8f0fe;
  color: #1a73e8;
  border: none;
}
:deep(.van-button--primary.van-button--plain) {
  border-color: #1a73e8;
  color: #1a73e8;
}
/* 菜单板块同步首页样式 */
.menu-group {
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 14px;
}
:deep(.van-cell) {
  padding: 16px 20px;
  font-size: 15px;
}
.logout-btn {
  margin-top: 30px;
  height: 48px;
  border-radius: 12px;
  background: #f56c6c;
  border: none;
  font-size: 16px;
  font-weight: 500;
}
.logout-btn:active {
  background: #e645;
}
.edit-form {
  padding: 10px 0;
}
:deep(.van-field) {
  padding: 12px 0;
}
/* 头像 */
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8 0%, #4a9af5 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
/* 性别选择 */
.gender-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
}
.gender-label {
  flex-shrink: 0;
  width: 70px;
  font-size: 14px;
  color: #323233;
}
.about-content {
  line-height: 2;
  padding: 12px 6px;
}
.about-content p {
  font-size: 15px;
  color: #333;
  margin: 12px 0;
}
/* 教练预约弹窗 */
.coach-bookings-wrap {
  padding: 20px 16px;
}
.coach-booking-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
  text-align: center;
}
</style>