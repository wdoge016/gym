<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索课程" style="width:200px" clearable @clear="fetchData" />
        <el-select v-model="typeFilter" placeholder="课程类型" clearable @change="fetchData" style="width:140px">
          <el-option label="团课" value="GROUP" />
          <el-option label="私教" value="PRIVATE" />
          <el-option label="训练营" value="CAMP" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="showAdd">新增课程</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{row}">
            <el-tag :type="row.type === 'GROUP' ? '' : row.type === 'PRIVATE' ? 'warning' : 'danger'" size="small">
              {{ row.type === 'GROUP' ? '团课' : row.type === 'PRIVATE' ? '私教' : '训练营' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intensity" label="强度" width="80">
          <template #default="{row}">
            <el-rate v-model="row.intensity" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="maxCapacity" label="容量" width="80" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="current" v-model:page-size="size" :total="total"
        :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="团课 GROUP" value="GROUP" />
            <el-option label="私教 PRIVATE" value="PRIVATE" />
            <el-option label="训练营 CAMP" value="CAMP" />
          </el-select>
        </el-form-item>
        <el-form-item label="强度"><el-input-number v-model="form.intensity" :min="1" :max="5" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.duration" :min="10" :max="180" /></el-form-item>
        <el-form-item label="最大容量"><el-input-number v-model="form.maxCapacity" :min="1" :max="100" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourses, addCourse, updateCourse, deleteCourse } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const typeFilter = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

async function fetchData() {
  loading.value = true
  try {
    const res = await getCourses({ current: current.value, size: size.value, keyword: keyword.value, type: typeFilter.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function showAdd() { isEdit.value = false; form.value = { type: 'GROUP', intensity: 1, duration: 60, maxCapacity: 20, price: 0 }; dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }

async function handleSave() {
  if (isEdit.value) { await updateCourse(form.value.id, form.value) }
  else { await addCourse(form.value) }
  ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该课程？', '提示', { type: 'warning' })
  await deleteCourse(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
