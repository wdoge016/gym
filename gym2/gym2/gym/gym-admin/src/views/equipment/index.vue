<template>
  <div class="equipment-page">
    <!-- 器材列表 -->
    <el-card style="margin-bottom:20px" class="page-card">
      <template #header><span>器材管理</span></template>
      <div class="toolbar">
        <el-button type="success" @click="showAdd" class="btn-add">新增器材</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" class="equipment-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="器材名称" />
        <el-table-column prop="equipmentType" label="类型" width="120" />
        <el-table-column prop="targetMuscle" label="目标肌群" />
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : row.status === 'MAINTENANCE' ? 'warning' : 'danger'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : row.status === 'MAINTENANCE' ? '维护中' : '报废' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{row}">
            <el-button type="primary" plain @click="showEdit(row)" class="btn-edit">编辑</el-button>
            <el-button v-if="row.status === 'NORMAL'" type="warning" @click="showReport(row.id)" class="btn-report">报修</el-button>
            <el-button type="danger" @click="handleDeleteEquip(row.id)" class="btn-delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" class="page-pagination"
      />
    </el-card>

    <!-- 报修单列表 -->
    <el-card class="page-card">
      <template #header><span>报修管理</span></template>
      <el-table :data="repairs" stripe v-loading="repairLoading" class="repair-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="equipmentId" label="器材ID" width="80" />
        <el-table-column prop="description" label="故障描述" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{row}">
            <el-tag :type="row.status==='REPORTED'?'danger':row.status==='IN_PROGRESS'?'warning':'success'" size="small">
              {{ row.status==='REPORTED'?'待维修':row.status==='IN_PROGRESS'?'维修中':'已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportTime" label="报修时间" width="160" />
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <el-button v-if="row.status==='REPORTED'" type="warning" @click="handleStartRepair(row.id)" class="btn-edit">开始维修</el-button>
            <el-button v-if="row.status==='IN_PROGRESS'" type="success" @click="handleCompleteRepair(row.id)" class="btn-add">完成维修</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="repairCurrent" v-model:page-size="repairSize"
        :total="repairTotal" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchRepairs" @current-change="fetchRepairs" class="page-pagination"
      />
    </el-card>

    <!-- 器材新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑器材' : '新增器材'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.equipmentType" /></el-form-item>
        <el-form-item label="目标肌群"><el-input v-model="form.targetMuscle" /></el-form-item>

        <el-form-item label="注意事项"><el-input v-model="form.warnings" type="textarea" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" class="btn-primary">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报修弹窗 -->
    <el-dialog v-model="reportVisible" title="提交报修" width="400px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="故障描述">
          <el-input v-model="reportForm.description" type="textarea" placeholder="请描述故障现象..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReport" class="btn-primary">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEquipment, addEquipment, updateEquipment, deleteEquipment, reportRepair, getRepairOrders, startRepair, completeRepair } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

// 报修
const repairs = ref([])
const repairLoading = ref(false)
const repairCurrent = ref(1)
const repairSize = ref(10)
const repairTotal = ref(0)
const reportVisible = ref(false)
const reportForm = ref({ equipmentId: null, description: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getEquipment({ current: current.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function fetchRepairs() {
  repairLoading.value = true
  try {
    const res = await getRepairOrders({ current: repairCurrent.value, size: repairSize.value })
    repairs.value = res.data.records
    repairTotal.value = res.data.total
  } finally { repairLoading.value = false }
}

function showAdd() {
  isEdit.value = false
  form.value = {}
  dialogVisible.value = true
}

function showEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateEquipment(form.value.id, form.value)
  } else {
    await addEquipment(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

async function handleDeleteEquip(id) {
  await ElMessageBox.confirm('确认删除该器材？', '提示', { type: 'warning' })
  await deleteEquipment(id)
  ElMessage.success('删除成功')
  fetchData()
}

// 报修流程
function showReport(equipmentId) {
  reportForm.value = { equipmentId, description: '' }
  reportVisible.value = true
}

async function handleReport() {
  await reportRepair(reportForm.value)
  ElMessage.success('报修已提交')
  reportVisible.value = false
  fetchData()
  fetchRepairs()
}

async function handleStartRepair(id) {
  await startRepair(id, '维修员')
  ElMessage.success('维修已开始')
  fetchRepairs()
}

async function handleCompleteRepair(id) {
  await completeRepair(id, 0)
  ElMessage.success('维修完成')
  fetchData()
  fetchRepairs()
}

onMounted(() => { fetchData(); fetchRepairs() })
</script>

<style scoped>
.equipment-page { height: 100%; }
.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.toolbar { margin-bottom: 16px; }

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

/* 新增/完成按钮：绿色渐变 */
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

/* 编辑/报修按钮：橙色描边+放大 */
:deep(.btn-edit),
:deep(.btn-report) {
  height: 36px;
  padding: 0 18px;
  font-size: 14px;
  border-color: #1a73e8;
  color: #1a73e8;
  font-weight: 500;
  transition: all 0.2s ease;
}
:deep(.btn-edit:hover),
:deep(.btn-report:hover) {
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

/* 表格样式 */
:deep(.equipment-table .el-table__header th),
:deep(.repair-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.equipment-table .el-table--striped .el-table__body tr.el-table__row--striped td),
:deep(.repair-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
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