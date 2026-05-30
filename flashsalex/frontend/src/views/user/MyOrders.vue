<template>
  <div class="orders-page">
    <h2 class="page-title">📦 我的订单</h2>
    <el-table :data="orders" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="payAmount" label="金额" width="100">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: bold">¥{{ row.payAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'WAIT_PAY'"
            type="success"
            size="small"
            @click="handlePay(row.orderNo)"
          >
            支付
          </el-button>
          <el-button
            v-if="row.status === 'WAIT_PAY'"
            type="danger"
            size="small"
            plain
            @click="handleCancel(row.orderNo)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      style="margin-top: 20px; justify-content: center"
      :current-page="page"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="(p) => { page = p; loadOrders() }"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList, payOrder, cancelOrder } from '../../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const page = ref(1)

onMounted(() => loadOrders())

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrderList({ page: page.value, size: 10 })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handlePay(orderNo) {
  await ElMessageBox.confirm('确认模拟支付此订单？', '支付确认', { type: 'warning' })
  await payOrder(orderNo)
  ElMessage.success('支付成功')
  loadOrders()
}

async function handleCancel(orderNo) {
  await ElMessageBox.confirm('确认取消此订单？库存将回补。', '取消确认', { type: 'warning' })
  await cancelOrder(orderNo)
  ElMessage.success('订单已取消')
  loadOrders()
}

function statusLabel(s) {
  return { WAIT_PAY: '待支付', PAID: '已支付', CANCELED: '已取消', REFUNDED: '已退款' }[s] || s
}

function statusType(s) {
  return { WAIT_PAY: 'warning', PAID: 'success', CANCELED: 'info', REFUNDED: 'danger' }[s] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}
.page-title {
  margin-bottom: 20px;
  color: #303133;
}
</style>
