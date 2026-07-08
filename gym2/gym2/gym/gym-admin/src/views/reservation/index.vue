<template>
  <div class="reservation-page">
    <el-tabs v-model="activeTab" class="res-tabs">
      <el-tab-pane label="课程预约" name="course">
        <el-card class="page-card">
          <div class="toolbar">
            <el-button type="primary" @click="fetchCourseRes" class="btn-primary">刷新</el-button>
          </div>
          <el-table :data="courseRes" stripe v-loading="courseLoading" class="res-table">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="memberName" label="会员" />
            <el-table-column label="课程">
              <template #default="{row}">{{ row.courseName || '-' }}</template>
            </el-table-column>
            <el-table-column label="教练">
              <template #default="{row}">{{ row.coachName || '-' }}</template>
            </el-table-column>
            <el-table-column label="上课时间" width="160">
              <template #default="{row}">{{ row.startTime || '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" width="120">
              <template #default="{row}">
                <el-tag :type="row.source === 'AI_RECOMMEND' ? 'warning' : row.source === 'WAITLIST' ? 'info' : ''" size="small">
                  {{ row.source === 'MANUAL' ? '手动' : row.source === 'AI_RECOMMEND' ? 'AI推荐' : '候补晋升' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <el-button v-if="row.status === 'BOOKED'" type="success" @click="handleCheckin(row.id)" class="btn-add">签到</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="教练预约" name="coach">
        <el-card class="page-card">
          <el-table :data="coachRes" stripe v-loading="coachLoading" class="res-table">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="memberId" label="会员ID" width="80" />
            <el-table-column prop="coachId" label="教练ID" width="80" />
            <el-table-column prop="appointmentDate" label="日期" width="120" />
            <el-table-column label="时间" width="120">
              <template #default="{row}">{{ row.startTime }} - {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="row.status==='BOOKED'?'':row.status==='COMPLETED'?'success':'info'" size="small">
                  {{ row.status==='BOOKED'?'已约':row.status==='COMPLETED'?'已完成':'已取消' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <el-button v-if="row.status === 'BOOKED'" type="success" @click="handleComplete(row.id)" class="btn-add">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllReservations, checkinReservation } from '@/api'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('course')
const courseLoading = ref(false)
const courseRes = ref([])
const coachLoading = ref(false)
const coachRes = ref([])

async function fetchCourseRes() {
  courseLoading.value = true
  try {
    const res = await getAllReservations()
    courseRes.value = res.data || []
  } finally { courseLoading.value = false }
}

async function fetchCoachRes() {
  coachLoading.value = true
  try {
    const res = await request.get('/api/v1/coach-bookings')
    coachRes.value = res.data || []
  } finally { coachLoading.value = false }
}

async function handleCheckin(id) {
  await checkinReservation(id)
  ElMessage.success('签到成功')
  fetchCourseRes()
}

async function handleComplete(id) {
  await request.put(`/api/v1/coach-bookings/${id}/complete`)
  ElMessage.success('已完成')
  fetchCoachRes()
}

function statusType(s) {
  return { BOOKED: '', CHECKED_IN: 'success', CANCELLED: 'info', ABSENT: 'danger' }[s] || ''
}
function statusLabel(s) {
  return { BOOKED: '已预约', CHECKED_IN: '已签到', CANCELLED: '已取消', ABSENT: '爽约' }[s] || s
}

onMounted(() => { fetchCourseRes(); fetchCoachRes() })
</script>

<style scoped>
.reservation-page { height: 100%; }
.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

/* 标签页橙色主题 */
:deep(.res-tabs .el-tabs__item.is-active) {
  color: #1a73e8;
}
:deep(.res-tabs .el-tabs__active-bar) {
  background-color: #1a73e8;
}
:deep(.res-tabs .el-tabs__item:hover) {
  color: #1a73e8;
}

/* 主按钮：橙色渐变 */
:deep(.btn-primary) {
  background: linear-gradient(90deg, #1a73e8 0%, #4a9af5 100%);
  border: none;
  color: #fff;
  transition: all 0.3s ease;
}
:deep(.btn-primary:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 122, 24, 0.3);
}

/* 签到/完成按钮：绿色渐变+放大 */
:deep(.btn-add) {
  height: 36px;
  padding: 0 18px;
  font-size: 14px;
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: #fff;
  transition: all 0.3s ease;
}
:deep(.btn-add:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* 表格样式 */
:deep(.res-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.res-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fffaf5;
}
</style>