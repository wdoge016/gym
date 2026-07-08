<template>
  <div class="card-page">
    <el-card class="page-card">
      <template #header><span>会员卡记录</span></template>
      <el-table :data="tableData" stripe v-loading="loading" class="card-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="memberName" label="会员" />
        <el-table-column prop="cardType" label="类型" width="100">
          <template #default="{row}">
            <el-tag size="small">
              {{ row.cardType === 'MONTH' ? '月卡' : row.cardType === 'QUARTER' ? '季卡' : '年卡' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="到期日期" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '生效中' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="购买时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/api/v1/cards/all')
    tableData.value = res.data
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.card-page { height: 100%; }
.page-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 表格样式 */
:deep(.card-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.card-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fffaf5;
}
</style>