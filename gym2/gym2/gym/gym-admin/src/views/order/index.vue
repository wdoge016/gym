<template>
  <div class="order-page">
    <el-card class="page-card">
      <el-table :data="tableData" stripe v-loading="loading" class="order-table">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="orderType" label="类型" width="100">
          <template #default="{row}">
            <el-tag size="small">{{ row.orderType === 'CARD_PURCHASE' ? '购卡' : row.orderType === 'RENEWAL' ? '续费' : '约课' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <el-button v-if="row.status === 'PENDING'" type="success" @click="handlePay(row.id)" class="btn-add">支付</el-button>
            <el-button v-if="row.status === 'PAID'" type="primary" plain @click="handleRefund(row.id)" class="btn-edit">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
        @size-change="fetchData" @current-change="fetchData" class="page-pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllOrders, payOrder, refundOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await getAllOrders({ current: current.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handlePay(orderId) {
  await payOrder({ orderId, payMethod: 'WECHAT' })
  ElMessage.success('支付成功')
  fetchData()
}

async function handleRefund(orderId) {
  await ElMessageBox.confirm('确认退款？', '提示', { type: 'warning' })
  await refundOrder(orderId)
  ElMessage.success('退款成功')
  fetchData()
}

function statusTag(s) {
  return { PENDING: 'warning', PAID: 'success', CANCELLED: 'info', REFUNDED: 'danger' }[s] || ''
}
function statusLabel(s) {
  return { PENDING: '待支付', PAID: '已支付', CANCELLED: '已取消', REFUNDED: '已退款' }[s] || s
}

onMounted(fetchData)
</script>

<style scoped>
.order-page { height: 100%; }
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

/* 支付按钮：绿色渐变+放大 */
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

/* 退款按钮：橙色描边+放大 */
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

/* 表格样式 */
:deep(.order-table .el-table__header th) {
  background-color: #fff7f0;
  color: #303133;
  font-weight: 600;
}
:deep(.order-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
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