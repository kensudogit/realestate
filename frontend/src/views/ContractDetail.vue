<template>
  <div class="contract-detail" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダー -->
    <div class="page-header">
      <div class="header-content">
        <el-button @click="$router.go(-1)" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          戻る
        </el-button>
        <h1>契約詳細</h1>
        <div class="header-actions">
          <el-button type="primary" @click="editContract">
            <el-icon><Edit /></el-icon>
            編集
          </el-button>
          <el-button type="danger" @click="deleteContract" :disabled="!canDelete">
            <el-icon><Delete /></el-icon>
            削除
          </el-button>
        </div>
      </div>
    </div>

    <div class="content-container">
      <!-- 契約基本情報 -->
      <el-card class="info-card" v-motion="'slide-visible'">
        <template #header>
          <div class="card-header">
            <span>契約基本情報</span>
            <el-tag :type="getStatusTagType(contract.status)" size="large">
              {{ getStatusLabel(contract.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="info-item">
              <label>契約ID</label>
              <span class="value">{{ contract.id }}</span>
            </div>
            <div class="info-item">
              <label>契約タイプ</label>
              <el-tag :type="getTypeTagType(contract.type)" size="large">
                {{ getTypeLabel(contract.type) }}
              </el-tag>
            </div>
            <div class="info-item">
              <label>契約金額</label>
              <span class="value amount">¥{{ formatNumber(contract.amount) }}</span>
            </div>
            <div class="info-item">
              <label>開始日</label>
              <span class="value">{{ formatDate(contract.startDate) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>契約状態</label>
              <span class="value">{{ getStatusLabel(contract.status) }}</span>
            </div>
            <div class="info-item">
              <label>終了日</label>
              <span class="value">{{ formatDate(contract.endDate) }}</span>
            </div>
            <div class="info-item">
              <label>契約条件</label>
              <span class="value">{{ contract.terms }}</span>
            </div>
            <div class="info-item">
              <label>作成日</label>
              <span class="value">{{ formatDate(contract.createdAt) }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 関連物件情報 -->
      <el-card class="info-card" v-motion="'slide-visible-delay-200'">
        <template #header>
          <div class="card-header">
            <span>関連物件情報</span>
            <el-button type="text" @click="viewProperty">
              詳細を見る
            </el-button>
          </div>
        </template>
        
        <div v-if="property" class="property-info">
          <div class="property-header">
            <h3>{{ property.name }}</h3>
            <el-tag :type="getPropertyStatusTagType(property.status)">
              {{ getPropertyStatusLabel(property.status) }}
            </el-tag>
          </div>
          <div class="property-details">
            <div class="detail-item">
              <label>住所</label>
              <span>{{ property.address }}</span>
            </div>
            <div class="detail-item">
              <label>物件タイプ</label>
              <el-tag :type="getPropertyTypeTagType(property.type)">
                {{ getPropertyTypeLabel(property.type) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>価格</label>
              <span class="price">¥{{ formatNumber(property.price) }}</span>
            </div>
            <div class="detail-item">
              <label>面積</label>
              <span>{{ property.area }}㎡</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="物件情報が見つかりません" />
          <div class="empty-actions">
            <el-button type="primary" @click="loadProperty">
              <el-icon><Refresh /></el-icon>
              物件情報を読み込む
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 関連クライアント情報 -->
      <el-card class="info-card" v-motion="'slide-visible-delay-400'">
        <template #header>
          <div class="card-header">
            <span>関連クライアント情報</span>
            <el-button type="text" @click="viewClient">
              詳細を見る
            </el-button>
          </div>
        </template>
        
        <div v-if="client" class="client-info">
          <div class="client-header">
            <h3>{{ client.lastName }} {{ client.firstName }}</h3>
            <el-tag :type="getClientTypeTagType(client.type)">
              {{ getClientTypeLabel(client.type) }}
            </el-tag>
          </div>
          <div class="client-details">
            <div class="detail-item">
              <label>メールアドレス</label>
              <span>{{ client.email }}</span>
            </div>
            <div class="detail-item">
              <label>電話番号</label>
              <span>{{ client.phone }}</span>
            </div>
            <div class="detail-item">
              <label>住所</label>
              <span>{{ client.address }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="クライアント情報が見つかりません" />
          <div class="empty-actions">
            <el-button type="primary" @click="loadClient">
              <el-icon><Refresh /></el-icon>
              クライアント情報を読み込む
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 取引履歴 -->
      <el-card class="info-card" v-motion="'slide-visible-delay-600'">
        <template #header>
          <div class="card-header">
            <span>取引履歴</span>
            <el-button type="primary" @click="addTransaction">
              <el-icon><Plus /></el-icon>
              取引追加
            </el-button>
          </div>
        </template>
        
        <div class="transaction-list">
          <div v-if="transactions.length === 0" class="empty-state">
            <el-empty description="取引履歴がありません" />
          </div>
          <div v-else>
            <div
              v-for="transaction in transactions"
              :key="transaction.id"
              class="transaction-item"
            >
              <div class="transaction-info">
                <div class="transaction-header">
                  <el-tag :type="getTransactionTypeTagType(transaction.type)" size="small">
                    {{ getTransactionTypeLabel(transaction.type) }}
                  </el-tag>
                  <span class="transaction-date">{{ formatDate(transaction.transactionDate) }}</span>
                </div>
                <div class="transaction-details">
                  <span class="transaction-amount">¥{{ formatNumber(transaction.amount) }}</span>
                  <span class="transaction-description">{{ transaction.description }}</span>
                </div>
                <div class="transaction-status">
                  <el-tag :type="getTransactionStatusTagType(transaction.status)" size="small">
                    {{ getTransactionStatusLabel(transaction.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 削除確認ダイアログ -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="契約削除の確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>この契約を削除してもよろしいですか？</p>
      <p class="warning-text">この操作は取り消すことができません。</p>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">キャンセル</el-button>
          <el-button type="danger" @click="confirmDelete">削除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { contractApi, propertyApi, clientApi, transactionApi } from '@/services/api'
import { Contract, Property, Client, Transaction, ContractType, ContractStatus, PropertyType, PropertyStatus, ClientType, TransactionType, TransactionStatus } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ
const contract = ref<Contract>({} as Contract)
const property = ref<Property | null>(null)
const client = ref<Client | null>(null)
const transactions = ref<Transaction[]>([])

// ダイアログ状態
const deleteDialogVisible = ref(false)

// 削除可能かどうか
const canDelete = computed(() => {
  return contract.value.status === ContractStatus.DRAFT || contract.value.status === ContractStatus.PENDING
})

// モックデータ（バックエンドが起動できない場合の代替）
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

const mockTransactions: Transaction[] = [
  {
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
]

onMounted(async () => {
  // ダークモードの設定
  isDarkMode.value = document.documentElement.classList.contains('dark')
  
  // 初期データとしてモックデータを設定
  console.log('モックデータを初期設定します')
  contract.value = mockContract
  property.value = mockProperty
  client.value = mockClient
  transactions.value = mockTransactions
  
  console.log('設定されたデータ:', {
    contract: contract.value,
    property: property.value,
    client: client.value,
    transactions: transactions.value
  })
  
  const contractId = Number(route.params.id)
  if (contractId) {
    // 実際のAPIが利用可能になったら以下のコードを使用
    // await loadContractData(contractId)
  }
})

const loadContractData = async (contractId: number) => {
  try {
    // モックデータを使用（バックエンドが起動できない場合）
    console.log('モックデータを使用します')
    contract.value = mockContract
    property.value = mockProperty
    client.value = mockClient
    transactions.value = mockTransactions
    
    // 実際のAPIが利用可能になったら以下のコードを使用
    /*
    // 契約情報を取得
    const contractResponse = await contractApi.getById(contractId)
    contract.value = contractResponse.data

    // 関連する物件情報を取得
    if (contract.value.propertyId) {
      try {
        const propertyResponse = await propertyApi.getById(contract.value.propertyId)
        property.value = propertyResponse.data
      } catch (error) {
        console.error('物件情報の取得に失敗しました:', error)
      }
    }

    // 関連するクライアント情報を取得
    if (contract.value.clientId) {
      try {
        const clientResponse = await clientApi.getById(contract.value.clientId)
        client.value = clientResponse.data
      } catch (error) {
        console.error('クライアント情報の取得に失敗しました:', error)
      }
    }

    // 取引履歴を取得
    try {
      const transactionsResponse = await transactionApi.getAll()
      transactions.value = transactionsResponse.data.filter(t => t.contractId === contractId)
    } catch (error) {
      console.error('取引履歴の取得に失敗しました:', error)
      // ダミーデータを使用
      transactions.value = [
        {
          id: 1,
          contractId: contractId,
          type: TransactionType.PAYMENT,
          amount: contract.value.amount,
          transactionDate: contract.value.startDate,
          description: '契約金支払い',
          status: TransactionStatus.COMPLETED,
          createdAt: contract.value.createdAt,
          updatedAt: contract.value.updatedAt
        }
      ]
    }
    */
  } catch (error) {
    console.error('契約情報の取得に失敗しました:', error)
    ElMessage.error('契約情報の取得に失敗しました')
    
    // エラー時もモックデータを使用
    contract.value = mockContract
    property.value = mockProperty
    client.value = mockClient
    transactions.value = mockTransactions
  }
}

const editContract = () => {
  if (contract.value.id) {
    router.push(`/contracts/${contract.value.id}/edit`)
  }
}

const deleteContract = () => {
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    if (contract.value.id) {
      await contractApi.delete(contract.value.id)
      ElMessage.success('契約を削除しました')
      router.push('/contracts')
    }
  } catch (error) {
    console.error('契約の削除に失敗しました:', error)
    ElMessage.error('契約の削除に失敗しました')
  } finally {
    deleteDialogVisible.value = false
  }
}

const viewProperty = () => {
  if (property.value?.id) {
    router.push(`/properties/${property.value.id}`)
  }
}

const viewClient = () => {
  if (client.value?.id) {
    router.push(`/clients/${client.value.id}/edit`)
  }
}

const addTransaction = () => {
  // 取引追加画面への遷移（実装予定）
  ElMessage.info('取引追加機能は実装予定です')
}

const loadProperty = async () => {
  try {
    console.log('物件情報を読み込みます')
    
    // モックデータを使用（バックエンドが起動できない場合）
    if (mockProperty) {
      property.value = mockProperty
      ElMessage.success('物件情報を読み込みました')
      console.log('モックデータで物件情報を設定:', property.value)
    } else {
      ElMessage.warning('物件情報が見つかりませんでした')
    }
    
    // 実際のAPIが利用可能になったら以下のコードを使用
    /*
    if (contract.value.propertyId) {
      const propertyResponse = await propertyApi.getById(contract.value.propertyId)
      property.value = propertyResponse.data
      ElMessage.success('物件情報を読み込みました')
    } else {
      ElMessage.warning('物件IDが見つかりませんでした。')
    }
    */
    
  } catch (error) {
    console.error('物件情報の読み込みに失敗しました:', error)
    ElMessage.error('物件情報の読み込みに失敗しました')
    
    // エラー時もモックデータを使用
    if (mockProperty) {
      property.value = mockProperty
      ElMessage.info('モックデータで物件情報を表示します')
    }
  }
}

const loadClient = async () => {
  try {
    console.log('クライアント情報を読み込みます')
    
    // モックデータを使用（バックエンドが起動できない場合）
    if (mockClient) {
      client.value = mockClient
      ElMessage.success('クライアント情報を読み込みました')
      console.log('モックデータでクライアント情報を設定:', client.value)
    } else {
      ElMessage.warning('クライアント情報が見つかりませんでした')
    }
    
    // 実際のAPIが利用可能になったら以下のコードを使用
    /*
    if (contract.value.clientId) {
      const clientResponse = await clientApi.getById(contract.value.clientId)
      client.value = clientResponse.data
      ElMessage.success('クライアント情報を読み込みました')
    } else {
      ElMessage.warning('クライアントIDが見つかりませんでした。')
    }
    */
    
  } catch (error) {
    console.error('クライアント情報の読み込みに失敗しました:', error)
    ElMessage.error('クライアント情報の読み込みに失敗しました')
    
    // エラー時もモックデータを使用
    if (mockClient) {
      client.value = mockClient
      ElMessage.info('モックデータでクライアント情報を表示します')
    }
  }
}

// ラベル取得関数
const getTypeLabel = (type: ContractType) => {
  const typeMap: Record<ContractType, string> = {
    SALE: '売買',
    RENTAL: '賃貸',
    LEASE: 'リース',
    MANAGEMENT: '管理'
  }
  return typeMap[type] || type
}

const getStatusLabel = (status: ContractStatus) => {
  const statusMap: Record<ContractStatus, string> = {
    DRAFT: '下書き',
    ACTIVE: '有効',
    EXPIRED: '期限切れ',
    TERMINATED: '終了',
    PENDING: '保留中'
  }
  return statusMap[status] || status
}

const getPropertyTypeLabel = (type: PropertyType) => {
  const typeMap: Record<PropertyType, string> = {
    APARTMENT: 'マンション',
    HOUSE: '一戸建て',
    COMMERCIAL: '商業施設',
    LAND: '土地',
    OFFICE: 'オフィス',
    WAREHOUSE: '倉庫'
  }
  return typeMap[type] || type
}

const getPropertyStatusLabel = (status: PropertyStatus) => {
  const statusMap: Record<PropertyStatus, string> = {
    AVAILABLE: '利用可能',
    SOLD: '売却済み',
    RENTED: '賃貸中',
    UNDER_CONTRACT: '契約中',
    MAINTENANCE: 'メンテナンス中'
  }
  return statusMap[status] || status
}

const getClientTypeLabel = (type: ClientType) => {
  const typeMap: Record<ClientType, string> = {
    BUYER: '買主',
    SELLER: '売主',
    TENANT: '賃借人',
    LANDLORD: '貸主'
  }
  return typeMap[type] || type
}

const getTransactionTypeLabel = (type: TransactionType) => {
  const typeMap: Record<TransactionType, string> = {
    PAYMENT: '支払い',
    REFUND: '返金',
    COMMISSION: '手数料',
    MAINTENANCE: 'メンテナンス',
    INSURANCE: '保険',
    TAX: '税金'
  }
  return typeMap[type] || type
}

const getTransactionStatusLabel = (status: TransactionStatus) => {
  const statusMap: Record<TransactionStatus, string> = {
    PENDING: '保留中',
    COMPLETED: '完了',
    FAILED: '失敗',
    CANCELLED: 'キャンセル'
  }
  return statusMap[status] || status
}

// タグタイプ取得関数
const getTypeTagType = (type: ContractType) => {
  const typeMap: Record<ContractType, string> = {
    SALE: 'success',
    RENTAL: 'primary',
    LEASE: 'warning',
    MANAGEMENT: 'info'
  }
  return typeMap[type] || 'info'
}

const getStatusTagType = (status: ContractStatus) => {
  const statusMap: Record<ContractStatus, string> = {
    DRAFT: 'info',
    ACTIVE: 'success',
    EXPIRED: 'warning',
    TERMINATED: 'danger',
    PENDING: 'warning'
  }
  return statusMap[status] || 'info'
}

const getPropertyTypeTagType = (type: PropertyType) => {
  const typeMap: Record<PropertyType, string> = {
    APARTMENT: 'primary',
    HOUSE: 'success',
    COMMERCIAL: 'warning',
    LAND: 'info',
    OFFICE: 'danger',
    WAREHOUSE: 'info'
  }
  return typeMap[type] || 'info'
}

const getPropertyStatusTagType = (status: PropertyStatus) => {
  const statusMap: Record<PropertyStatus, string> = {
    AVAILABLE: 'success',
    SOLD: 'danger',
    RENTED: 'warning',
    UNDER_CONTRACT: 'primary',
    MAINTENANCE: 'info'
  }
  return statusMap[status] || 'info'
}

const getClientTypeTagType = (type: ClientType) => {
  const typeMap: Record<ClientType, string> = {
    BUYER: 'success',
    SELLER: 'warning',
    TENANT: 'primary',
    LANDLORD: 'info'
  }
  return typeMap[type] || 'info'
}

const getTransactionTypeTagType = (type: TransactionType) => {
  const typeMap: Record<TransactionType, string> = {
    PAYMENT: 'success',
    REFUND: 'warning',
    COMMISSION: 'info',
    MAINTENANCE: 'primary',
    INSURANCE: 'success',
    TAX: 'danger'
  }
  return typeMap[type] || 'info'
}

const getTransactionStatusTagType = (status: TransactionStatus) => {
  const statusMap: Record<TransactionStatus, string> = {
    PENDING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return statusMap[status] || 'info'
}

// ユーティリティ関数
const formatNumber = (num: number | undefined | null) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('ja-JP')
}
</script>

<style scoped>
.contract-detail {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.contract-detail.dark-mode {
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 12px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
}

.dark-mode .info-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 1.1rem;
}

.info-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dark-mode .info-item label {
  color: #bdc3c7;
}

.info-item .value {
  font-size: 1.1rem;
  color: #303133;
  font-weight: 500;
}

.dark-mode .info-item .value {
  color: #ffffff;
}

.info-item .amount {
  font-size: 1.3rem;
  font-weight: 700;
  color: #e74c3c;
}

.property-info, .client-info {
  padding: 20px 0;
}

.property-header, .client-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.property-header h3, .client-header h3 {
  margin: 0;
  color: #303133;
  font-size: 1.3rem;
  font-weight: 600;
}

.dark-mode .property-header h3,
.dark-mode .client-header h3 {
  color: #ffffff;
}

.property-details, .client-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item label {
  font-weight: 600;
  color: #606266;
  font-size: 0.85rem;
}

.dark-mode .detail-item label {
  color: #bdc3c7;
}

.detail-item span {
  color: #303133;
  font-size: 1rem;
}

.dark-mode .detail-item span {
  color: #ffffff;
}

.detail-item .price {
  font-weight: 700;
  color: #e74c3c;
}

.transaction-list {
  max-height: 400px;
  overflow-y: auto;
}

.transaction-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.dark-mode .transaction-item {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

.transaction-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.dark-mode .transaction-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.transaction-date {
  font-size: 0.85rem;
  color: #909399;
}

.dark-mode .transaction-date {
  color: #bdc3c7;
}

.transaction-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.transaction-amount {
  font-weight: 700;
  color: #e74c3c;
  font-size: 1.1rem;
}

.transaction-description {
  color: #606266;
  font-size: 0.9rem;
}

.dark-mode .transaction-description {
  color: #bdc3c7;
}

.transaction-status {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.dark-mode .empty-state {
  color: #bdc3c7;
}

.warning-text {
  color: #e6a23c;
  font-weight: 600;
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.empty-actions {
  margin-top: 20px;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .contract-detail {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .property-details, .client-details {
    grid-template-columns: 1fr;
  }
  
  .info-card {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .transaction-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
