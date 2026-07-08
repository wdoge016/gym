<template>
  <div class="schedule-page">
    <el-card class="page-card">
      <div class="toolbar">
        <el-date-picker v-model="date" type="date" placeholder="选择日期" @change="fetchData" clearable style="width:200px" />
        <el-button type="success" @click="showAdd" class="btn-add">新增排课</el-button>
        <el-button type="danger" plain @click="handleCleanExpired" class="btn-clean">一键清理过期</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" class="schedule-table">
        <el-table-column label="日期" width="110">
          <template #default="{row}">{{ row.startTime?.substring(0, 10) }}</template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="coachName" label="教练" />
        <el-table-column prop="roomName" label="教室" />
        <el-table-column label="上课时间" width="100">
          <template #default="{row}">{{ row.startTime?.substring(11, 16) }} - {{ row.endTime?.substring(11, 16) }}</template>
        </el-table-column>
        <el-table-column label="预约" width="100">
          <template #default="{row}">
            {{ row.bookedCount }} / {{ row.maxMembers }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <el-button type="primary" plain @click="showEdit(row)" class="btn-edit">编辑</el-button>
            <el-button v-if="row.status === 1" type="danger" @click="handleCancel(row.id)" class="btn-delete">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" class="page-pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排课' : '新增排课'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" style="width:100%" placeholder="选择课程">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练">
          <el-select v-model="form.coachId" style="width:100%" placeholder="选择教练">
            <el-option v-for="c in coaches" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室"><el-input v-model="form.roomName" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="最大人数"><el-input-number v-model="form.maxMembers" :min="1" :max="100" /></el-form-item>
        <el-form-item label="退课截止"><el-input-number v-model="form.cancelDeadline" :min="0" /> 分钟前</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" class="btn-primary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSchedules, addSchedule, updateSchedule, cancelSchedule, getCourses, getAllCoaches, cleanExpiredSchedules } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const date = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const courses = ref([])
const coaches = ref([])

async function fetchData() {
  loading.value = true
  try {
    const params = { current: current.value, size: size.value }
    if (date.value) params.date = date.value
    const res = await getSchedules(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function showAdd() {
  isEdit.value = false
  form.value = { maxMembers: 20, cancelDeadline: 120 }
  const [c1, c2] = await Promise.all([getCourses({ size: 100 }), getAllCoaches()])
  courses.value = c1.data.records
  coaches.value = c2.data
  dialogVisible.value = true
}

function showEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  // 兼容后端可能返回数组格式的时间
  if (Array.isArray(form.value.startTime)) {
    const [y, m, d, h = 0, mi = 0, s = 0] = form.value.startTime
    form.value.startTime = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}T${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  if (Array.isArray(form.value.endTime)) {
    const [y, m, d, h = 0, mi = 0, s = 0] = form.value.endTime
    form.value.endTime = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}T${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateSchedule(form.value.id, form.value)
  } else {
    await addSchedule(form.value)
  }
  ElMessage.success(isEdit.value ? '更新成功' : '排课成功')
  dialogVisible.value = false
  fetchData()
}

async function handleCleanExpired() {
  await ElMessageBox.confirm('确认清理所有已过期的排课？（不下课时间 < 当前时间）', '提示', { type: 'warning' })
  const res = await cleanExpiredSchedules()
  ElMessage.success(res.message || '清理完成')
  fetchData()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确认取消该排课？', '提示', { type: 'warning' })
  await cancelSchedule(id)
  ElMessage.success('已取消')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.schedule-page { height: 100%; }
.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
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

/* 新增按钮：绿色渐变 */
:deep(.btn-add) {
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

/* 一键清理按钮 */
:deep(.btn-clean) {
  height: 36px;
  padding: 0 18px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}
:deep(.btn-clean:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

/* 编辑按钮：橙色描边+放大 */
:deep(.btn-edit) {
  height: 36px;
  padding: 0 18px;
  font-size: 14px;
  border-color: #1a73e8;
  color: #1a73e8;
  font-weight: 500;
  transition: all 0.2s ease;
}
:deep(.btn-edit:hover) {
  background: #e8f0fe;
  border-color: #1a73e8;
  color: #1a73e8;
}

/* 取消按钮：实心红色+放大 */
:deep(.btn-delete) {
  height: 36px;
  padding: 0 18px;
  font-size: 14px;
  background: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
  font-weight: 500;
  transition: all 0.2s ease;
}
:deep(.btn-delete:hover) {
  background: #e64545;
  border-color: #e64545;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

/* 输入框/选择器聚焦橙色 */
:deep(.el-input__wrapper.is-focus),
:deep(.el-select.is-focus .el-input__wrapper),
:deep(.el-date-editor.is-focus) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 数字输入框橙色 */
:deep(.el-input-number.is-controls-right .el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 表格样式 */
:deep(.schedule-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.schedule-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
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