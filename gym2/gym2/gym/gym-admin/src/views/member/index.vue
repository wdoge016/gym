<template>
  <div class="member-page">
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索会员" style="width:240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData" class="btn-primary">搜索</el-button>
        <el-button type="success" @click="showAdd" class="btn-add">新增会员</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" class="member-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="creditScore" label="信用分" width="80">
          <template #default="{row}">
            <el-tag :type="row.creditScore >= 80 ? 'success' : row.creditScore >= 60 ? 'warning' : 'danger'" size="small">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-switch :model-value="row.status === 1" @change="(val) => toggleStatus(row.id, val ? 1 : 0)" class="status-switch" />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑会员' : '新增会员'" width="500px" class="member-dialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!editingId" label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { getMembers, updateMember, updateMemberStatus } from '@/api'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ gender: 1 })

async function fetchData() {
  loading.value = true
  try {
    const res = await getMembers({ current: current.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function showAdd() {
  editingId.value = null
  form.value = { gender: 1 }
  dialogVisible.value = true
}

function showEdit(row) {
  editingId.value = row.id
  form.value = { ...row, password: '' }
  dialogVisible.value = true
}

async function handleSave() {
  if (editingId.value) {
    const { password, ...data } = form.value
    await updateMember(editingId.value, data)
    ElMessage.success('更新成功')
  } else {
    await request.post('/api/v1/members', form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该会员？', '提示', { type: 'warning' })
  await request.delete(`/api/v1/members/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

async function toggleStatus(id, status) {
  await updateMemberStatus(id, status)
  ElMessage.success('状态更新成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.member-page {
  height: 100%;
}
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

/* 主按钮：统一橙色渐变主题 */
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

/* 新增按钮：绿色渐变，正向操作区分 */
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

/* 编辑按钮：橙色描边 + 放大尺寸 */
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

/* 删除按钮：实心高饱和红色 + 放大尺寸 */
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

/* 输入框聚焦橙色边框 */
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 状态开关：橙色主题 */
:deep(.status-switch .el-switch.is-checked .el-switch__core) {
  background-color: #1a73e8;
}

/* 表格表头与斑马纹优化 */
:deep(.member-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.member-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fffaf5;
}

/* 分页：橙色激活态 */
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

/* 单选框：橙色选中态 */
:deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #1a73e8;
  background-color: #1a73e8;
}
:deep(.el-radio__input.is-checked+.el-radio__label) {
  color: #1a73e8;
}
</style>