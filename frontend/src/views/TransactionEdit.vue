<template>
  <div class="transaction-edit">
    <div class="header">
      <el-button @click="$router.go(-1)" icon="ArrowLeft">
        戻る
      </el-button>
      <h1>{{ isEdit ? '取引編集' : '新規取引作成' }}</h1>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="transaction-form"
    >
      <!-- 基本情報 -->
      <el-card class="form-card">
        <template #header>
          <span>基本情報</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="契約" prop="contractId">
              <el-select
                v-model="form.contractId"
                placeholder="契約を選択"
                filterable
                clearable
                @change="onContractChange"
              >
                <el-option
                  v-for="contract in contracts"
                  :key="contract.id"
                  :label="`${contract.id} - ${contract.propertyName}`"
                  :value="contract.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="取引タイプ" prop="type">
              <el-select v-model="form.type" placeholder="タイプを選択">
                <el-option
                  v-for="type in transactionTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金額" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0"
                :precision="0"
                :step="1000"
                style="width: 100%"
                placeholder="金額を入力"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="取引日" prop="transactionDate">
              <el-date-picker
                v-model="form.transactionDate"
                type="date"
                placeholder="取引日を選択"
                style="width: 100%"
                format="YYYY/MM/DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ステータス" prop="status">
              <el-select v-model="form.status" placeholder="ステータスを選択">
                <el-option
                  v-for="status in transactionStatuses"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="説明" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="取引の説明を入力"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 関連情報表示 -->
      <el-card v-if="selectedContract" class="related-card">
        <template #header>
          <span>選択された契約情報</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>契約詳細</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="契約ID">
                {{ selectedContract.id }}
              </el-descriptions-item>
              <el-descriptions-item label="契約期間">
                {{ formatDate(selectedContract.startDate) }} 〜 {{ formatDate(selectedContract.endDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="月額家賃">
                ¥{{ selectedContract.monthlyRent?.toLocaleString() }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <h4>物件情報</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="物件名">
                {{ selectedContract.propertyName }}
              </el-descriptions-item>
              <el-descriptions-item label="住所">
                {{ selectedContract.propertyAddress }}
              </el-descriptions-item>
              <el-descriptions-item label="価格">
                ¥{{ selectedContract.propertyPrice?.toLocaleString() }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>

      <!-- 操作ボタン -->
      <div class="form-actions">
        <el-button @click="$router.go(-1)">
          キャンセル
        </el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '作成' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { transactionApi, contractApi } from '@/services/api'
import type { Transaction, Contract, TransactionType, TransactionStatus } from '@/types'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const contracts = ref<Contract[]>([])
const selectedContract = ref<Contract | null>(null)
const submitting = ref(false)

const isEdit = computed(() => route.params.id !== 'new')

const form = ref({
  contractId: null as number | null,
  type: '' as TransactionType,
  amount: null as number | null,
  transactionDate: '',
  status: 'PENDING' as TransactionStatus,
  description: ''
})

const rules: FormRules = {
  contractId: [
    { required: true, message: '契約を選択してください', trigger: 'change' }
  ],
  type: [
    { required: true, message: '取引タイプを選択してください', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '金額を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '金額は0以上で入力してください', trigger: 'blur' }
  ],
  transactionDate: [
    { required: true, message: '取引日を選択してください', trigger: 'change' }
  ],
  status: [
    { required: true, message: 'ステータスを選択してください', trigger: 'change' }
  ]
}

const transactionTypes = [
  { value: 'PAYMENT', label: '支払い' },
  { value: 'REFUND', label: '返金' },
  { value: 'COMMISSION', label: '手数料' },
  { value: 'MAINTENANCE', label: 'メンテナンス' },
  { value: 'INSURANCE', label: '保険' },
  { value: 'TAX', label: '税金' }
]

const transactionStatuses = [
  { value: 'PENDING', label: '処理中' },
  { value: 'COMPLETED', label: '完了' },
  { value: 'FAILED', label: '失敗' },
  { value: 'CANCELLED', label: 'キャンセル' }
]

onMounted(async () => {
  await loadContracts()
  
  if (isEdit.value && route.params.id) {
    await loadTransaction(parseInt(route.params.id as string))
  }
})

const loadContracts = async () => {
  try {
    const response = await contractApi.getAll()
    contracts.value = response.data
  } catch (error) {
    ElMessage.error('契約データの取得に失敗しました')
    console.error(error)
  }
}

const loadTransaction = async (id: number) => {
  try {
    const response = await transactionApi.getById(id)
    const transaction = response.data
    
    form.value = {
      contractId: transaction.contractId,
      type: transaction.type,
      amount: transaction.amount,
      transactionDate: transaction.transactionDate,
      status: transaction.status,
      description: transaction.description || ''
    }
    
    // 契約情報も読み込む
    if (transaction.contractId) {
      await loadContract(transaction.contractId)
    }
  } catch (error) {
    ElMessage.error('取引データの取得に失敗しました')
    console.error(error)
  }
}

const loadContract = async (contractId: number) => {
  try {
    const response = await contractApi.getById(contractId)
    selectedContract.value = response.data
  } catch (error) {
    ElMessage.error('契約データの取得に失敗しました')
    console.error(error)
  }
}

const onContractChange = async (contractId: number | null) => {
  if (contractId) {
    await loadContract(contractId)
  } else {
    selectedContract.value = null
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (isEdit.value && route.params.id) {
      await transactionApi.update(parseInt(route.params.id as string), form.value)
      ElMessage.success('取引を更新しました')
    } else {
      await transactionApi.create(form.value)
      ElMessage.success('取引を作成しました')
    }
    
    router.push('/transactions')
  } catch (error) {
    if (error !== false) { // バリデーションエラー以外
      ElMessage.error(isEdit.value ? '取引の更新に失敗しました' : '取引の作成に失敗しました')
      console.error(error)
    }
  } finally {
    submitting.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '不明'
  return new Date(date).toLocaleDateString('ja-JP')
}
</script>

<style scoped>
.transaction-edit {
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.header h1 {
  margin: 0;
  color: #303133;
}

.transaction-form {
  max-width: 1200px;
}

.form-card,
.related-card {
  margin-bottom: 20px;
}

.related-card h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
