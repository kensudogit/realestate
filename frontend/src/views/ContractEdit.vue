<template>
  <div class="contract-edit" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダー -->
    <div class="page-header">
      <div class="header-content">
        <el-button @click="$router.go(-1)" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          戻る
        </el-button>
        <h1>{{ isEdit ? '契約編集' : '新規契約作成' }}</h1>
      </div>
    </div>

    <div class="content-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="contract-form"
        v-motion="'slide-visible'"
      >
        <!-- 基本情報セクション -->
        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span>基本情報</span>
            </div>
          </template>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="契約タイプ" prop="type">
                <el-select v-model="form.type" placeholder="契約タイプを選択" class="full-width">
                  <el-option
                    v-for="type in contractTypes"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="契約状態" prop="status">
                <el-select v-model="form.status" placeholder="契約状態を選択" class="full-width">
                  <el-option
                    v-for="status in contractStatuses"
                    :key="status.value"
                    :label="status.label"
                    :value="status.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="契約金額" prop="amount">
                <el-input-number
                  v-model="form.amount"
                  :min="0"
                  :precision="0"
                  :step="100000"
                  class="full-width"
                  placeholder="契約金額を入力"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="契約条件" prop="terms">
                <el-input
                  v-model="form.terms"
                  placeholder="契約条件を入力"
                  class="full-width"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="開始日" prop="startDate">
                <el-date-picker
                  v-model="form.startDate"
                  type="date"
                  placeholder="開始日を選択"
                  class="full-width"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="終了日" prop="endDate">
                <el-date-picker
                  v-model="form.endDate"
                  type="date"
                  placeholder="終了日を選択"
                  class="full-width"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 関連情報セクション -->
        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span>関連情報</span>
            </div>
          </template>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="物件" prop="propertyId">
                <el-select
                  v-model="form.propertyId"
                  placeholder="物件を選択"
                  class="full-width"
                  filterable
                  remote
                  :remote-method="searchProperties"
                  :loading="propertyLoading"
                  @change="onPropertyChange"
                >
                  <el-option
                    v-for="property in properties"
                    :key="property.id"
                    :label="`${property.name} - ${property.address}`"
                    :value="property.id"
                  >
                    <div class="property-option">
                      <div class="property-name">{{ property.name }}</div>
                      <div class="property-address">{{ property.address }}</div>
                      <div class="property-price">¥{{ formatNumber(property.price) }}</div>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="クライアント" prop="clientId">
                <el-select
                  v-model="form.clientId"
                  placeholder="クライアントを選択"
                  class="full-width"
                  filterable
                  remote
                  :remote-method="searchClients"
                  :loading="clientLoading"
                  @change="onClientChange"
                >
                  <el-option
                    v-for="client in clients"
                    :key="client.id"
                    :label="`${client.lastName} ${client.firstName}`"
                    :value="client.id"
                  >
                    <div class="client-option">
                      <div class="client-name">{{ client.lastName }} {{ client.firstName }}</div>
                      <div class="client-email">{{ client.email }}</div>
                      <div class="client-type">
                        <el-tag :type="getClientTypeTagType(client.type)" size="small">
                          {{ getClientTypeLabel(client.type) }}
                        </el-tag>
                      </div>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 選択された物件・クライアントの詳細表示 -->
          <div v-if="selectedProperty || selectedClient" class="selection-details">
            <el-row :gutter="24">
              <el-col :span="12" v-if="selectedProperty">
                <div class="detail-card">
                  <h4>選択された物件</h4>
                  <div class="detail-content">
                    <p><strong>名称:</strong> {{ selectedProperty.name }}</p>
                    <p><strong>住所:</strong> {{ selectedProperty.address }}</p>
                    <p><strong>価格:</strong> ¥{{ formatNumber(selectedProperty.price) }}</p>
                    <p><strong>タイプ:</strong> {{ getPropertyTypeLabel(selectedProperty.type) }}</p>
                  </div>
                </div>
              </el-col>
              <el-col :span="12" v-if="selectedClient">
                <div class="detail-card">
                  <h4>選択されたクライアント</h4>
                  <div class="detail-content">
                    <p><strong>氏名:</strong> {{ selectedClient.lastName }} {{ selectedClient.firstName }}</p>
                    <p><strong>メール:</strong> {{ selectedClient.email }}</p>
                    <p><strong>電話:</strong> {{ selectedClient.phone }}</p>
                    <p><strong>タイプ:</strong> {{ getClientTypeLabel(selectedClient.type) }}</p>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 追加情報セクション -->
        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span>追加情報</span>
            </div>
          </template>
          
          <el-form-item label="備考">
            <el-input
              v-model="form.notes"
              type="textarea"
              :rows="4"
              placeholder="契約に関する備考を入力"
              class="full-width"
            />
          </el-form-item>
        </el-card>

        <!-- アクションボタン -->
        <div class="form-actions">
          <el-button @click="$router.go(-1)" size="large">
            キャンセル
          </el-button>
          <el-button type="primary" @click="saveContract" size="large" :loading="saving">
            {{ isEdit ? '更新' : '作成' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { contractApi, propertyApi, clientApi } from '@/services/api'
import { Contract, Property, Client, ContractType, ContractStatus, PropertyType, ClientType } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const route = useRoute()
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// フォーム関連
const formRef = ref<FormInstance>()
const saving = ref(false)

// データ
const properties = ref<Property[]>([])
const clients = ref<Client[]>([])
const selectedProperty = ref<Property | null>(null)
const selectedClient = ref<Client | null>(null)

// ローディング状態
const propertyLoading = ref(false)
const clientLoading = ref(false)

// 編集モードかどうか
const isEdit = computed(() => !!route.params.id)

// フォームデータ
const form = reactive({
  type: ContractType.SALE,
  status: ContractStatus.DRAFT,
  amount: 0,
  startDate: '',
  endDate: '',
  terms: '',
  propertyId: undefined as number | undefined,
  clientId: undefined as number | undefined,
  notes: ''
})

// バリデーションルール
const rules: FormRules = {
  type: [
    { required: true, message: '契約タイプを選択してください', trigger: 'change' }
  ],
  status: [
    { required: true, message: '契約状態を選択してください', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '契約金額を入力してください', trigger: 'blur' },
    { type: 'number', min: 1, message: '契約金額は1円以上で入力してください', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '開始日を選択してください', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '終了日を選択してください', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (form.startDate && value && new Date(value) <= new Date(form.startDate)) {
          callback(new Error('終了日は開始日より後である必要があります'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  terms: [
    { required: true, message: '契約条件を入力してください', trigger: 'blur' }
  ],
  propertyId: [
    { required: true, message: '物件を選択してください', trigger: 'change' }
  ],
  clientId: [
    { required: true, message: 'クライアントを選択してください', trigger: 'change' }
  ]
}

// 選択肢データ
const contractTypes = [
  { value: ContractType.SALE, label: '売買' },
  { value: ContractType.RENTAL, label: '賃貸' },
  { value: ContractType.LEASE, label: 'リース' },
  { value: ContractType.MANAGEMENT, label: '管理' }
]

const contractStatuses = [
  { value: ContractStatus.DRAFT, label: '下書き' },
  { value: ContractStatus.PENDING, label: '保留中' },
  { value: ContractStatus.ACTIVE, label: '有効' },
  { value: ContractStatus.EXPIRED, label: '期限切れ' },
  { value: ContractStatus.TERMINATED, label: '終了' }
]

onMounted(async () => {
  if (isEdit.value) {
    await loadContractData()
  }
  await loadInitialData()
})

const loadContractData = async () => {
  try {
    const contractId = Number(route.params.id)
    const response = await contractApi.getById(contractId)
    const contract = response.data
    
    // フォームにデータを設定
    Object.assign(form, {
      type: contract.type,
      status: contract.status,
      amount: contract.amount,
      startDate: contract.startDate,
      endDate: contract.endDate,
      terms: contract.terms,
      propertyId: contract.propertyId,
      clientId: contract.clientId,
      notes: contract.notes || ''
    })
    
    // 関連データを取得
    if (contract.propertyId) {
      await loadProperty(contract.propertyId)
    }
    if (contract.clientId) {
      await loadClient(contract.clientId)
    }
  } catch (error) {
    console.error('契約データの取得に失敗しました:', error)
    ElMessage.error('契約データの取得に失敗しました')
  }
}

const loadInitialData = async () => {
  await Promise.all([
    searchProperties(''),
    searchClients('')
  ])
}

const loadProperty = async (propertyId: number) => {
  try {
    const response = await propertyApi.getById(propertyId)
    selectedProperty.value = response.data
  } catch (error) {
    console.error('物件情報の取得に失敗しました:', error)
  }
}

const loadClient = async (clientId: number) => {
  try {
    const response = await clientApi.getById(clientId)
    selectedClient.value = response.data
  } catch (error) {
    console.error('クライアント情報の取得に失敗しました:', error)
  }
}

const searchProperties = async (query: string) => {
  try {
    propertyLoading.value = true
    let response
    if (query) {
      response = await propertyApi.search(query)
    } else {
      response = await propertyApi.getAll()
    }
    properties.value = response.data
  } catch (error) {
    console.error('物件検索に失敗しました:', error)
    properties.value = []
  } finally {
    propertyLoading.value = false
  }
}

const searchClients = async (query: string) => {
  try {
    clientLoading.value = true
    let response
    if (query) {
      response = await clientApi.search ? await clientApi.search(query) : await clientApi.getAll()
    } else {
      response = await clientApi.getAll()
    }
    clients.value = response.data
  } catch (error) {
    console.error('クライアント検索に失敗しました:', error)
    clients.value = []
  } finally {
    clientLoading.value = false
  }
}

const onPropertyChange = async (propertyId: number) => {
  if (propertyId) {
    await loadProperty(propertyId)
  } else {
    selectedProperty.value = null
  }
}

const onClientChange = async (clientId: number) => {
  if (clientId) {
    await loadClient(clientId)
  } else {
    selectedClient.value = null
  }
}

const saveContract = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    if (isEdit.value) {
      // 更新
      const contractId = Number(route.params.id)
      await contractApi.update(contractId, form as Contract)
      ElMessage.success('契約を更新しました')
    } else {
      // 新規作成
      await contractApi.create(form as Contract)
      ElMessage.success('契約を作成しました')
    }
    
    router.push('/contracts')
  } catch (error) {
    console.error('契約の保存に失敗しました:', error)
    ElMessage.error('契約の保存に失敗しました')
  } finally {
    saving.value = false
  }
}

// ラベル取得関数
const getClientTypeLabel = (type: ClientType) => {
  const typeMap: Record<ClientType, string> = {
    BUYER: '買主',
    SELLER: '売主',
    TENANT: '賃借人',
    LANDLORD: '貸主'
  }
  return typeMap[type] || type
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

const getClientTypeTagType = (type: ClientType) => {
  const typeMap: Record<ClientType, string> = {
    BUYER: 'success',
    SELLER: 'warning',
    TENANT: 'primary',
    LANDLORD: 'info'
  }
  return typeMap[type] || ''
}

// ユーティリティ関数
const formatNumber = (num: number) => {
  return num.toLocaleString()
}
</script>

<style scoped>
.contract-edit {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.contract-edit.dark-mode {
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 12px;
  font-weight: 600;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
}

.contract-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
}

.dark-mode .form-section {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.section-header {
  font-weight: 600;
  font-size: 1.1rem;
  color: #303133;
}

.dark-mode .section-header {
  color: #ffffff;
}

.full-width {
  width: 100%;
}

.property-option, .client-option {
  padding: 8px 0;
}

.property-name, .client-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.dark-mode .property-name,
.dark-mode .client-name {
  color: #ffffff;
}

.property-address, .client-email {
  font-size: 0.85rem;
  color: #606266;
  margin-bottom: 4px;
}

.dark-mode .property-address,
.dark-mode .client-email {
  color: #bdc3c7;
}

.property-price, .client-type {
  font-size: 0.85rem;
  color: #e74c3c;
  font-weight: 600;
}

.selection-details {
  margin-top: 20px;
}

.detail-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e9ecef;
}

.dark-mode .detail-card {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

.detail-card h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 1rem;
  font-weight: 600;
}

.dark-mode .detail-card h4 {
  color: #ffffff;
}

.detail-content p {
  margin: 4px 0;
  font-size: 0.9rem;
  color: #606266;
}

.dark-mode .detail-content p {
  color: #bdc3c7;
}

.detail-content strong {
  color: #303133;
  font-weight: 600;
}

.dark-mode .detail-content strong {
  color: #ffffff;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 32px 0;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .contract-edit {
    padding: 16px;
  }
  
  .content-container {
    max-width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .detail-card {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-actions {
    padding: 24px 0;
  }
}
</style>
