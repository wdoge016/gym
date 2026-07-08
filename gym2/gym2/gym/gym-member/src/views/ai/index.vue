<template>
  <div class="ai-chat-page">
    <!-- 同步首页居中容器 -->
    <div class="page-inner">
      <div class="chat-header">
        <h3>AI 健身课程顾问</h3>
        <span class="tip">在线为您解答课程、健身相关问题</span>
      </div>
      <div class="chat-box" ref="box">
        <div
          v-for="(m, i) in msgs"
          :key="i"
          :class="['msg', m.role === 'user' ? 'user-msg' : 'ai-msg']"
        >
          {{ m.content }}
        </div>
      </div>
      <div class="input-row">
        <van-field
          v-model="input"
          placeholder="输入你的健身问题，按回车发送"
          @keyup.enter="send"
          class="input-field"
        />
        <van-button size="normal" type="primary" @click="send" :loading="sending">
          发送
        </van-button>
        <van-button size="normal" plain type="warning" @click="recommend">
          智能推荐课程
        </van-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, nextTick } from 'vue'
import request from '@/api/request'
const msgs = ref([
  { role: 'assistant', content: '您好！我是AI健身顾问，可以帮您推荐课程、解答健身、会员卡相关问题。' }
])
const input = ref('')
const sending = ref(false)
const box = ref(null)
async function send() {
  const text = input.value.trim()
  if (!text) return
  msgs.value.push({ role: 'user', content: text })
  input.value = ''
  scrollDown()
  sending.value = true
  try {
    const res = await request.post('/api/v1/ai/chat', { message: text })
    msgs.value.push({ role: 'assistant', content: res.data.content })
  } catch {
    msgs.value.push({ role: 'assistant', content: 'AI服务暂时繁忙，请稍后再试~' })
  } finally {
    sending.value = false
    scrollDown()
  }
}
async function recommend() {
  try {
    const res = await request.post('/api/v1/ai/recommend')
    const courseList = (res.data.courses || [])
      .map((c, i) => `${i + 1}. ${c.courseName} | 类型：${c.type} | 价格：¥${c.price}`)
      .join('\n')
    const replyText = `【智能课程推荐】\n${res.data.reason}\n\n可选课程：\n${courseList}`
    msgs.value.push({ role: 'assistant', content: replyText })
    scrollDown()
  } catch (err) {
    msgs.value.push({ role: 'assistant', content: '获取课程推荐失败，请重试' })
    scrollDown()
  }
}
function scrollDown() {
  nextTick(() => {
    if (box.value) {
      box.value.scrollTop = box.value.scrollHeight
    }
  })
}
</script>
<style scoped>
/* 和首页完全统一页面配置 */
.ai-chat-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}
/* 全局居中容器 */
.page-inner {
  max-width: 520px;
  margin: 0 auto;
}
.chat-header {
  margin-bottom: 14px;
}
.chat-header h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #1d2129;
  font-weight: 600;
}
.tip {
  font-size: 13px;
  color: #86909c;
}
/* 聊天卡片同步首页圆角阴影 */
.chat-box {
  height: 58vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}
.chat-box::-webkit-scrollbar {
  width: 4px;
}
.chat-box::-webkit-scrollbar-thumb {
  background: #e5e6eb;
  border-radius: 4px;
}
.msg {
  max-width: 78%;
  margin-bottom: 14px;
  padding: 10px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
}
.user-msg {
  background: linear-gradient(135deg, #1989fa, #40a9ff);
  color: #fff;
  margin-left: auto;
  border-bottom-right-radius: 4px;
}
.ai-msg {
  background: #f2f3f5;
  color: #333333;
  border-bottom-left-radius: 4px;
}
.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
:deep(.input-field) {
  flex: 1;
}
:deep(.van-field__control) {
  background: #fff;
  border-radius: 12px;
}
</style>