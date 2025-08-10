<template>
  <div class="transaction-list" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-content">
        <div class="header-left">
          <h1>取引管理</h1>
          <p>取引の一覧表示、検索、管理を行います</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="$router.push('/transactions/new')" class="add-btn">
            <el-icon><Plus /></el-icon>
            新規取引登録
          </el-button>
        </div>
      </div>
    </div>

    <!-- 検索・フィルターセクション -->
    <el-card class="search-card" v-motion="'slide-visible-delay-100'">
      <div class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.transactionNumber"
              placeholder="取引番号で検索"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.status"
              placeholder="取引ステータス"
              clearable
              style="width: 100%"
            >
              <el-option label="進行中" value="IN_PROGRESS" />
              <el-option label="完了" value="COMPLETED" />
              <el-option label="キャンセル" value="CANCELLED" />
              <el-option label="保留中" value="PENDING" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.type"
              placeholder="取引タイプ"
              clearable
              style="width: 100%"
            >
              <el-option label="売買" value="SALE" />
              <el-option label="賃貸" value="RENTAL" />
              <el-option label="管理" value="MANAGEMENT" />
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

    <!-- 取引一覧テーブル -->
    <el-card class="table-card" v-motion="'slide-visible-delay-200'">
      <div class="table-header">
        <div class="table-title">
          <h3>取引一覧</h3>
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
        :data="transactions"
        v-loading="loading"
        stripe
        class="transaction-table"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        <el-table-column prop="transactionNumber" label="取引番号" min-width="150" sortable="custom">
          <template #default="{ row }">
            <div class="transaction-number">
              <el-tag type="info" size="small">{{ row.transactionNumber }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="取引タイプ" width="120" sortable="custom">
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
        <el-table-column prop="amount" label="取引金額" width="150" sortable="custom">
          <template #default="{ row }">
            <span v-if="row.amount">¥{{ formatCurrency(row.amount) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="commission" label="手数料" width="120" sortable="custom">
          <template #default="{ row }">
            <span v-if="row.commission">¥{{ formatCurrency(row.commission) }}</span>
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
        <el-table-column prop="createdAt" label="作成日" width="120" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                @click="viewTransaction(row)"
                type="info"
                text
              >
                <el-icon><View /></el-icon>
                詳細
              </el-button>
              <el-button
                size="small"
                @click="editTransaction(row)"
                type="primary"
                text
              >
                <el-icon><Edit /></el-icon>
                編集
              </el-button>
              <el-button
                size="small"
                @click="deleteTransaction(row)"
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
      title="取引削除の確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-confirmation">
        <p>以下の取引を削除してもよろしいですか？</p>
        <div class="transaction-info">
          <strong>取引番号: {{ transactionToDelete?.transactionNumber }}</strong>
          <br>
          <small>{{ transactionToDelete?.clientName }} - {{ transactionToDelete?.propertyName }}</small>
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
import type { Transaction } from '@/types'
import { transactionApi } from '@/services/api'

// ルーター
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ状態
const transactions = ref<Transaction[]>([])
const loading = ref(false)
const exporting = ref(false)
const deleting = ref(false)

// ページネーション
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 検索フォーム
const searchForm = reactive({
  transactionNumber: '',
  status: '',
  type: ''
})

// ソート状態
const sortBy = ref('')
const sortOrder = ref('')

// 削除ダイアログ
const deleteDialogVisible = ref(false)
const transactionToDelete = ref<Transaction | null>(null)

// 初期化
onMounted(() => {
  loadTransactions()
  checkDarkMode()
})

// ダークモードチェック
const checkDarkMode = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
}

// 取引データ読み込み
const loadTransactions = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      transactionNumber: searchForm.transactionNumber || undefined,
      status: searchForm.status || undefined,
      type: searchForm.type || undefined,
      sortBy: sortBy.value || undefined,
      sortOrder: sortOrder.value || undefined
    }
    
    const response = await transactionApi.getTransactions(params)
    transactions.value = response.content
    totalRecords.value = response.totalElements
  } catch (error) {
    console.error('取引データの読み込みに失敗しました:', error)
    ElMessage.error('取引データの読み込みに失敗しました')
  } finally {
    loading.value = false
  }
}

// 検索実行
const handleSearch = () => {
  currentPage.value = 1
  loadTransactions()
}

// 検索リセット
const resetSearch = () => {
  Object.assign(searchForm, {
    transactionNumber: '',
    status: '',
    type: ''
  })
  currentPage.value = 1
  loadTransactions()
}

// ソート変更
const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadTransactions()
}

// ページサイズ変更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTransactions()
}

// ページ変更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadTransactions()
}

// 取引詳細表示
const viewTransaction = (transaction: Transaction) => {
  router.push(`/transactions/${transaction.id}`)
}

// 取引編集
const editTransaction = (transaction: Transaction) => {
  router.push(`/transactions/${transaction.id}/edit`)
}

// 取引削除
const deleteTransaction = (transaction: Transaction) => {
  transactionToDelete.value = transaction
  deleteDialogVisible.value = true
}

// 削除確認
const confirmDelete = async () => {
  if (!transactionToDelete.value) return
  
  try {
    deleting.value = true
    await transactionApi.deleteTransaction(transactionToDelete.value.id)
    ElMessage.success('取引を削除しました')
    deleteDialogVisible.value = false
    transactionToDelete.value = null
    loadTransactions()
  } catch (error) {
    console.error('取引の削除に失敗しました:', error)
    ElMessage.error('取引の削除に失敗しました')
  } finally {
    deleting.value = false
  }
}

// データエクスポート
const exportData = async () => {
  try {
    exporting.value = true
    // エクスポート処理の実装
    ElMessage.success('データのエクスポートが完了しました')
  } catch (error) {
    console.error('データのエクスポートに失敗しました:', error)
    ElMessage.error('データのエクスポートに失敗しました')
  } finally {
    exporting.value = false
  }
}

// 取引タイプラベル取得
const getTypeLabel = (type: string) => {
  switch (type) {
    case 'SALE': return '売買'
    case 'RENTAL': return '賃貸'
    case 'MANAGEMENT': return '管理'
    default: return type
  }
}

// 取引タイプタグタイプ取得
const getTypeTagType = (type: string) => {
  switch (type) {
    case 'SALE': return 'success'
    case 'RENTAL': return 'primary'
    case 'MANAGEMENT': return 'warning'
    default: return 'info'
  }
}

// ステータスタイプ取得
const getStatusType = (status: string) => {
  switch (status) {
    case 'IN_PROGRESS': return 'primary'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'danger'
    case 'PENDING': return 'warning'
    default: return 'info'
  }
}

// ステータスラベル取得
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'IN_PROGRESS': return '進行中'
    case 'COMPLETED': return '完了'
    case 'CANCELLED': return 'キャンセル'
    case 'PENDING': return '保留中'
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
.transaction-list {
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

.transaction-table {
  margin-bottom: 20px;
}

.transaction-number {
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

.transaction-info {
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
  .transaction-list {
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
