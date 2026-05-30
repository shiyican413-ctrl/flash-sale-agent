<template>
  <div>
    <h3 style="margin-bottom: 20px">{{ isEdit ? '编辑商品' : '添加商品' }}</h3>
    <el-card style="max-width: 600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="large">
        <el-form-item label="商品名称" prop="goodsName">
          <el-input v-model="form.goodsName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="goodsDesc">
          <el-input v-model="form.goodsDesc" type="textarea" :rows="3" placeholder="商品描述" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="封面图URL">
          <el-input v-model="form.coverUrl" placeholder="图片URL" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createGoods, updateGoods, getGoodsDetail } from '../../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const form = ref({ goodsName: '', goodsDesc: '', originalPrice: null, coverUrl: '' })
const rules = {
  goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

onMounted(async () => {
  if (isEdit.value) {
    const res = await getGoodsDetail(route.params.id)
    form.value = res.data
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateGoods(route.params.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createGoods(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/admin/goods')
  } finally {
    submitting.value = false
  }
}
</script>
