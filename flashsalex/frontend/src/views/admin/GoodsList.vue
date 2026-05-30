<template>
  <div>
    <div class="toolbar">
      <h3>商品管理</h3>
      <el-button type="primary" @click="$router.push('/admin/goods/add')">
        <el-icon><Plus /></el-icon> 添加商品
      </el-button>
    </div>

    <el-card>
      <el-table :data="goodsList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="originalPrice" label="原价" width="120">
          <template #default="{ row }">¥{{ row.originalPrice }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="$router.push(`/admin/goods/edit/${row.id}`)">
              编辑
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getGoodsList, deleteGoods } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const goodsList = ref([])

onMounted(() => loadGoods())

async function loadGoods() {
  loading.value = true
  try {
    const res = await getGoodsList({ page: 1, size: 100 })
    goodsList.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteGoods(id)
  ElMessage.success('删除成功')
  loadGoods()
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
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
