<template>
  <div class="dashboard">
    <!-- 核心指标卡片 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-left">
          <div class="stat-value">¥{{ fmt(stats.monthIncome) }}</div>
          <div class="stat-label">本月收入</div>
        </div>
        <div class="stat-icon-box blue"><el-icon :size="24"><Coin /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-left">
          <div class="stat-value">{{ stats.todayNewMembers || 0 }}</div>
          <div class="stat-label">今日新增会员</div>
        </div>
        <div class="stat-icon-box blue"><el-icon :size="24"><UserFilled /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-left">
          <div class="stat-value">{{ stats.todayBookings || 0 }}</div>
          <div class="stat-label">今日预约</div>
        </div>
        <div class="stat-icon-box green"><el-icon :size="24"><Tickets /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-left">
          <div class="stat-value">{{ stats.todaySchedules || 0 }}</div>
          <div class="stat-label">今日排课</div>
        </div>
        <div class="stat-icon-box purple"><el-icon :size="24"><Clock /></el-icon></div>
      </div>
    </div>

    <!-- 图表 + 指标面板 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">热门课程</span>
              <span class="panel-sub">预约量排名</span>
            </div>
          </template>
          <div ref="courseChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">运营数据</span>
            </div>
          </template>
          <div class="kpi-list">
            <div class="kpi-item">
              <div class="kpi-top">
                <span>会员总数</span>
                <strong>{{ stats.totalMembers || 0 }}</strong>
              </div>
            </div>
            <div class="kpi-item">
              <div class="kpi-top">
                <span>在职教练</span>
                <strong>{{ stats.totalCoaches || 0 }}</strong>
              </div>
            </div>
            <div class="kpi-item">
              <div class="kpi-top">
                <span>正常器材</span>
                <strong>{{ stats.totalEquipment || 0 }}</strong>
              </div>
            </div>
            <div class="kpi-divider"></div>
            <div class="kpi-item">
              <div class="kpi-top">
                <span>会员留存率</span>
                <strong>{{ fmtPct(stats.retentionRate) }}</strong>
              </div>
              <el-progress :percentage="pct(stats.retentionRate)" :stroke-width="8" color="#67c23a" :show-text="false" />
            </div>
            <div class="kpi-item">
              <div class="kpi-top">
                <span>课程满座率</span>
                <strong>{{ fmtPct(stats.occupancyRate) }}</strong>
              </div>
              <el-progress :percentage="pct(stats.occupancyRate)" :stroke-width="8" color="#1a73e8" :show-text="false" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getDashboardStats } from '@/api'
import { Coin, UserFilled, Tickets, Clock } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref({})
const courseChartRef = ref(null)
let courseChart = null

function fmt(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString()
}
function pct(val) {
  if (val == null) return 0
  return Math.round(Number(val))
}
function fmtPct(val) {
  return pct(val) + '%'
}

function initChart() {
  if (!courseChartRef.value) return
  if (!courseChart) courseChart = echarts.init(courseChartRef.value)

  const data = (stats.value.hotCourses || [])
    .map(d => ({ name: d.courseName, value: d.bookingCount || 0 }))
    .filter(d => d.value > 0 && d.name !== '未知课程')
    .sort((a, b) => b.value - a.value)

  const names = data.map(d => d.name).reverse()
  const values = data.map(d => d.value).reverse()

  courseChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}: {c} 次' },
    grid: { left: 100, right: 40, top: 10, bottom: 20 },
    xAxis: { type: 'value', minInterval: 1, axisLine: { show: false }, axisTick: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: names, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#606266', fontSize: 13 } },
    series: [{
      type: 'bar', data: values, barWidth: 16,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#4a9af5' }, { offset: 1, color: '#1a73e8' }
        ])
      },
      label: { show: true, position: 'right', color: '#909399', fontSize: 12 }
    }]
  }, true)
}

function handleResize() { courseChart?.resize() }

onMounted(async () => {
  try { const res = await getDashboardStats(); stats.value = res.data } catch {}
})

watch(() => stats.value, () => {
  nextTick(() => { initChart(); window.addEventListener('resize', handleResize) })
}, { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  courseChart?.dispose(); courseChart = null
})
</script>

<style scoped>
.dashboard { padding: 24px; background: #f0f2f5; min-height: 100%; }

/* ====== 统计卡片 ====== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff; border-radius: 12px; padding: 22px 24px;
  display: flex; justify-content: space-between; align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s; cursor: pointer;
}
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.stat-value { font-size: 28px; font-weight: 700; color: #1d2129; line-height: 1; }
.stat-label { font-size: 13px; color: #86909c; margin-top: 8px; }
.stat-icon-box { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-icon-box.blue { background: #e8f0fe; color: #1a73e8; }
.stat-icon-box.blue   { background: #e8f3ff; color: #409eff; }
.stat-icon-box.green  { background: #e6f7e6; color: #67c23a; }
.stat-icon-box.purple { background: #f3e8ff; color: #8e44ad; }

/* ====== 面板 ====== */
.panel {
  border-radius: 12px; border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); height: 100%;
}
.panel-header { display: flex; align-items: baseline; gap: 10px; }
.panel-title { font-size: 16px; font-weight: 600; color: #1d2129; }
.panel-sub { font-size: 12px; color: #86909c; }
.chart-box { width: 100%; height: 340px; }

/* ====== KPI ====== */
.kpi-list { display: flex; flex-direction: column; gap: 20px; padding: 4px 0; }
.kpi-item { display: flex; flex-direction: column; gap: 8px; }
.kpi-top { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: #606266; }
.kpi-top strong { color: #1d2129; font-size: 16px; }
.kpi-divider { height: 1px; background: #f0f2f5; }

/* ====== Element Plus 覆盖 ====== */
:deep(.panel .el-card__header) { padding: 16px 20px 12px; border-bottom: 1px solid #f0f2f5; }
:deep(.panel .el-card__body) { padding: 16px 20px 20px; }

/* ====== 响应式 ====== */
@media (max-width: 1200px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .dashboard { padding: 16px; }
  .stat-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .stat-card { padding: 16px 18px; }
  .stat-value { font-size: 22px; }
}
</style>
