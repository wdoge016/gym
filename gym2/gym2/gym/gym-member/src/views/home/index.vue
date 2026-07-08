<template>
  <div class="home-page">
    <div class="page-inner">
      <!-- 头部 -->
      <div class="header">
        <div class="header-left">
          <h2>Hi, {{ userInfo?.realName || '会员' }}</h2>
          <span class="header-sub">信用 {{ userInfo?.creditScore || 100 }} 分</span>
        </div>
      </div>

      <!-- 轮播 -->
      <div class="swipe-wrap">
        <van-swipe class="gym-swipe" autoplay="3000" indicator-color="#1a73e8">
          <van-swipe-item v-for="img in gymImgList" :key="img">
            <img :src="img" class="swipe-img" />
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 会员卡 -->
      <div class="section">
        <div class="section-head">
          <span class="section-title">会员卡</span>
          <span class="section-more" @click="$router.push('/card')">查看 <van-icon name="arrow" /></span>
        </div>
        <div class="card-wrap" @click="$router.push('/card')">
          <img src="./photo05.png" class="card-img" />
          <div class="card-info">
            <span class="card-label">月卡 ¥299</span>
            <span class="card-label">季卡 ¥799</span>
            <span class="card-label">年卡 ¥2599</span>
          </div>
        </div>
      </div>

      <!-- 健身教练 -->
      <div class="section">
        <div class="section-head">
          <span class="section-title">健身教练</span>
          <span class="section-more" @click="$router.push('/coach')">全部 <van-icon name="arrow" /></span>
        </div>
        <div class="coach-scroll">
          <div class="coach-card" v-for="c in coachList" :key="c.id" @click="$router.push('/coach')">
            <span class="coach-name">{{ c.name }}</span>
            <span class="coach-tag">{{ c.tag }}</span>
          </div>
        </div>
      </div>

      <!-- 健身器材 -->
      <div class="section">
        <div class="section-head">
          <span class="section-title">健身器材</span>
        </div>
        <div class="equip-grid">
          <div class="equip-card" v-for="e in equipList" :key="e.id">
            <div class="equip-thumb">
              <img v-if="e.img" :src="e.img" />
              <van-icon v-else name="bag-o" size="24" color="#ccc" />
            </div>
            <span class="equip-name">{{ e.name }}</span>
            <span class="equip-desc">{{ e.desc }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMemberStore } from '@/stores/user'
import request from '@/api/request'

const store = useMemberStore()
const userInfo = computed(() => store.userInfo)

import img1 from './photo01.png'
import img2 from './photo02.png'
import img3 from './photo03.png'
import img4 from './photo04.png'

const gymImgList = ref([img1, img2, img3, img4])
const coachList = ref([])
const equipList = ref([])

async function fetchCoaches() {
  try {
    const res = await request.get('/api/v1/coaches/all')
    coachList.value = (res.data || []).filter(c => c.status === 1).slice(0, 6).map(c => ({
      id: c.id, name: c.name, tag: c.speciality || '专业教练', img: c.avatarUrl || ''
    }))
  } catch {}
}

async function fetchEquipment() {
  // 本地图片映射（兜底）
  const imgMap = {
    '跑步机': 'treadmill.png', '史密斯': 'smith.jpg', '哑铃': 'dumbbell.jpg',
    '龙门架': 'cable.jpg', '瑜伽': 'yoga.jpg'
  }
  function mapImg(name) {
    for (const [k, v] of Object.entries(imgMap)) if (name.includes(k)) return '/uploads/' + v
    return ''
  }
  try {
    const res = await request.get('/api/v1/equipment', { params: { size: 50 } })
    equipList.value = (res.data?.records || []).filter(e => e.status === 'NORMAL' || e.status === '正常').map(e => ({
      id: e.id, name: e.name,
      desc: e.description || (e.targetMuscle || '') + ' · ' + (e.equipmentType || ''),
      img: e.imageUrl || mapImg(e.name)
    }))
  } catch {}
}

onMounted(() => { fetchCoaches(); fetchEquipment() })
</script>

<style scoped>
.home-page { padding: 16px 12px 32px; background: #f5f6f8; min-height: 100vh; box-sizing: border-box; }
.page-inner { max-width: 520px; margin: 0 auto; }

/* 头部 */
.header { display: flex; align-items: center; margin-bottom: 14px; }
.header h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1a1a1a; }
.header-sub { font-size: 13px; color: #999; margin-left: 12px; }

/* 轮播 */
.swipe-wrap { margin-bottom: 20px; }
.gym-swipe { border-radius: 14px; overflow: hidden; height: 160px; }
.swipe-img { width: 100%; height: 100%; object-fit: cover; }

/* 通用板块 */
.section { margin-bottom: 20px; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-size: 17px; font-weight: 600; color: #1a1a1a; }
.section-more { font-size: 13px; color: #999; display: flex; align-items: center; gap: 2px; cursor: pointer; }

/* 会员卡 */
.card-wrap { border-radius: 14px; overflow: hidden; cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.card-img { width: 100%; display: block; }
.card-info { display: flex; gap: 16px; padding: 14px 16px; background: #fff; }
.card-label { font-size: 15px; font-weight: 500; color: #1a73e8; }

/* 教练横向滚动 */
.coach-scroll { display: flex; gap: 12px; overflow-x: auto; -webkit-overflow-scrolling: touch; }
.coach-scroll::-webkit-scrollbar { display: none; }
.coach-card {
  flex-shrink: 0; width: 88px; display: flex; flex-direction: column; align-items: center; gap: 8px;
  background: #fff; border-radius: 14px; padding: 14px 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.03); cursor: pointer;
}
.coach-name { font-size: 13px; font-weight: 500; color: #1a1a1a; }
.coach-tag { font-size: 11px; color: #1a73e8; background: #e8f0fe; padding: 1px 8px; border-radius: 10px; white-space: nowrap; }

/* 器材网格 */
.equip-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.equip-card {
  background: #fff; border-radius: 12px; padding: 12px;
  display: flex; flex-direction: column; gap: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}
.equip-thumb { width: 100%; height: 80px; border-radius: 8px; overflow: hidden; background: #f7f7f7; display: flex; align-items: center; justify-content: center; }
.equip-thumb img { width: 100%; height: 100%; object-fit: contain; }
.equip-name { font-size: 14px; font-weight: 500; color: #1a1a1a; }
.equip-desc { font-size: 11px; color: #999; line-height: 1.3; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
