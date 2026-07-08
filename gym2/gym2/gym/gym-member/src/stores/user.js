import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMemberStore = defineStore('member', () => {
  const token = ref(localStorage.getItem('memberToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('memberInfo') || 'null'))

  function setToken(val) {
    token.value = val
    localStorage.setItem('memberToken', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('memberInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('memberToken')
    localStorage.removeItem('memberInfo')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})