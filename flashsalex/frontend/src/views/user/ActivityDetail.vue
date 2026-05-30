<template>
  <div class="detail-page" v-loading="loading">
    <el-card class="detail-card" v-if="activity">
      <el-row :gutter="40">
        <!-- 左侧商品信息 -->
        <el-col :span="12">
          <div class="goods-image">
            <img v-if="activity.coverUrl" :src="activity.coverUrl" alt="" />
            <div v-else class="placeholder-img">🎁</div>
          </div>
          <h2 class="goods-name">{{ activity.goodsName }}</h2>
          <p class="activity-name">{{ activity.activityName }}</p>
        </el-col>

        <!-- 右侧秒杀信息 -->
        <el-col :span="12">
          <div class="price-section">
            <div class="price-label">秒杀价</div>
            <div class="price-value">
              <span class="symbol">¥</span>
              <span class="amount">{{ activity.seckillPrice }}</span>
            </div>
            <div class="original">原价: ¥{{ activity.originalPrice }}</div>
          </div>

          <div class="info-section">
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Goods /></el-icon>
              <span>剩余库存: <b>{{ activity.availableStock }}</b> 件</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>每人限购 {{ activity.perUserLimit }} 件</span>
            </div>
          </div>

          <el-tag :type="statusTagType" size="large" class="status-tag">
            {{ statusText }}
          </el-tag>

          <!-- 秒杀操作区 -->
          <div class="action-section" v-if="activity.status === 2">
            <div v-if="!isLoggedIn">
              <el-button type="warning" size="large" @click="$router.push('/login')">
                请先登录
              </el-button>
            </div>
            <div v-else-if="step === 0">
              <el-button type="danger" size="large" class="seckill-btn" @click="startSeckill">
                🔥 立即秒杀
              </el-button>
            </div>

            <!-- 步骤1：验证码 -->
            <div v-else-if="step === 1" class="captcha-section">
              <h4>请完成验证码</h4>
              <p class="captcha-question">{{ captchaQuestion }} = ?</p>
              <el-input
                v-model="captchaAnswer"
                placeholder="请输入验证码答案"
                size="large"
                style="width: 200px; margin-right: 10px"
                @keyup.enter="submitCaptcha"
              />
              <el-button type="primary" size="large" @click="submitCaptcha" :loading="submitting">
                提交
              </el-button>
            </div>

            <!-- 步骤2：秒杀中 -->
            <div v-else-if="step === 2" class="seckill-section">
              <el-button
                type="danger"
                size="large"
                class="seckill-btn"
                @click="doSeckill"
                :loading="submitting"
              >
                确认抢购
              </el-button>
            </div>

            <!-- 步骤3：等待结果 -->
            <div v-else-if="step === 3" class="result-section">
              <el-result
                icon="info"
                title="排队中..."
                sub-title="正在等待秒杀结果"
              >
                <template #extra>
                  <div class="countdown">已等待 {{ waitSeconds }} 秒</div>
                </template>
              </el-result>
            </div>

            <!-- 步骤4：结果 -->
            <div v-else-if="step === 4">
              <el-result
                v-if="seckillSuccess"
                icon="success"
                title="🎉 秒杀成功！"
                :sub-title="'订单号: ' + orderNo"
              >
                <template #extra>
                  <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
                </template>
              </el-result>
              <el-result
                v-else
                icon="error"
                title="秒杀失败"
                :sub-title="failReason"
              >
                <template #extra>
                  <el-button @click="step = 0">返回重试</el-button>
                </template>
              </el-result>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Clock, Goods, User } from '@element-plus/icons-vue'
import { getActivityDetail } from '../../api/activity'
import { getCaptcha, getSeckillPath, executeSeckill, getSeckillResult } from '../../api/seckill'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const activity = ref(null)
const isLoggedIn = computed(() => !!userStore.token)
const activityId = computed(() => route.params.id)

// 秒杀流程步骤
const step = ref(0) // 0-未开始 1-验证码 2-确认抢购 3-等待结果 4-最终结果
const captchaId = ref('')
const captchaQuestion = ref('')
const captchaAnswer = ref('')
const seckillPath = ref('')
const submitting = ref(false)
const seckillSuccess = ref(false)
const orderNo = ref('')
const failReason = ref('')
const waitSeconds = ref(0)

let pollTimer = null
let countTimer = null

const statusText = computed(() => {
  return { 0: '草稿', 1: '即将开始', 2: '抢购中', 3: '已结束', 4: '已下线' }[activity.value?.status] || ''
})
const statusTagType = computed(() => {
  return { 0: 'info', 1: 'warning', 2: 'danger', 3: 'info', 4: 'info' }[activity.value?.status] || 'info'
})

onMounted(async () => {
  await loadDetail()
})

onUnmounted(() => {
  clearTimers()
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getActivityDetail(activityId.value)
    activity.value = res.data
  } finally {
    loading.value = false
  }
}

// 开始秒杀 → 获取验证码
async function startSeckill() {
  step.value = 1
  submitting.value = true
  try {
    const res = await getCaptcha(activityId.value)
    captchaId.value = res.data.captchaId
    captchaQuestion.value = res.data.question
  } finally {
    submitting.value = false
  }
}

// 提交验证码 → 获取动态地址
async function submitCaptcha() {
  if (!captchaAnswer.value) {
    return ElMessage.warning('请输入验证码')
  }
  submitting.value = true
  try {
    const res = await getSeckillPath(activityId.value, {
      captchaId: captchaId.value,
      captchaCode: captchaAnswer.value,
    })
    seckillPath.value = res.data.path
    step.value = 2
    ElMessage.success('验证通过，请点击抢购！')
  } finally {
    submitting.value = false
  }
}

// 确认抢购
async function doSeckill() {
  submitting.value = true
  try {
    await executeSeckill(activityId.value, seckillPath.value, { quantity: 1 })
    step.value = 3
    startPolling()
  } catch (e) {
    step.value = 4
    seckillSuccess.value = false
    failReason.value = e.message || '秒杀失败'
  } finally {
    submitting.value = false
  }
}

// 轮询结果
function startPolling() {
  waitSeconds.value = 0
  countTimer = setInterval(() => waitSeconds.value++, 1000)
  pollTimer = setInterval(async () => {
    try {
      const res = await getSeckillResult(activityId.value)
      const status = res.data.status
      if (status === 'SUCCESS') {
        clearTimers()
        step.value = 4
        seckillSuccess.value = true
        orderNo.value = res.data.orderNo
        ElMessage.success('秒杀成功！')
      } else if (status === 'FAILED') {
        clearTimers()
        step.value = 4
        seckillSuccess.value = false
        failReason.value = '很遗憾，秒杀失败'
      }
      // PROCESSING 继续轮询
    } catch {
      // 继续轮询
    }
  }, 1500)

  // 30秒超时
  setTimeout(() => {
    if (step.value === 3) {
      clearTimers()
      step.value = 4
      seckillSuccess.value = false
      failReason.value = '等待超时，请到订单列表查看'
    }
  }, 30000)
}

function clearTimers() {
  if (pollTimer) clearInterval(pollTimer)
  if (countTimer) clearInterval(countTimer)
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.detail-page {
  max-width: 1100px;
  margin: 20px auto;
  padding: 0 20px;
}
.detail-card {
  border-radius: 12px;
}
.goods-image {
  width: 100%;
  height: 300px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  overflow: hidden;
}
.placeholder-img {
  font-size: 80px;
}
.goods-name {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px;
}
.activity-name {
  color: #909399;
  font-size: 14px;
}
.price-section {
  background: linear-gradient(135deg, #fff8e1, #fff3e0);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.price-label {
  color: #e6a23c;
  font-size: 14px;
  margin-bottom: 4px;
}
.price-value {
  color: #e6a23c;
  margin-bottom: 4px;
}
.symbol {
  font-size: 20px;
}
.amount {
  font-size: 42px;
  font-weight: bold;
}
.original {
  color: #c0c4cc;
  text-decoration: line-through;
  font-size: 14px;
}
.info-section {
  margin-bottom: 20px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}
.status-tag {
  margin-bottom: 20px;
}
.seckill-btn {
  width: 100%;
  height: 50px;
  font-size: 20px;
  border-radius: 12px;
  letter-spacing: 4px;
}
.captcha-section,
.seckill-section {
  text-align: center;
}
.captcha-question {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 16px 0;
}
.countdown {
  color: #909399;
  margin-top: 10px;
}
</style>
