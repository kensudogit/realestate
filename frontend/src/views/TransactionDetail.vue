<template>
  <div class="transaction-detail">
    <div class="header">
      <el-button @click="$router.go(-1)" icon="ArrowLeft">
        戻る
      </el-button>
      <h1>取引詳細</h1>
      <div class="actions">
        <el-button type="primary" @click="editTransaction">
          <el-icon><Edit /></el-icon>
          編集
        </el-button>
        <el-button type="danger" @click="deleteTransaction">
          <el-icon><Delete /></el-icon>
          削除
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="transaction" class="content">
      <!-- 基本情報 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本情報</span>
            <el-tag :type="getStatusTagType(transaction.status)" size="large">
              {{ getStatusLabel(transaction.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="取引ID">
                {{ transaction.id }}
              </el-descriptions-item>
              <el-descriptions-item label="契約ID">
                <el-button type="text" @click="viewContract(transaction.contractId)">
                  {{ transaction.contractId }}
                </el-button>
              </el-descriptions-item>
              <el-descriptions-item label="取引タイプ">
                <el-tag :type="getTypeTagType(transaction.type)">
                  {{ getTypeLabel(transaction.type) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="金額">
                <span class="amount">¥{{ transaction.amount.toLocaleString() }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="取引日">
                {{ formatDate(transaction.transactionDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="作成日">
                {{ formatDate(transaction.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新日">
                {{ formatDate(transaction.updatedAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="説明">
                {{ transaction.description || '説明なし' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>

      <!-- 関連情報 -->
      <el-card class="related-card">
        <template #header>
          <span>関連情報</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>契約情報</h4>
            <div v-if="contract" class="contract-info">
              <p><strong>契約ID:</strong> {{ contract.id }}</p>
              <p><strong>物件名:</strong> {{ contract.propertyName }}</p>
              <p><strong>クライアント:</strong> {{ contract.clientName }}</p>
              <p><strong>契約期間:</strong> {{ formatDate(contract.startDate) }} 〜 {{ formatDate(contract.endDate) }}</p>
            </div>
            <div v-else class="no-data">
              <el-button type="primary" @click="loadContract">
                契約情報を読み込む
              </el-button>
            </div>
          </el-col>
          
          <el-col :span="12">
            <h4>物件情報</h4>
            <div v-if="property" class="property-info">
              <p><strong>物件ID:</strong> {{ property.id }}</p>
              <p><strong>物件名:</strong> {{ property.name }}</p>
              <p><strong>住所:</strong> {{ property.address }}</p>
              <p><strong>価格:</strong> ¥{{ property.price?.toLocaleString() }}</p>
            </div>
            <div v-else class="no-data">
              <el-button type="primary" @click="loadProperty">
                物件情報を読み込む
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 操作履歴 -->
      <el-card class="history-card">
        <template #header>
          <span>操作履歴</span>
        </template>
        
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in activities"
            :key="index"
            :timestamp="formatDateTime(activity.timestamp)"
            :type="getActivityType(activity.type)"
          >
            {{ activity.description }}
          </el-timeline-item>
        </el-timeline>
        
        <div v-if="activities.length === 0" class="no-data">
          操作履歴がありません
        </div>
      </el-card>
    </div>

    <div v-else class="error">
      <el-result
        icon="error"
        title="取引が見つかりません"
        sub-title="指定された取引IDのデータが存在しません"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/transactions')">
            取引一覧に戻る
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { transactionApi, contractApi, propertyApi } from '@/services/api'
import { TransactionType, TransactionStatus, ContractType, ContractStatus, PropertyType, PropertyStatus } from '@/types'
import type { Transaction, Contract, Property } from '@/types'

const route = useRoute()
const router = useRouter()

// モックデータ（バックエンドが起動できない場合の代替）
const mockTransaction: Transaction = {
  id: 1,
  contractId: 1,
  type: TransactionType.PAYMENT,
  amount: 5000000,
  transactionDate: '2024-01-15',
  description: '物件購入の支払い',
  status: TransactionStatus.COMPLETED,
  createdAt: '2024-01-15T10:00:00Z',
  updatedAt: '2024-01-15T10:00:00Z'
}

const mockContract: Contract = {
  id: 1,
  contractNumber: 'CTR-2024-001',
  propertyId: 1,
  clientId: 1,
  propertyName: 'サンプルマンション',
  clientName: '田中太郎',
  type: ContractType.SALE,
  status: ContractStatus.ACTIVE,
  amount: 5000000,
  startDate: '2024-01-01',
  endDate: '2024-12-31',
  terms: '標準的な売買契約条件',
  createdAt: '2024-01-01T09:00:00Z',
  updatedAt: '2024-01-01T09:00:00Z'
}

const mockProperty: Property = {
  id: 1,
  name: 'サンプルマンション',
  address: '東京都渋谷区1-1-1',
  description: '駅徒歩5分の便利なマンション',
  type: PropertyType.APARTMENT,
  status: PropertyStatus.SOLD,
  price: 5000000,
  area: 80,
  rooms: 3,
  bathrooms: 2,
  parkingSpaces: 1,
  yearBuilt: 2020,
  createdAt: '2024-01-01T08:00:00Z',
  updatedAt: '2024-01-01T08:00:00Z'
}

const transaction = ref<Transaction | null>(null)
const contract = ref<Contract | null>(null)
const property = ref<Property | null>(null)
const loading = ref(false)
const activities = ref([
  {
    timestamp: new Date(),
    type: 'info',
    description: '取引が作成されました'
  }
])

const transactionId = computed(() => route.params.id as string)

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

onMounted(() => {
  if (transactionId.value) {
    loadTransaction(parseInt(transactionId.value))
  }
})

const loadTransaction = async (id: number) => {
  loading.value = true
  console.log('取引データ取得開始:', id)
  
  try {
    const response = await transactionApi.getById(id)
    console.log('APIレスポンス:', response)
    
    if (response.data) {
      transaction.value = response.data
      console.log('取引データ設定完了:', transaction.value)
      
      // 関連データも読み込む
      if (transaction.value.contractId) {
        console.log('契約データ読み込み開始:', transaction.value.contractId)
        loadContract()
      }
    } else {
      console.error('レスポンスにデータがありません:', response)
      ElMessage.error('取引データが見つかりません')
    }
  } catch (error: any) {
    console.error('取引データ取得エラー:', error)
    ElMessage.error('取引データの取得に失敗しました')
    
    // エラーの詳細を表示
    if (error.response) {
      console.error('エラーレスポンス:', error.response.status, error.response.data)
    }
    
    // モックデータを使用（バックエンドが起動できない場合）
    console.log('モックデータを使用します')
    transaction.value = mockTransaction
    contract.value = mockContract
    property.value = mockProperty
  } finally {
    loading.value = false
  }
}

const loadContract = async () => {
  if (!transaction.value?.contractId) return
  
  try {
    const response = await contractApi.getById(transaction.value.contractId)
    contract.value = response.data
    
    // 物件情報も読み込む
    if (contract.value.propertyId) {
      loadProperty()
    }
  } catch (error: any) {
    console.error('契約データの取得に失敗しました:', error)
    ElMessage.error('契約データの取得に失敗しました')
    
    // モックデータを使用
    contract.value = mockContract
    property.value = mockProperty
  }
}

const loadProperty = async () => {
  if (!contract.value?.propertyId) return
  
  try {
    const response = await propertyApi.getById(contract.value.propertyId)
    property.value = response.data
  } catch (error: any) {
    console.error('物件データの取得に失敗しました:', error)
    ElMessage.error('物件データの取得に失敗しました')
    
    // モックデータを使用
    property.value = mockProperty
  }
}

const viewContract = (contractId: number) => {
  router.push(`/contracts/${contractId}`)
}

const editTransaction = () => {
  if (transaction.value?.id) {
    router.push(`/transactions/${transaction.value.id}/edit`)
  }
}

const deleteTransaction = async () => {
  if (!transaction.value?.id) return
  
  try {
    await ElMessageBox.confirm(
      `取引ID: ${transaction.value.id} を削除しますか？`,
      '確認',
      {
        confirmButtonText: '削除',
        cancelButtonText: 'キャンセル',
        type: 'warning'
      }
    )
    
    await transactionApi.delete(transaction.value.id)
    ElMessage.success('取引を削除しました')
    router.push('/transactions')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取引の削除に失敗しました')
      console.error(error)
    }
  }
}

const getTypeLabel = (type: TransactionType) => {
  const found = transactionTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getStatusLabel = (status: TransactionStatus) => {
  const found = transactionStatuses.find(s => s.value === status)
  return found ? found.label : status
}

const getTypeTagType = (type: TransactionType) => {
  const typeMap: Record<TransactionType, string> = {
    PAYMENT: 'success',
    REFUND: 'warning',
    COMMISSION: 'primary',
    MAINTENANCE: 'info',
    INSURANCE: 'danger',
    TAX: ''
  }
  return typeMap[type] || ''
}

const getStatusTagType = (status: TransactionStatus) => {
  const statusMap: Record<TransactionStatus, string> = {
    PENDING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return statusMap[status] || ''
}

const getActivityType = (type: string) => {
  const typeMap: Record<string, string> = {
    info: 'primary',
    success: 'success',
    warning: 'warning',
    error: 'danger'
  }
  return typeMap[type] || 'info'
}

const formatDate = (date: string) => {
  if (!date) return '不明'
  return new Date(date).toLocaleDateString('ja-JP')
}

const formatDateTime = (date: Date) => {
  return date.toLocaleString('ja-JP')
}
</script>

<style scoped>
.transaction-detail {
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
  flex-grow: 1;
}

.actions {
  display: flex;
  gap: 10px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.related-card h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.contract-info p,
.property-info p {
  margin: 8px 0;
  line-height: 1.5;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.history-card .el-timeline {
  padding: 20px 0;
}

.loading {
  padding: 40px;
}

.error {
  padding: 40px;
}
</style>
