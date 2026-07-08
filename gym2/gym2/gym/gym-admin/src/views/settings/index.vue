<template>
  <div class="settings-page">
    <el-card class="page-card">
      <template #header><span>系统参数配置</span></template>
      <el-table :data="tableData" stripe class="settings-table">
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configValue" label="当前值" width="150">
          <template #default="{row}">
            <template v-if="editingKey === row.configKey">
              <el-input v-model="editValue" style="width:120px" />
            </template>
            <template v-else>
              <el-tag size="small">{{ row.configValue }}</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <template v-if="editingKey === row.configKey">
              <el-button type="success" @click="handleSave(row.configKey)" class="btn-add">保存</el-button>
              <el-button @click="editingKey = null" class="btn-edit">取消</el-button>
            </template>
            <template v-else>
              <el-button type="primary" plain @click="startEdit(row.configKey, row.configValue)" class="btn-edit">修改</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const editingKey = ref(null)
const editValue = ref('')

async function fetchData() {
  const res = await request.get('/api/v1/settings')
  tableData.value = res.data
}

function startEdit(key, value) {
  editingKey.value = key
  editValue.value = value
}

async function handleSave(key) {
  await request.put(`/api/v1/settings/${key}`, { configValue: editValue.value })
  ElMessage.success('更新成功')
  editingKey.value = null
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.settings-page { height: 100%; }
.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 修改/取消按钮：橙色描边+放大 */
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

/* 保存按钮：绿色渐变+放大 */
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

/* 输入框聚焦橙色 */
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1a73e8 inset;
}

/* 表格样式 */
:deep(.settings-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.settings-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fffaf5;
}
</style>