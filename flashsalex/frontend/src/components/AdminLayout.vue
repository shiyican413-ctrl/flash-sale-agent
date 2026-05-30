<template>
  <el-container style="min-height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="aside-title">⚡ 管理后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin/goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activities">
          <el-icon><Timer /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/blacklist">
          <el-icon><Lock /></el-icon>
          <span>黑名单管理</span>
        </el-menu-item>
        <el-divider />
        <el-menu-item index="/activities">
          <el-icon><Back /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <el-header class="admin-header">
        <span>{{ currentTitle }}</span>
        <div>
          <span style="margin-right: 12px; color: #606266">{{ userStore.userInfo?.username }}</span>
          <el-button type="danger" text @click="userStore.logout()">退出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '管理后台')
</script>

<style scoped>
.aside {
  background: #304156;
  overflow-y: auto;
}
.aside-title {
  color: #e6a23c;
  font-size: 18px;
  font-weight: bold;
  padding: 20px;
  text-align: center;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  font-weight: 600;
}
.admin-main {
  background: #f5f5f5;
}
</style>
