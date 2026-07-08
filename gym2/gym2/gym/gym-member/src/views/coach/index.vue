<template>
  <div class="coach-booking-page">
    <!-- 全局居中容器，和首页保持一致 -->
    <div class="page-inner">
      <!-- 教练列表板块 -->
      <div class="block-wrap">
        <div class="block-title">教练预约</div>
        <van-list v-model:loading="coachLoading" :finished="coachFinished" finished-text="全部教练已加载完毕">
          <van-cell
            v-for="c in coaches"
            :key="c.id"
            :title="c.name"
            :label="'专长: ' + (c.speciality || '暂无') + (c.description ? ' · ' + c.description : '')"
            is-link
            class="coach-cell"
            @click="showBook(c)"
          />
        </van-list>
      </div>

      <!-- 预约底部弹窗 -->
      <van-popup v-model:show="showPopup" position="bottom" :style="{ height: '50%' }" round>
        <div class="popup-content">
          <h4 class="popup-title">预约 {{ selectedCoach?.name }}</h4>
          <van-field v-model="bookingDate" label="预约日期" type="date" />
          <van-field v-model="bookingTime" label="开始时间" type="time" />
          <van-field v-model="bookingEndTime" label="结束时间" type="time" />
          <van-field v-model="remark" label="训练备注" placeholder="想练什么？" type="textarea" />
          <van-button round block type="primary" class="submit-btn" @click="handleBook">确认预约</van-button>
        </div>
      </van-popup>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { showNotify } from 'vant'

const coaches = ref([])
const coachLoading = ref(false)
const coachFinished = ref(false)
const showPopup = ref(false)
const selectedCoach = ref(null)
const bookingDate = ref('')
const bookingTime = ref('')
const bookingEndTime = ref('')
const remark = ref('')

async function fetchCoaches() {
  try {
    const res = await request.get('/api/v1/coaches/all')
    coaches.value = res.data || []
    coachFinished.value = true
  } catch (err) {
    showNotify({ type: 'danger', message: '教练列表加载失败' })
  } finally {
    coachLoading.value = false
  }
}

function showBook(coach) {
  selectedCoach.value = coach
  const now = new Date()
  bookingDate.value = now.toISOString().split('T')[0]
  bookingTime.value = `${String(now.getHours() + 1).padStart(2, '0')}:00`
  bookingEndTime.value = `${String(now.getHours() + 2).padStart(2, '0')}:00`
  remark.value = ''
  showPopup.value = true
}

async function handleBook() {
  try {
    await request.post('/api/v1/coach-bookings', {
      coachId: selectedCoach.value.id,
      date: bookingDate.value,
      startTime: bookingTime.value,
      endTime: bookingEndTime.value,
      remark: remark.value
    })
    showNotify({ type: 'success', message: '预约成功' })
    showPopup.value = false
    fetchMyBookings()
  } catch (e) {
    showNotify({ type: 'danger', message: e.response?.data?.message || '预约失败' })
  }
}

onMounted(fetchCoaches)
</script>

<style scoped>
/* 页面全局基础样式，完全对齐首页index.vue */
.coach-booking-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}
/* 居中限制宽度容器 */
.page-inner {
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 通用板块容器（复用首页统一样式） */
.block-wrap {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}
.block-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
.block-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: #1a73e8;
  margin-right: 8px;
  border-radius: 2px;
}

/* 教练单元格间距 */
.coach-cell {
  --van-cell-padding-vertical: 12px;
}

/* 弹窗内部样式 */
.popup-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.popup-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #222;
}
.submit-btn {
  margin-top: 10px;
  --van-button-height: 42px;
  font-size: 16px;
}
</style>