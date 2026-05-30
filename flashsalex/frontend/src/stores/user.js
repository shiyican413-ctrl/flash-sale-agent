import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '../api/auth'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  async function login(form) {
    const res = await loginApi(form)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    // 登录后获取用户信息
    await fetchUserInfo()
    return res
  }

  async function register(form) {
    return await registerApi(form)
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  const isAdmin = () => userInfo.value?.role === 'ADMIN'

  return { token, userInfo, login, register, fetchUserInfo, logout, isAdmin }
})
