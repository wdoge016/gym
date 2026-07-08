<template>
  <div class="card-page">
    <!-- 全局居中容器，和首页保持一致 -->
    <div class="page-inner">
      <!-- 我的会员卡板块 -->
      <div class="block-wrap">
        <div class="block-title">我的会员卡</div>

        <van-empty v-if="cards.length === 0" description="暂无会员卡" />

        <div v-else class="card-list">
          <van-cell-group inset v-for="c in cards" :key="c.id" class="card-item-wrap">
            <van-cell
              :title="c.cardType === 'MONTH' ? '月卡' : c.cardType === 'QUARTER' ? '季卡' : '年卡'"
              :label="'有效期: ' + (c.startDate || '') + ' ~ ' + (c.endDate || '')"
              :value="'¥' + c.amount"
            >
              <template #right-icon>
                <van-tag :type="c.status === 1 ? 'success' : 'default'" size="small">
                  {{ c.status === 1 ? '生效中' : '已过期' }}
                </van-tag>
              </template>
            </van-cell>
            <van-row class="btn-row" gutter="8">
              <van-col span="12">
                <van-button size="small" block plain type="primary" @click="handleRenew(c.id)">续费</van-button>
              </van-col>
            </van-row>
          </van-cell-group>
        </div>
      </div>

      <!-- 购买会员卡板块 -->
      <div class="block-wrap">
        <div class="block-title">购买会员卡</div>

        <van-radio-group v-model="cardType" direction="horizontal" class="radio-group">
          <van-radio name="MONTH">月卡 ¥299</van-radio>
          <van-radio name="QUARTER">季卡 ¥799</van-radio>
          <van-radio name="YEAR">年卡 ¥2599</van-radio>
        </van-radio-group>

        <van-button round block type="primary" class="buy-btn" @click="handleBuy">立即购买</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { showNotify } from 'vant'

const cards = ref([])
const cardType = ref('MONTH')

async function fetchData() {
  try {
    const res = await request.get('/api/v1/cards/my')
    cards.value = res.data || []
  } catch (err) {
    showNotify({ type: 'danger', message: '会员卡列表加载失败' })
  }
}

async function handleBuy() {
  try {
    const orderRes = await request.post(`/api/v1/cards?cardType=${cardType.value}`)
    await request.post('/api/v1/payments', { orderId: orderRes.data.id, payMethod: 'WECHAT' })
    showNotify({ type: 'success', message: '购买成功' })
    fetchData()
  } catch (e) {
    showNotify({ type: 'danger', message: e.response?.data?.message || '购买失败' })
  }
}

async function handleRenew(cardId) {
  try {
    const orderRes = await request.post(`/api/v1/cards/${cardId}/renew`)
    await request.post('/api/v1/payments', { orderId: orderRes.data.id, payMethod: 'WECHAT' })
    showNotify({ type: 'success', message: '续费成功' })
    fetchData()
  } catch (e) {
    showNotify({ type: 'danger', message: e.response?.data?.message || '续费失败' })
  }
}

onMounted(fetchData)
</script>

<style scoped>
/* 全局页面容器，和首页完全统一 */
.card-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}
.page-inner {
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 通用板块样式（复制首页block-wrap） */
.block-wrap {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}
.block-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
.block-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: #1a73e8;
  margin-right: 8px;
  border-radius: 2px;
}

/* 会员卡列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.card-item-wrap {
  margin-bottom: 0;
}
.btn-row {
  padding: 8px 16px;
}

/* 单选框区域 */
.radio-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

/* 底部购买按钮 */
.buy-btn {
  --van-button-height: 42px;
  font-size: 16px;
}
</style>