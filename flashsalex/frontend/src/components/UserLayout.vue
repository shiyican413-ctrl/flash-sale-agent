<template>
  <el-container style="min-height: 100vh">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-left">
        <h2 class="logo" @click="$router.push('/')">⚡ FlashSaleX</h2>
        <el-menu mode="horizontal" :default-active="activeMenu" router :ellipsis="false">
          <el-menu-item index="/activities">秒杀活动</el-menu-item>
          <el-menu-item index="/orders" v-if="token">我的订单</el-menu-item>
          <el-menu-item index="/admin/goods" v-if="isAdmin">管理后台</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="token && userInfo">
          <span class="user-info">{{ userInfo.username }}</span>
          <el-tag v-if="isAdmin" type="danger" size="small" style="margin-right: 10px">管理员</el-tag>
          <el-button type="danger" text @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <!-- 主内容 -->
    <el-main>
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      FlashSaleX 秒杀系统 © 2026
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

const token = computed(() => userStore.token)
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin())
const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 60px;
}
.header-left {
  display: flex;
  align-items: center;
}
.logo {
  color: #e6a23c;
  cursor: pointer;
  margin-right: 20px;
  white-space: nowrap;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  margin-right: 10px;
  color: #606266;
  font-weight: 500;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #eee;
}
</style>
