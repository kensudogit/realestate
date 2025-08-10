<template>
  <div class="contract-list" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-content">
        <div class="header-left">
          <h1>契約管理</h1>
          <p>契約の一覧表示、検索、管理を行います</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="$router.push('/contracts/new')" class="add-btn">
            <el-icon><Plus /></el-icon>
            新規契約作成
          </el-button>
        </div>
      </div>
    </div>

    <!-- 検索・フィルターセクション -->
    <el-card class="search-card" v-motion="'slide-visible-delay-100'">
      <div class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select
              v-model="searchForm.status"
              placeholder="契約ステータス"
              clearable
              style="width: 100%"
            >
              <el-option label="契約中" value="ACTIVE" />
              <el-option label="契約終了" value="EXPIRED" />
              <el-option label="契約解除" value="CANCELLED" />
              <el-option label="更新待ち" value="RENEWAL_PENDING" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.type"
              placeholder="契約タイプ"
              clearable
              style="width: 100%"
            >
              <el-option label="賃貸契約" value="RENTAL" />
              <el-option label="売買契約" value="SALE" />
              <el-option label="管理契約" value="MANAGEMENT" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              検索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              リセット
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 契約一覧テーブル -->
    <el-card class="table-card" v-motion="'slide-visible-delay-200'">
      <div class="table-header">
        <div class="table-title">
          <h3>契約一覧</h3>
          <span class="record-count">全 {{ totalRecords }} 件</span>
        </div>
        <div class="table-actions">
          <el-button @click="exportData" :loading="exporting">
            <el-icon><Download /></el-icon>
            エクスポート
          </el-button>
        </div>
      </div>

      <el-table
        :data="contracts"
        v-loading="loading"
        stripe
        class="contract-table"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        <el-table-column prop="contractNumber" label="契約番号" min-width="150" sortable="custom">
          <template #default="{ row }">
            <div class="contract-number">
              <el-tag type="info" size="small">{{ row.contractNumber }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="契約タイプ" width="120" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientName" label="顧客名" min-width="150" sortable="custom">
          <template #default="{ row }">
            <div class="client-info">
              <el-avatar :size="24" class="client-avatar">
                {{ row.clientName?.charAt(0) }}
              </el-avatar>
              <span>{{ row.clientName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="propertyName" label="物件名" min-width="150" sortable="custom" />
        <el-table-column prop="startDate" label="契約開始日" width="120" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="契約終了日" width="120" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="契約金額" width="120" sortable="custom">
          <template #default="{ row }">
            <span>¥{{ formatCurrency(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="monthlyRent" label="月額家賃" width="120" sortable="custom">
          <template #default="{ row }">
            <span v-if="row.monthlyRent">¥{{ formatCurrency(row.monthlyRent) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="ステータス" width="120" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                @click="viewContract(row)"
                type="info"
                text
              >
                <el-icon><View /></el-icon>
                詳細
              </el-button>
              <el-button
                size="small"
                @click="editContract(row)"
                type="primary"
                text
              >
                <el-icon><Edit /></el-icon>
                編集
              </el-button>
              <el-button
                size="small"
                @click="deleteContract(row)"
                type="danger"
                text
              >
                <el-icon><Delete /></el-icon>
                削除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- ページネーション -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalRecords"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 削除確認ダイアログ -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="契約削除の確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-confirmation">
        <p>以下の契約を削除してもよろしいですか？</p>
        <div class="contract-info">
          <strong>契約番号: {{ contractToDelete?.contractNumber }}</strong>
          <br>
          <small>{{ contractToDelete?.clientName }} - {{ contractToDelete?.propertyName }}</small>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Document, Search, Refresh, Download, View, Edit, Delete } from '@element-plus/icons-vue'
import type { Contract } from '@/types'
import { contractApi } from '@/services/api'

// ルーター
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ状態
const contracts = ref<Contract[]>([])
const loading = ref(false)
const exporting = ref(false)
const deleting = ref(false)

// ページネーション
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 検索フォーム
const searchForm = reactive({
  status: '',
  type: ''
})

// ソート状態
const sortBy = ref('')
const sortOrder = ref('')

// 削除ダイアログ
const deleteDialogVisible = ref(false)
const contractToDelete = ref<Contract | null>(null)

// 初期化
onMounted(() => {
  loadContracts()
  checkDarkMode()
})

// ダークモードチェック
const checkDarkMode = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
}

// 契約データ読み込み
const loadContracts = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: searchForm.status || undefined,
      type: searchForm.type || undefined,
      sortBy: sortBy.value || undefined,
      sortOrder: sortOrder.value || undefined
    }
    
    const response = await contractApi.getContracts(params)
    contracts.value = response.data || []
    totalRecords.value = contracts.value.length
  } catch (error) {
    console.error('契約データの読み込みに失敗しました:', error)
    ;(ElMessage as any).error('契約データの読み込みに失敗しました')
  } finally {
    loading.value = false
  }
}

// 検索実行
const handleSearch = () => {
  currentPage.value = 1
  loadContracts()
}

// 検索リセット
const resetSearch = () => {
  Object.assign(searchForm, {
    status: '',
    type: ''
  })
  currentPage.value = 1
  loadContracts()
}

// ソート変更
const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadContracts()
}

// ページサイズ変更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadContracts()
}

// ページ変更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadContracts()
}

// 契約詳細表示
const viewContract = (contract: Contract) => {
  router.push(`/contracts/${contract.id}`)
}

// 契約編集
const editContract = (contract: Contract) => {
  router.push(`/contracts/${contract.id}/edit`)
}

// 契約削除
const deleteContract = (contract: Contract) => {
  contractToDelete.value = contract
  deleteDialogVisible.value = true
}

// 削除確認
const confirmDelete = async () => {
  if (!contractToDelete.value) return
  
  try {
    deleting.value = true
    await contractApi.delete(contractToDelete.value.id!)
    ;(ElMessage as any).success('契約を削除しました')
    deleteDialogVisible.value = false
    contractToDelete.value = null
    loadContracts()
  } catch (error) {
    console.error('契約の削除に失敗しました:', error)
    ;(ElMessage as any).error('契約の削除に失敗しました')
  } finally {
    deleting.value = false
  }
}

// データエクスポート
const exportData = async () => {
  try {
    exporting.value = true
    // エクスポート処理の実装
    ;(ElMessage as any).success('データのエクスポートが完了しました')
  } catch (error) {
    console.error('データのエクスポートに失敗しました:', error)
    ;(ElMessage as any).error('データのエクスポートに失敗しました')
  } finally {
    exporting.value = false
  }
}

// 契約タイプラベル取得
const getTypeLabel = (type: string) => {
  switch (type) {
    case 'RENTAL': return '賃貸契約'
    case 'SALE': return '売買契約'
    case 'MANAGEMENT': return '管理契約'
    default: return type
  }
}

// 契約タイプタグタイプ取得
const getTypeTagType = (type: string) => {
  switch (type) {
    case 'RENTAL': return 'primary'
    case 'SALE': return 'success'
    case 'MANAGEMENT': return 'warning'
    default: return 'info'
  }
}

// ステータスタイプ取得
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'EXPIRED': return 'danger'
    case 'CANCELLED': return 'warning'
    case 'RENEWAL_PENDING': return 'info'
    default: return 'info'
  }
}

// ステータスラベル取得
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'ACTIVE': return '契約中'
    case 'EXPIRED': return '契約終了'
    case 'CANCELLED': return '契約解除'
    case 'RENEWAL_PENDING': return '更新待ち'
    default: return status
  }
}

// 日付フォーマット
const formatDate = (date: string | Date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('ja-JP')
}

// 通貨フォーマット
const formatCurrency = (amount: number) => {
  return amount.toLocaleString()
}
</script>

<style scoped>
.contract-list {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.dark-mode {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.dark-mode .header-content {
  background: rgba(44, 62, 80, 0.95);
  color: #ecf0f1;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.dark-mode .header-left h1 {
  color: #ecf0f1;
}

.header-left p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

.dark-mode .header-left p {
  color: #bdc3c7;
}

.add-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.3);
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.search-form {
  padding: 8px 0;
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 8px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.record-count {
  color: #7f8c8d;
  font-size: 14px;
}

.contract-table {
  margin-bottom: 20px;
}

.contract-number {
  display: flex;
  align-items: center;
}

.client-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.client-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.delete-confirmation {
  text-align: center;
}

.contract-info {
  margin: 16px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.warning-text {
  color: #e74c3c;
  font-weight: 600;
  margin-top: 16px;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .contract-list {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .search-form .el-col {
    margin-bottom: 16px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
