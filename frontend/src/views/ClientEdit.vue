<template>
  <div class="client-edit">
    <div class="header">
      <el-button @click="$router.go(-1)">
        <el-icon><ArrowLeft /></el-icon>
        戻る
      </el-button>
      <h1>{{ isEdit ? 'クライアント編集' : '新規クライアント登録' }}</h1>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <h3>基本情報</h3>
            
            <el-form-item label="姓" prop="lastName">
              <el-input v-model="form.lastName" placeholder="姓を入力" />
            </el-form-item>
            
            <el-form-item label="名" prop="firstName">
              <el-input v-model="form.firstName" placeholder="名を入力" />
            </el-form-item>
            
            <el-form-item label="メールアドレス" prop="email">
              <el-input v-model="form.email" placeholder="メールアドレスを入力" />
            </el-form-item>
            
            <el-form-item label="電話番号" prop="phone">
              <el-input v-model="form.phone" placeholder="電話番号を入力" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <h3>詳細情報</h3>
            
            <el-form-item label="クライアントタイプ" prop="type">
              <el-select v-model="form.type" placeholder="タイプを選択" style="width: 100%">
                <el-option
                  v-for="type in clientTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="住所" prop="address">
              <el-input
                v-model="form.address"
                type="textarea"
                :rows="3"
                placeholder="住所を入力"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <div class="form-actions">
          <el-button @click="$router.go(-1)">キャンセル</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '更新' : '登録' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { clientApi } from '@/services/api'
import type { Client, ClientType } from '@/types'

const route = useRoute()
const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  address: '',
  type: '' as ClientType
})

const rules = {
  firstName: [
    { required: true, message: '名を入力してください', trigger: 'blur' }
  ],
  lastName: [
    { required: true, message: '姓を入力してください', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'メールアドレスを入力してください', trigger: 'blur' },
    { type: 'email', message: '正しいメールアドレスを入力してください', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '電話番号を入力してください', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '住所を入力してください', trigger: 'blur' }
  ],
  type: [
    { required: true, message: 'クライアントタイプを選択してください', trigger: 'change' }
  ]
}

const clientTypes = [
  { value: 'BUYER', label: '購入者' },
  { value: 'SELLER', label: '売却者' },
  { value: 'TENANT', label: '賃借人' },
  { value: 'LANDLORD', label: '貸主' }
]

onMounted(() => {
  if (isEdit.value) {
    loadClient()
  }
})

const loadClient = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const response = await clientApi.getById(id)
    const client = response.data
    
    form.value = {
      firstName: client.firstName,
      lastName: client.lastName,
      email: client.email,
      phone: client.phone,
      address: client.address,
      type: client.type
    }
  } catch (error) {
    ElMessage.error('クライアントデータの取得に失敗しました')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      const id = Number(route.params.id)
      await clientApi.update(id, form.value)
      ElMessage.success('クライアントを更新しました')
    } else {
      await clientApi.create(form.value)
      ElMessage.success('クライアントを登録しました')
    }
    
    router.push('/clients')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? 'クライアントの更新に失敗しました' : 'クライアントの登録に失敗しました')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.client-edit {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #303133;
}

h3 {
  color: #606266;
  margin-bottom: 15px;
  margin-top: 20px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.el-divider {
  margin: 30px 0;
}
</style>
