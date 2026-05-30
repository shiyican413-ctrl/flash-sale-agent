<template>
  <div>
    <h3 style="margin-bottom: 20px">黑名单管理</h3>

    <!-- 添加黑名单 -->
    <el-card style="max-width: 600px; margin-bottom: 20px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="large" inline>
        <el-form-item label="类型" prop="targetType">
          <el-select v-model="form.targetType" style="width: 120px">
            <el-option label="用户ID" value="USER" />
            <el-option label="IP地址" value="IP" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值" prop="targetValue">
          <el-input v-model="form.targetValue" placeholder="用户ID 或 IP" style="width: 180px" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" placeholder="封禁原因" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="handleAdd">添加</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 说明 -->
    <el-card>
      <el-alert
        title="黑名单说明"
        type="info"
        :closable="false"
        description="添加到黑名单的用户或IP将被拦截，无法访问秒杀相关接口。支持手动输入用户ID或IP地址进行封禁。"
        show-icon
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { addBlacklist } from '../../api/admin'
import { ElMessage } from 'element-plus'

const formRef = ref()
const form = ref({ targetType: 'USER', targetValue: '', reason: '' })
const rules = {
  targetType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  targetValue: [{ required: true, message: '请输入目标值', trigger: 'blur' }],
}

async function handleAdd() {
  await formRef.value.validate()
  await addBlacklist(form.value)
  ElMessage.success('已添加到黑名单')
  form.value.targetValue = ''
  form.value.reason = ''
}
</script>
