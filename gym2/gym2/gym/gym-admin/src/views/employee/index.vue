<template>
  <div class="employee-page">
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索员工" style="width:240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData" class="btn-primary">搜索</el-button>
        <el-button type="success" @click="showAdd" class="btn-add">新增员工</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" class="employee-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{row}">
            <el-tag size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-switch :model-value="row.status === 1" @change="(val) => toggleStatus(row.id, val ? 1 : 0)" class="status-switch" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <el-button type="primary" plain @click="showEdit(row)" class="btn-edit">编辑</el-button>
            <el-button type="danger" @click="handleDelete(row.id)" class="btn-delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" class="page-pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="500px" class="employee-dialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item v-if="!isEdit" label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="店长" value="STORE_ADMIN" />
            <el-option label="教练" value="COACH" />
            <el-option label="员工" value="STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位"><el-input v-model="form.position" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="form.department" /></el-form-item>
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
import { getEmployees, addEmployee, updateEmployee, updateEmployeeStatus } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

async function fetchData() {
  loading.value = true
  try {
    const res = await getEmployees({ current: current.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function showAdd() {
  isEdit.value = false
  form.value = { role: 'STAFF' }
  dialogVisible.value = true
}

function showEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateEmployee(form.value.id, form.value)
  } else {
    await addEmployee(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该员工？', '提示', { type: 'warning' })
  await request.delete(`/api/v1/employees/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

async function toggleStatus(id, status) {
  await updateEmployeeStatus(id, status)
  ElMessage.success('状态更新')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.employee-page { height: 100%; }
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

/* 删除按钮：实心红色+放大 */
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

/* 输入框聚焦橙色 */
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 状态开关橙色 */
:deep(.status-switch .el-switch.is-checked .el-switch__core) {
  background-color: #1a73e8;
}

/* 下拉选择橙色 */
:deep(.el-select.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 表格样式 */
:deep(.employee-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.employee-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
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