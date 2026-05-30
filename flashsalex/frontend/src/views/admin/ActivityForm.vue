<template>
  <div>
    <h3 style="margin-bottom: 20px">创建秒杀活动</h3>
    <el-card style="max-width: 700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" size="large">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="如：iPhone 秒杀专场" />
        </el-form-item>
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="g in goodsOptions"
              :key="g.id"
              :label="`${g.goodsName} (¥${g.originalPrice})`"
              :value="g.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价格" prop="seckillPrice">
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="活动库存" prop="totalStock">
          <el-input-number v-model="form.totalStock" :min="1" :step="10" />
        </el-form-item>
        <el-form-item label="每人限购" prop="perUserLimit">
          <el-input-number v-model="form.perUserLimit" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">创建活动</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createActivity, getGoodsList } from '../../api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const goodsOptions = ref([])

const form = ref({
  activityName: '',
  goodsId: null,
  seckillPrice: null,
  totalStock: 100,
  perUserLimit: 1,
  startTime: '',
  endTime: '',
})

const rules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价格', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

onMounted(async () => {
  const res = await getGoodsList({ page: 1, size: 100 })
  goodsOptions.value = res.data?.records || []
})

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createActivity(form.value)
    ElMessage.success('活动创建成功')
    router.push('/admin/activities')
  } finally {
    submitting.value = false
  }
}
</script>
