<template>
  <div class="reserve-page">
    <!-- 同步首页居中容器 -->
    <div class="page-inner">
      <!-- 顶部标题区域 -->
      <div class="page-header">
        <h3>我的预约</h3>
        <span class="header-tip">查看全部课程预约记录</span>
      </div>
      <van-tabs v-model:active="tab" class="tab-wrap" color="#1a73e8">
        <!-- 进行中 Tab -->
        <van-tab title="进行中">
          <div class="tab-content">
            <van-empty v-if="activeList.length === 0" description="暂无进行中的预约" />
            <div v-else class="reserve-list">
              <div class="reserve-item" v-for="r in activeList" :key="r.id">
                <van-cell
                  :title="r.courseName"
                  :label="`上课时间：${r.startTime || ''}`"
                  class="reserve-cell"
                >
                  <template #right-icon>
                    <van-tag
                      size="small"
                      :class="r.status === 'BOOKED' ? 'tag-book' : 'tag-check'"
                    >
                      {{ r.status === 'BOOKED' ? '已预约' : '已签到' }}
                    </van-tag>
                  </template>
                </van-cell>
                <div v-if="r.status === 'BOOKED'" class="btn-wrap">
                  <van-button size="small" plain type="danger" @click="handleCancel(r.id)">
                    取消预约
                  </van-button>
                </div>
              </div>
            </div>
          </div>
        </van-tab>
        <!-- 已完成 Tab -->
        <van-tab title="已完成">
          <div class="tab-content">
            <van-empty v-if="doneList.length === 0" description="暂无历史预约记录" />
            <div v-else class="reserve-list">
              <div class="reserve-item" v-for="r in doneList" :key="r.id">
                <van-cell
                  :title="r.courseName || '课程已下架'"
                  :label="r.startTime ? `上课时间：${r.startTime}` : '排课已删除'"
                  class="reserve-cell"
                >
                  <template #right-icon>
                    <van-tag
                      size="small"
                      :class="doneTagClass(r.status)"
                    >
                      {{ doneTagText(r.status) }}
                    </van-tag>
                  </template>
                </van-cell>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { showNotify } from 'vant'
const tab = ref(0)
const activeList = ref([])
const doneList = ref([])
const now = () => new Date()

function isBeforeNow(time) {
  if (!time) return false
  return new Date(time) <= now()
}

async function fetchData() {
  try {
    const res = await request.get('/api/v1/reservations/my')
    const all = res.data || []
    // 进行中：已预约（有任何课程信息）或 已签到且课程未开始
    activeList.value = all.filter(r => {
      if (r.status === 'BOOKED' && r.startTime) return true
      if (r.status === 'CHECKED_IN' && r.startTime) return !isBeforeNow(r.startTime)
      return false
    })
    // 已完成：已取消 / 爽约 / 已签到且课程已开始 / 关联排课已删除(无startTime)
    doneList.value = all.filter(r => {
      if (r.status === 'CANCELLED' || r.status === 'ABSENT') return true
      if (r.status === 'CHECKED_IN') {
        if (!r.startTime) return true // 排课已删除，按过期处理
        return isBeforeNow(r.startTime)
      }
      if (r.status === 'BOOKED' && !r.startTime) return true // 排课已删除
      return false
    })
  } catch (err) {
    showNotify({ type: 'danger', message: '加载预约失败' })
  }
}

function doneTagClass(status) {
  if (status === 'CANCELLED') return 'tag-cancel'
  if (status === 'ABSENT') return 'tag-absent'
  if (status === 'BOOKED') return 'tag-cancel'
  return 'tag-done'
}

function doneTagText(status) {
  if (status === 'CANCELLED') return '已取消'
  if (status === 'ABSENT') return '爽约'
  if (status === 'BOOKED') return '已过期'
  return '已完成'
}
async function handleCancel(id) {
  try {
    await request.delete(`/api/v1/reservations/${id}?reason=用户取消`)
    showNotify({ type: 'success', message: '预约取消成功' })
    fetchData()
  } catch (e) {
    showNotify({ type: 'danger', message: e.response?.data?.message || '取消失败，请重试' })
  }
}
onMounted(fetchData)
</script>
<style scoped>
/* 和首页统一页面边距、背景 */
.reserve-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}
/* 全局居中容器 */
.page-inner {
  max-width: 520px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 14px;
}
.page-header h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
}
.header-tip {
  font-size: 13px;
  color: #86909c;
}
/* tab板块同步首页圆角阴影 */
.tab-wrap {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  margin-bottom: 14px;
}
.tab-content {
  padding: 12px;
  min-height: 40vh;
}
.reserve-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.reserve-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}
.reserve-cell {
  padding: 10px 14px !important;
}
:deep(.van-cell__title) {
  font-size: 15px;
  font-weight: 500;
  color: #222;
}
:deep(.van-cell__label) {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}
.btn-wrap {
  padding: 0 14px 12px;
}
:deep(.van-empty) {
  padding: 40px 0;
}
.tag-book {
  background: #e8f0fe;
  color: #1a73e8;
  border: none;
}
.tag-check {
  background: #e6f7e6;
  color: #07c160;
  border: none;
}
.tag-cancel {
  background: #f2f3f5;
  color: #666;
  border: none;
}
.tag-absent {
  background: #fee;
  color: #ee0a24;
  border: none;
}
.tag-done {
  background: #e6f7ff;
  color: #1890ff;
  border: none;
}
</style>