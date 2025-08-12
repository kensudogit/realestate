<template>
  <div class="client-detail" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="$router.go(-1)" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            戻る
          </el-button>
          <h1>顧客詳細</h1>
          <p>顧客の詳細情報を表示します</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="editClient" class="edit-btn">
            <el-icon><Edit /></el-icon>
            編集
          </el-button>
          <el-button type="danger" @click="deleteClient" class="delete-btn">
            <el-icon><Delete /></el-icon>
            削除
          </el-button>
        </div>
      </div>
    </div>

    <!-- ローディング表示 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- エラー表示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      class="error-alert"
    />

    <!-- 顧客詳細情報 -->
    <div v-if="client && !loading" class="detail-content">
      <!-- 基本情報カード -->
      <el-card class="info-card" v-motion="'slide-visible-delay-100'">
        <template #header>
          <div class="card-header">
            <h3>基本情報</h3>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="顧客ID">
            {{ client.id }}
          </el-descriptions-item>
          <el-descriptions-item label="顧客タイプ">
            <el-tag :type="getClientTypeColor(client.type)">
              {{ getClientTypeLabel(client.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="姓">
            {{ client.firstName }}
          </el-descriptions-item>
          <el-descriptions-item label="名">
            {{ client.lastName }}
          </el-descriptions-item>
          <el-descriptions-item label="メールアドレス">
            <a :href="`mailto:${client.email}`">{{ client.email }}</a>
          </el-descriptions-item>
          <el-descriptions-item label="電話番号">
            <a :href="`tel:${client.phone}`">{{ client.phone }}</a>
          </el-descriptions-item>
          <el-descriptions-item label="住所" :span="2">
            {{ client.address || '住所が設定されていません' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 関連契約カード -->
      <el-card class="contracts-card" v-motion="'slide-visible-delay-200'">
        <template #header>
          <div class="card-header">
            <h3>関連契約</h3>
            <span class="contract-count">{{ contracts.length }}件</span>
          </div>
        </template>
        
        <div v-if="contracts.length === 0" class="no-contracts">
          <el-empty description="関連する契約がありません" />
        </div>
        
        <el-table v-else :data="contracts" stripe class="contracts-table">
          <el-table-column prop="contractNumber" label="契約番号" min-width="150" />
          <el-table-column prop="propertyName" label="物件名" min-width="200" />
          <el-table-column prop="type" label="契約タイプ" width="120">
            <template #default="{ row }">
              <el-tag :type="getContractTypeColor(row.type)">
                {{ getContractTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="ステータス" width="120">
            <template #default="{ row }">
              <el-tag :type="getContractStatusColor(row.status)">
                {{ getContractStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金額" width="120">
            <template #default="{ row }">
              {{ formatCurrency(row.amount) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="viewContract(row.id)">
                詳細
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- システム情報カード -->
      <el-card class="system-card" v-motion="'slide-visible-delay-300'">
        <template #header>
          <div class="card-header">
            <h3>システム情報</h3>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作成日時">
            {{ formatDateTime(client.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新日時">
            {{ formatDateTime(client.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <!-- 削除確認ダイアログ -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="顧客削除の確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-confirmation">
        <p>以下の顧客を削除してもよろしいですか？</p>
        <div class="client-info">
          <strong>{{ client?.firstName }} {{ client?.lastName }}</strong>
          <br>
          <small>{{ client?.email }}</small>
        </div>
        <p class="warning-text">この操作は取り消すことができません。</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">キャンセル</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleting">
          削除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete } from '@element-plus/icons-vue'
import type { Client, Contract } from '@/types'
import { clientApi, contractApi } from '@/services/api'
import { ClientType, ContractType, ContractStatus } from '@/types'

// ルーター
const route = useRoute()
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ状態
const client = ref<Client | null>(null)
const contracts = ref<Contract[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const deleting = ref(false)
const deleteDialogVisible = ref(false)

// モックデータ（バックエンドが起動できない場合の代替）
const mockClient: Client = {
  id: 1,
  firstName: '田中',
  lastName: '太郎',
  email: 'tanaka@example.com',
  phone: '090-1234-5678',
  address: '東京都渋谷区1-1-1',
  type: ClientType.BUYER,
  createdAt: '2024-01-01T09:00:00Z',
  updatedAt: '2024-01-01T09:00:00Z'
}

const mockContracts: Contract[] = [
  {
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
]

// 初期化
onMounted(() => {
  loadClient()
  checkDarkMode()
})

// ダークモードチェック
const checkDarkMode = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
}

// 顧客データ読み込み
const loadClient = async () => {
  const clientId = route.params.id as string
  if (!clientId) {
    error.value = '顧客IDが指定されていません'
    return
  }

  try {
    loading.value = true
    error.value = null
    
    // モックデータを使用
    client.value = mockClient
    contracts.value = mockContracts

    // 関連契約も読み込み
    // await loadContracts() // モックデータの場合は不要
  } catch (err) {
    console.error('顧客データの読み込みに失敗しました:', err)
    error.value = '顧客データの読み込みに失敗しました'
  } finally {
    loading.value = false
  }
}

// 関連契約読み込み
const loadContracts = async () => {
  if (!client.value) return
  
  try {
    const response = await contractApi.getContracts()
    const allContracts = response.data || []
    
    // この顧客に関連する契約をフィルタリング
    contracts.value = allContracts.filter(contract => 
      contract.client?.id === client.value?.id
    )
  } catch (err) {
    console.error('契約データの読み込みに失敗しました:', err)
  }
}

// 顧客編集
const editClient = () => {
  if (client.value) {
    router.push(`/clients/${client.value.id}/edit`)
  }
}

// 顧客削除
const deleteClient = () => {
  deleteDialogVisible.value = true
}

// 削除確認
const confirmDelete = async () => {
  if (!client.value) return
  
  try {
    deleting.value = true
    await clientApi.delete(client.value.id!)
    
    ElMessage.success('顧客を削除しました')
    deleteDialogVisible.value = false
    
    // 一覧ページに戻る
    router.push('/clients')
  } catch (err) {
    console.error('顧客の削除に失敗しました:', err)
    ElMessage.error('顧客の削除に失敗しました')
  } finally {
    deleting.value = false
  }
}

// 契約詳細表示
const viewContract = (contractId: number) => {
  router.push(`/contracts/${contractId}`)
}

// 顧客タイプの色を取得
const getClientTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    'BUYER': 'success',
    'SELLER': 'warning',
    'TENANT': 'info',
    'LANDLORD': 'primary'
  }
  return colors[type] || 'default'
}

// 顧客タイプのラベルを取得
const getClientTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'BUYER': '買主',
    'SELLER': '売主',
    'TENANT': '賃借人',
    'LANDLORD': '貸主'
  }
  return labels[type] || type
}

// 契約タイプの色を取得
const getContractTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    'SALE': 'success',
    'RENTAL': 'info',
    'LEASE': 'warning',
    'MANAGEMENT': 'primary'
  }
  return colors[type] || 'default'
}

// 契約タイプのラベルを取得
const getContractTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'SALE': '売買',
    'RENTAL': '賃貸',
    'LEASE': 'リース',
    'MANAGEMENT': '管理'
  }
  return labels[type] || type
}

// 契約ステータスの色を取得
const getContractStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'DRAFT': 'info',
    'ACTIVE': 'success',
    'EXPIRED': 'warning',
    'TERMINATED': 'danger',
    'PENDING': 'primary'
  }
  return colors[status] || 'default'
}

// 契約ステータスのラベルを取得
const getContractStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'DRAFT': '下書き',
    'ACTIVE': '有効',
    'EXPIRED': '期限切れ',
    'TERMINATED': '終了',
    'PENDING': '保留'
  }
  return labels[status] || status
}

// 通貨フォーマット
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ja-JP', {
    style: 'currency',
    currency: 'JPY'
  }).format(amount)
}

// 日時フォーマット
const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.client-detail {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.dark-mode {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: #ecf0f1;
}

.page-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  margin-right: 10px;
}

.header-left h1 {
  margin: 0;
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-left p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 1.1rem;
}

.dark-mode .header-left p {
  color: #bdc3c7;
}

.header-right {
  display: flex;
  gap: 10px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.contracts-card,
.system-card {
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: none;
}

.dark-mode .info-card,
.dark-mode .contracts-card,
.dark-mode .system-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.dark-mode .card-header h3 {
  color: #ecf0f1;
}

.contract-count {
  background: #667eea;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.contracts-table {
  margin-top: 20px;
}

.no-contracts {
  text-align: center;
  padding: 40px 20px;
}

.loading-container {
  padding: 40px;
}

.error-alert {
  margin-bottom: 20px;
}

.delete-confirmation {
  text-align: center;
}

.client-info {
  margin: 20px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.dark-mode .client-info {
  background: rgba(255, 255, 255, 0.1);
}

.warning-text {
  color: #e74c3c;
  font-weight: 600;
  margin-top: 15px;
}

.dark-mode .warning-text {
  color: #ff6b6b;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .client-detail {
    padding: 15px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-left h1 {
    font-size: 2rem;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
