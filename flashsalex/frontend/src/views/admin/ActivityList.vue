<template>
  <div>
    <div class="toolbar">
      <h3>秒杀活动管理</h3>
      <el-button type="primary" @click="$router.push('/admin/activities/add')">
        <el-icon><Plus /></el-icon> 创建活动
      </el-button>
    </div>

    <el-card>
      <el-table :data="activityList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="activityName" label="活动名称" min-width="150" />
        <el-table-column prop="seckillPrice" label="秒杀价" width="100">
          <template #default="{ row }">¥{{ row.seckillPrice }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 4"
              type="success" text size="small"
              @click="handleOnline(row.id)"
            >上线</el-button>
            <el-button
              v-if="row.status === 1 || row.status === 2"
              type="warning" text size="small"
              @click="handleOffline(row.id)"
            >下线</el-button>
            <el-button
              type="primary" text size="small"
              @click="handlePreheat(row.id)"
            >预热</el-button>
            <el-button
              type="info" text size="small"
              @click="handleVerify(row.id)"
            >校验缓存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getActivityList, onlineActivity, offlineActivity, preheatActivity, verifyCache } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const activityList = ref([])

onMounted(() => loadActivities())

async function loadActivities() {
  loading.value = true
  try {
    const res = await getActivityList({ page: 1, size: 100 })
    activityList.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function handleOnline(id) {
  await ElMessageBox.confirm('确认上线该活动？', '提示')
  await onlineActivity(id)
  ElMessage.success('上线成功')
  loadActivities()
}

async function handleOffline(id) {
  await ElMessageBox.confirm('确认下线该活动？', '提示')
  await offlineActivity(id)
  ElMessage.success('已下线')
  loadActivities()
}

async function handlePreheat(id) {
  await preheatActivity(id)
  ElMessage.success('缓存预热成功')
}

async function handleVerify(id) {
  const res = await verifyCache(id)
  if (res.data) {
    ElMessage.success('缓存一致性校验通过')
  } else {
    ElMessage.warning('缓存不一致，请检查！')
  }
}

function statusLabel(s) {
  return { 0: '草稿', 1: '待开始', 2: '进行中', 3: '已结束', 4: '已下线' }[s] || '未知'
}

function statusType(s) {
  return { 0: 'info', 1: 'warning', 2: 'danger', 3: 'info', 4: 'info' }[s] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
