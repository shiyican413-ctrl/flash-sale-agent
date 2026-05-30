<template>
  <div class="activity-list">
    <h2 class="page-title">🔥 秒杀活动</h2>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="loadActivities">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">即将开始</el-radio-button>
        <el-radio-button :value="2">进行中</el-radio-button>
        <el-radio-button :value="3">已结束</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 活动卡片 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="item in activities" :key="item.id">
        <el-card class="activity-card" shadow="hover" @click="goDetail(item.id)">
          <div class="card-header">
            <el-tag :type="statusTagType(item.status)" size="large">
              {{ statusText(item.status) }}
            </el-tag>
            <span class="stock">库存: {{ item.availableStock || 0 }}</span>
          </div>
          <h3 class="card-title">{{ item.activityName }}</h3>
          <div class="price-row">
            <span class="seckill-price">¥{{ item.seckillPrice }}</span>
            <span class="original-price">¥{{ item.originalPrice }}</span>
          </div>
          <div class="time-row">
            <el-icon><Clock /></el-icon>
            {{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}
          </div>
          <el-button
            type="danger"
            class="seckill-btn"
            :disabled="item.status !== 2"
          >
            {{ item.status === 2 ? '立即秒杀' : item.status === 1 ? '即将开始' : '已结束' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && activities.length === 0" description="暂无秒杀活动" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'
import { getActivityList } from '../../api/admin'

const router = useRouter()
const loading = ref(false)
const activities = ref([])
const statusFilter = ref(null)

onMounted(() => {
  loadActivities()
})

async function loadActivities() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await getActivityList(params)
    activities.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/activities/${id}`)
}

function statusText(s) {
  return { 0: '草稿', 1: '即将开始', 2: '进行中', 3: '已结束', 4: '已下线' }[s] || '未知'
}

function statusTagType(s) {
  return { 0: 'info', 1: 'warning', 2: 'danger', 3: 'info', 4: 'info' }[s] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.activity-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-title {
  margin-bottom: 20px;
  color: #303133;
}
.filter-bar {
  margin-bottom: 20px;
}
.activity-card {
  margin-bottom: 20px;
  cursor: pointer;
  border-radius: 12px;
  transition: transform 0.2s;
}
.activity-card:hover {
  transform: translateY(-4px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.stock {
  color: #909399;
  font-size: 13px;
}
.card-title {
  font-size: 18px;
  margin: 8px 0 12px;
  color: #303133;
}
.price-row {
  margin-bottom: 10px;
}
.seckill-price {
  color: #e6a23c;
  font-size: 28px;
  font-weight: bold;
}
.original-price {
  color: #c0c4cc;
  text-decoration: line-through;
  margin-left: 10px;
  font-size: 14px;
}
.time-row {
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.seckill-btn {
  width: 100%;
  font-size: 16px;
  height: 42px;
  border-radius: 8px;
}
</style>
