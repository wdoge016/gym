<template>
  <div class="course-page">
    <!-- 全局居中容器，统一适配大屏 -->
    <div class="page-inner">
      <!-- 搜索区域 -->
      <div class="search-wrap">
        <van-search
          v-model="keyword"
          placeholder="搜索课程"
          shape="round"
          clearable
          show-action
          @search="handleSearch"
          @clear="resetSearch"
          @update:model-value="onKeywordChange"
          class="search-input"
        >
          <template #action>
            <span @click="handleSearch" style="color:#1a73e8;font-size:14px">搜索</span>
          </template>
        </van-search>
      </div>

      <!-- 标签切换区域 -->
      <div class="tabs-wrap">
        <van-tabs v-model:active="typeFilter" @change="fetchAll" sticky>
          <van-tab title="全部" name="" />
          <van-tab title="团课" name="GROUP" />
          <van-tab title="私教" name="PRIVATE" />
          <van-tab title="训练营" name="CAMP" />
        </van-tabs>
      </div>

      <!-- 课程列表区域 -->
      <div class="list-wrap">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            @load="fetchAll"
            finished-text="没有更多课程了"
            class="course-list"
          >
            <van-card
              v-for="c in courses"
              :key="c.id"
              :title="c.name"
              :desc="c.description"
              :price="'¥' + c.price"
              :thumb="courseThumb(c)"
              class="course-card"
            >
              <template #tags>
                <van-tag plain type="primary" size="small">
                  {{ c.type === 'GROUP' ? '团课' : c.type === 'PRIVATE' ? '私教' : '训练营' }}
                </van-tag>
                <van-tag plain type="warning" size="small" style="margin-left:4px">
                  {{ '★'.repeat(c.intensity) }}
                </van-tag>
                <span style="font-size:12px;color:#999;margin-left:4px">{{ c.duration }}分钟</span>
              </template>
              <template #bottom>
                <div v-if="getScheduleTimes(c.id).length > 0" class="time-tags">
                  <van-tag
                    v-for="s in getScheduleTimes(c.id)"
                    :key="s.id"
                    :type="s._selected ? 'primary' : 'default'"
                    size="small"
                    style="margin:2px"
                    @click="selectTime(c.id, s)"
                  >
                    {{ s.roomName }} {{ formatTime(s.startTime) }}
                  </van-tag>
                </div>
                <div v-else style="font-size:12px;color:#999;margin-top:4px">暂无排课</div>
              </template>
              <template #footer>
                <van-button
                  size="small"
                  round
                  type="primary"
                  :disabled="!selectedSchedule[c.id]"
                  @click="handleBook(c.id)"
                  class="book-btn"
                >
                  {{ selectedSchedule[c.id] ? '预约 ' + formatTime(selectedSchedule[c.id].startTime) : '请选择时间' }}
                </van-button>
              </template>
            </van-card>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { useMemberStore } from '@/stores/user'
import request from '@/api/request'
import { showNotify } from 'vant'

const store = useMemberStore()
const userInfo = computed(() => store.userInfo)
const keyword = ref('')
const typeFilter = ref('')
const courses = ref([])
const schedules = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const selectedSchedule = reactive({})

// 课程图片本地映射
function courseThumb(c) {
  if (c.coverImage) return c.coverImage
  const map = {
    'HIIT': 'hit.jpg', '间歇': 'hit.jpg',
    '减脂': 'fatloss.jpg', '训练营': 'fatloss.jpg',
    '动感单车': 'spin.jpg', '单车': 'spin.jpg',
    '哈他': 'hatha.jpg', '瑜伽': 'hatha.jpg',
    '普拉提': 'pilates.jpg', '核心': 'pilates.jpg',
    '流瑜伽': 'flowyoga.jpg', '流': 'flowyoga.jpg',
    '私教': 'pt.jpg', '一对一': 'pt.jpg'
  }
  for (const [k, v] of Object.entries(map)) {
    if (c.name && c.name.includes(k)) return '/uploads/' + v
  }
  return 'https://via.placeholder.com/80'
}

// 时间格式化
function formatTime(t) {
  if (!t) return ''
  return t.substring(5, 16).replace('T', ' ')
}

// 根据课程id获取排课列表
function getScheduleTimes(courseId) {
  return schedules.value.filter(s => s.courseId === courseId)
}

// 实时搜索防抖
let searchTimer = null
function onKeywordChange() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => handleSearch(), 300)
}

// 点击搜索/回车搜索
async function handleSearch() {
  loading.value = true
  await fetchAll()
}

// 点击搜索框清空叉号，重置搜索恢复全部数据
async function resetSearch() {
  keyword.value = ''
  loading.value = true
  await fetchAll()
}

// 统一加载课程+排课
async function fetchAll() {
  await Promise.all([fetchCourses(), fetchSchedules()])
}

// 请求课程列表
async function fetchCourses() {
  try {
    const params = { size: 50 }
    // 关键词存在则携带，不存在则不传，自动返回全部
    if (keyword.value) params.keyword = keyword.value
    if (typeFilter.value) params.type = typeFilter.value
    const res = await request.get('/api/v1/courses', { params })
    courses.value = res.data.records || []
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 请求可预约排课
async function fetchSchedules() {
  try {
    const res = await request.get('/api/v1/schedules/available')
    schedules.value = res.data || []
  } catch (e) {
    showNotify({ type: 'danger', message: '排课数据加载失败' })
  }
}

// 选择排课时间段
function selectTime(courseId, schedule) {
  schedules.value.forEach(s => s._selected = false)
  schedule._selected = true
  selectedSchedule[courseId] = schedule
}

// 下拉刷新
function onRefresh() {
  refreshing.value = true
  fetchAll()
}

// 预约课程
async function handleBook(courseId) {
  const s = selectedSchedule[courseId]
  if (!s) return
  try {
    await request.post('/api/v1/reservations', { scheduleId: s.id })
    showNotify({ type: 'success', message: '预约成功' })
    // 清空已选时间
    delete selectedSchedule[courseId]
    schedules.value.forEach(item => item._selected = false)
  } catch (e) {
    showNotify({ type: 'danger', message: e.response?.data?.message || '预约失败' })
  }
}
</script>

<style scoped>
/* 页面全局样式 - 统一index.vue风格 */
.course-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}

/* 全局居中容器 - 和index.vue保持一致 */
.page-inner {
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px; /* 板块之间统一间距 */
}

/* 搜索区域样式 */
.search-wrap {
  background: #fff;
  border-radius: 14px;
  padding: 12px 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}

.search-input {
  --van-search-background: #f5f5f5;
  --van-search-input-height: 36px;
  --van-search-border-radius: 18px;
}

/* 标签切换区域 */
.tabs-wrap {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 课程列表区域 */
.list-wrap {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}

.course-list {
  --van-list-padding: 0;
}

/* 课程卡片样式 */
.course-card {
  margin-bottom: 12px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.course-card:last-child {
  margin-bottom: 0;
}

/* 时间标签区域 */
.time-tags {
  margin-top: 6px;
  cursor: pointer;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 预约按钮样式 */
.book-btn {
  width: 100%;
  --van-button-height: 32px;
  --van-button-font-size: 14px;
}

/* 适配小屏样式 */
@media (max-width: 375px) {
  .course-page {
    padding: 8px;
  }

  .page-inner {
    gap: 10px;
  }

  .search-wrap {
    padding: 8px 12px;
  }

  .list-wrap {
    padding: 12px;
  }
}
</style>