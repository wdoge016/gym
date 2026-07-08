<template>
  <div class="attendance-page">
    <el-row :gutter="20" class="action-row">
      <el-col :span="12">
        <div class="action-card action-checkin" @click="handleCheckIn">
          <el-icon :size="44"><CircleCheck /></el-icon>
          <p class="action-text">签到</p>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="action-card action-checkout" @click="showCheckOutDialog">
          <el-icon :size="44"><Clock /></el-icon>
          <p class="action-text">签退</p>
        </div>
      </el-col>
    </el-row>

    <el-card class="page-card">
      <template #header><span>考勤记录</span></template>
      <el-table :data="tableData" stripe v-loading="loading" class="attendance-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="employeeId" label="员工ID" width="80" />
        <el-table-column prop="attendanceDate" label="日期" width="120" />
        <el-table-column prop="checkInTime" label="签到时间" width="120" />
        <el-table-column prop="checkOutTime" label="签退时间" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='NORMAL'?'success':row.status==='LATE'?'warning':'danger'" size="small">
              {{ row.status==='NORMAL'?'正常':row.status==='LATE'?'迟到':'缺勤' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <el-pagination
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" class="page-pagination"
      />
    </el-card>

    <!-- 签退选择弹窗 -->
    <el-dialog v-model="checkOutVisible" title="选择签退记录" width="400px">
      <el-table :data="todayRecords" @row-click="handleCheckOut" highlight-current-row>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="checkInTime" label="签到时间" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status==='NORMAL'?'success':'warning'" size="small">
              {{ row.status==='NORMAL'?'正常':'迟到' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="checkOutVisible = false">取消</el-button>
        <el-button type="primary" @click="checkOutVisible = false" class="btn-primary">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAttendance, checkIn, checkOut } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const checkOutVisible = ref(false)
const todayRecords = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getAttendance({ current: current.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleCheckIn() {
  try {
    await checkIn()
    ElMessage.success('签到成功')
    fetchData()
  } catch {}
}

async function showCheckOutDialog() {
  const res = await getAttendance({ current: 1, size: 50 })
  const today = new Date().toISOString().split('T')[0]
  todayRecords.value = (res.data.records || []).filter(r => r.attendanceDate === today && !r.checkOutTime)
  if (todayRecords.value.length === 0) {
    ElMessage.warning('没有可签退的记录')
    return
  }
  checkOutVisible.value = true
}

async function handleCheckOut(row) {
  try {
    await checkOut(row.id)
    ElMessage.success('签退成功')
    checkOutVisible.value = false
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.attendance-page { height: 100%; }
.action-row { margin-bottom: 20px; }

/* 快捷操作卡片 */
.action-card {
  height: 120px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}
.action-text {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.action-checkin {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}
.action-checkout {
  background: linear-gradient(135deg, #1a73e8 0%, #4a9af5 100%);
}

.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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

/* 表格样式 */
:deep(.attendance-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.attendance-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fffaf5;
}

/* 分页橙色 */
:deep(.page-pagination .el-pagination.is-background .el-pager li:not(.disabled).is-active) {
  background-color: #1a73e8;
}
:deep(.page-pagination .el-pagination.is-background .el-pager li:not(.disabled):hover) {
  color: #1a73e8;
}
.page-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>