<template>
  <div class="transaction-list">
    <!-- ヘッダーセクション -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>取引管理</h1>
          <p>取引の一覧表示、検索、管理を行います</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showNewTransactionDialog" class="add-btn">
            <el-icon><Plus /></el-icon>
            +新規取引登録
          </el-button>
        </div>
      </div>
    </div>

    <!-- 検索・フィルターセクション -->
    <el-card class="search-card">
      <div class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.transactionNumber"
              placeholder="取引番号で検索"
              clearable
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.status"
              placeholder="取引ステータス"
              clearable
              class="filter-select"
            >
              <el-option
                v-for="status in transactionStatuses"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.type"
              placeholder="取引タイプ"
              clearable
              class="filter-select"
            >
              <el-option
                v-for="type in transactionTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.propertyName"
              placeholder="物件名"
              clearable
              class="filter-select"
            >
              <el-option
                v-for="property in properties"
                :key="property.id"
                :label="property.name"
                :value="property.name"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <div class="search-actions">
              <el-button @click="resetSearch" class="reset-btn">
                <el-icon><Refresh /></el-icon>
                リセット
              </el-button>
              <el-button type="primary" @click="handleSearch" class="search-btn">
                <el-icon><Search /></el-icon>
                検索
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 取引一覧 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>取引一覧 全{{ totalCount }}件</span>
          <div class="view-controls">
            <el-button type="info" @click="exportData" :loading="exportLoading">
              <el-icon><Download /></el-icon>
              ↓エクスポート
            </el-button>
          </div>
        </div>
      </template>

      <!-- ローディング状態 -->
      <div v-if="loading" class="loading-overlay">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- テーブルビュー -->
      <div v-else class="table-view">
        <el-table
          :data="filteredTransactions"
          style="width: 100%"
          class="transaction-table"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="取引番号" width="120">
            <template #default="{ row }">
              <el-checkbox v-model="row.selected" />
            </template>
          </el-table-column>
          <el-table-column prop="type" label="取引タイプ" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="顧客名" width="120">
            <template #default="{ row }">
              <div class="customer-avatar">
                <el-avatar :size="24" :src="row.customerAvatar" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="propertyName" label="物件名" min-width="200" />
          <el-table-column prop="amount" label="取引金額" width="150">
            <template #default="{ row }">
              <span class="amount-text">¥{{ formatNumber(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="fee" label="手数料" width="120">
            <template #default="{ row }">
              <span v-if="row.fee" class="fee-text">¥{{ formatNumber(row.fee) }}</span>
              <span v-else class="no-fee">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="ステータス" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="作成日" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" @click="viewTransaction(row)" class="view-btn">
                  ◇詳細
                </el-button>
                <el-button size="small" type="primary" @click="editTransaction(row)" class="edit-btn">
                  編集
                </el-button>
                <el-button size="small" type="success" @click="addTransactionInfo(row)" class="info-btn">
                  <el-icon><InfoFilled /></el-icon>
                  情報追加
                </el-button>
                <el-button size="small" type="danger" @click="deleteTransaction(row)" class="delete-btn">
                  削除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- ページネーション -->
      <div class="pagination-container">
        <div class="pagination-info">
          <span>Total {{ totalCount }}</span>
          <el-select v-model="pageSize" size="small" style="margin-left: 16px;">
            <el-option label="20/page" :value="20" />
            <el-option label="50/page" :value="50" />
            <el-option label="100/page" :value="100" />
          </el-select>
        </div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新規取引登録ダイアログ -->
    <el-dialog
      v-model="newTransactionDialogVisible"
      title="新規取引登録"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="newTransactionFormRef"
        :model="newTransactionForm"
        :rules="transactionRules"
        label-width="120px"
      >
        <el-form-item label="取引番号" prop="transactionNumber">
          <el-input v-model="newTransactionForm.transactionNumber" placeholder="取引番号を入力" />
        </el-form-item>
        <el-form-item label="取引タイプ" prop="type">
          <el-select v-model="newTransactionForm.type" placeholder="取引タイプを選択" style="width: 100%">
            <el-option
              v-for="type in transactionTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="顧客名" prop="customerName">
          <el-input v-model="newTransactionForm.customerName" placeholder="顧客名を入力" />
        </el-form-item>
        <el-form-item label="物件名" prop="propertyName">
          <el-select v-model="newTransactionForm.propertyName" placeholder="物件を選択" style="width: 100%">
            <el-option
              v-for="property in properties"
              :key="property.id"
              :label="property.name"
              :value="property.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="取引金額" prop="amount">
          <el-input-number
            v-model="newTransactionForm.amount"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="取引金額を入力"
          />
        </el-form-item>
        <el-form-item label="手数料" prop="fee">
          <el-input-number
            v-model="newTransactionForm.fee"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="手数料を入力"
          />
        </el-form-item>
        <el-form-item label="ステータス" prop="status">
          <el-select v-model="newTransactionForm.status" placeholder="ステータスを選択" style="width: 100%">
            <el-option
              v-for="status in transactionStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newTransactionDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitNewTransaction">登録</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 取引詳細表示ダイアログ -->
    <el-dialog
      v-model="transactionDetailDialogVisible"
      title="取引詳細"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedTransaction" class="transaction-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="取引ID">{{ selectedTransaction.id }}</el-descriptions-item>
          <el-descriptions-item label="取引番号">{{ selectedTransaction.transactionNumber }}</el-descriptions-item>
          <el-descriptions-item label="取引タイプ">
            <el-tag :type="getTypeTagType(selectedTransaction.type)" size="small">
              {{ getTypeLabel(selectedTransaction.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="ステータス">
            <el-tag :type="getStatusTagType(selectedTransaction.status)" size="small">
              {{ getStatusLabel(selectedTransaction.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="顧客名">{{ selectedTransaction.customerName }}</el-descriptions-item>
          <el-descriptions-item label="物件名">{{ selectedTransaction.propertyName }}</el-descriptions-item>
          <el-descriptions-item label="取引金額">
            <span class="amount-text">¥{{ formatNumber(selectedTransaction.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="手数料">
            <span v-if="selectedTransaction.fee" class="fee-text">¥{{ formatNumber(selectedTransaction.fee) }}</span>
            <span v-else class="no-fee">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="作成日">{{ selectedTransaction.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="顧客アバター">
            <el-avatar :size="40" :src="selectedTransaction.customerAvatar" />
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 取引履歴セクション -->
        <div class="transaction-history" style="margin-top: 24px;">
          <h4>取引履歴</h4>
          <el-timeline>
            <el-timeline-item
              timestamp="2024/1/15 10:30"
              placement="top"
            >
              <el-card>
                <h4>取引作成</h4>
                <p>システム管理者により取引が作成されました</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item
              timestamp="2024/1/15 14:20"
              placement="top"
            >
              <el-card>
                <h4>顧客確認完了</h4>
                <p>顧客による取引内容の確認が完了しました</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item
              timestamp="2024/1/16 09:15"
              placement="top"
            >
              <el-card>
                <h4>決済処理完了</h4>
                <p>決済処理が正常に完了しました</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transactionDetailDialogVisible = false">閉じる</el-button>
          <el-button type="primary" @click="editTransaction(selectedTransaction)">編集</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 取引編集ダイアログ -->
    <el-dialog
      v-model="transactionEditDialogVisible"
      title="取引編集"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editTransactionFormRef"
        :model="editTransactionForm"
        :rules="transactionRules"
        label-width="120px"
      >
        <el-form-item label="取引番号" prop="transactionNumber">
          <el-input v-model="editTransactionForm.transactionNumber" placeholder="取引番号を入力" />
        </el-form-item>
        <el-form-item label="取引タイプ" prop="type">
          <el-select v-model="editTransactionForm.type" placeholder="取引タイプを選択" style="width: 100%">
            <el-option
              v-for="type in transactionTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="顧客名" prop="customerName">
          <el-input v-model="editTransactionForm.customerName" placeholder="顧客名を入力" />
        </el-form-item>
        <el-form-item label="物件名" prop="propertyName">
          <el-select v-model="editTransactionForm.propertyName" placeholder="物件を選択" style="width: 100%">
            <el-option
              v-for="property in properties"
              :key="property.id"
              :label="property.name"
              :value="property.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="取引金額" prop="amount">
          <el-input-number
            v-model="editTransactionForm.amount"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="取引金額を入力"
          />
        </el-form-item>
        <el-form-item label="手数料" prop="fee">
          <el-input-number
            v-model="editTransactionForm.fee"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="手数料を入力"
          />
        </el-form-item>
        <el-form-item label="ステータス" prop="status">
          <el-select v-model="editTransactionForm.status" placeholder="ステータスを選択" style="width: 100%">
            <el-option
              v-for="status in transactionStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transactionEditDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitEditTransaction">更新</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 取引情報追加ダイアログ -->
    <el-dialog
      v-model="transactionInfoDialogVisible"
      title="取引情報追加"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="transactionInfoFormRef"
        :model="transactionInfoForm"
        :rules="transactionInfoRules"
        label-width="120px"
      >
        <el-form-item label="取引番号" prop="transactionNumber">
          <el-input v-model="transactionInfoForm.transactionNumber" disabled />
        </el-form-item>
        <el-form-item label="情報タイプ" prop="infoType">
          <el-select v-model="transactionInfoForm.infoType" placeholder="情報タイプを選択" style="width: 100%">
            <el-option label="メモ" value="MEMO" />
            <el-option label="履歴" value="HISTORY" />
            <el-option label="添付ファイル" value="ATTACHMENT" />
            <el-option label="その他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="タイトル" prop="title">
          <el-input v-model="transactionInfoForm.title" placeholder="タイトルを入力" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="transactionInfoForm.content"
            type="textarea"
            :rows="4"
            placeholder="内容を入力"
          />
        </el-form-item>
        <el-form-item label="優先度" prop="priority">
          <el-rate
            v-model="transactionInfoForm.priority"
            :max="5"
            :texts="['低', '中低', '中', '中高', '高']"
            show-text
          />
        </el-form-item>
        <el-form-item label="タグ" prop="tags">
          <el-select
            v-model="transactionInfoForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="タグを選択または作成"
            style="width: 100%"
          >
            <el-option label="重要" value="重要" />
            <el-option label="緊急" value="緊急" />
            <el-option label="要確認" value="要確認" />
            <el-option label="完了" value="完了" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transactionInfoDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitTransactionInfo">追加</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 削除確認ダイアログ -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="取引削除確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-target">
        <p>以下の取引を削除しますか？</p>
        <p><strong>{{ transactionToDelete?.transactionNumber }} - {{ transactionToDelete?.customerName }}</strong></p>
        <p>この操作は取り消せません。</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">キャンセル</el-button>
          <el-button type="danger" @click="confirmDeleteTransaction">削除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus,
  Search,
  Refresh,
  Download,
  View,
  Edit,
  Delete,
  InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 状態管理
const loading = ref(false)
const exportLoading = ref(false)
const deleteLoading = ref(false)
const deleteDialogVisible = ref(false)
const transactionToDelete = ref<any>(null)
const newTransactionDialogVisible = ref(false)
const transactionDetailDialogVisible = ref(false)
const transactionEditDialogVisible = ref(false)

// データ
const transactions = ref<any[]>([])
const properties = ref<any[]>([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedTransaction = ref<any>(null)

// 検索フォーム
const searchForm = ref({
  transactionNumber: '',
  status: '',
  type: '',
  propertyName: ''
})

// 新規取引フォーム
const newTransactionForm = ref({
  transactionNumber: '',
  type: '',
  customerName: '',
  propertyName: '',
  amount: 0,
  fee: 0,
  status: ''
})

// 取引編集フォーム
const editTransactionForm = ref({
  transactionNumber: '',
  type: '',
  customerName: '',
  propertyName: '',
  amount: 0,
  fee: 0,
  status: ''
})

// 取引情報追加関連の状態
const transactionInfoDialogVisible = ref(false)
const transactionInfoForm = ref({
  transactionNumber: '',
  infoType: '',
  title: '',
  content: '',
  priority: 3,
  tags: []
})

const transactionInfoRules = {
  infoType: [{ required: true, message: '情報タイプを選択してください', trigger: 'change' }],
  title: [{ required: true, message: 'タイトルを入力してください', trigger: 'blur' }],
  content: [{ required: true, message: '内容を入力してください', trigger: 'blur' }]
}

const transactionRules = {
  transactionNumber: [{ required: true, message: '取引番号を入力してください', trigger: 'blur' }],
  type: [{ required: true, message: '取引タイプを選択してください', trigger: 'change' }],
  customerName: [{ required: true, message: '顧客名を入力してください', trigger: 'blur' }],
  propertyName: [{ required: true, message: '物件を選択してください', trigger: 'change' }],
  amount: [{ required: true, message: '取引金額を入力してください', trigger: 'blur' }],
  status: [{ required: true, message: 'ステータスを選択してください', trigger: 'change' }]
}

// 取引タイプとステータスの定義
const transactionTypes = [
  { value: 'PAYMENT', label: 'PAYMENT' },
  { value: 'COMMISSION', label: 'COMMISSION' },
  { value: 'MAINTENANCE', label: 'MAINTENANCE' },
  { value: 'INSURANCE', label: 'INSURANCE' },
  { value: 'TAX', label: 'TAX' },
  { value: 'REFUND', label: 'REFUND' }
]

const transactionStatuses = [
  { value: 'COMPLETED', label: '完了' },
  { value: 'PENDING', label: '保留中' },
  { value: 'FAILED', label: 'FAILED' },
  { value: 'CANCELLED', label: 'キャンセル' }
]

// フィルタリングされた取引
const filteredTransactions = computed(() => {
  let filtered = transactions.value

  if (searchForm.value.transactionNumber) {
    filtered = filtered.filter(transaction =>
      transaction.transactionNumber.toLowerCase().includes(searchForm.value.transactionNumber.toLowerCase())
    )
  }

  if (searchForm.value.status) {
    filtered = filtered.filter(transaction => transaction.status === searchForm.value.status)
  }

  if (searchForm.value.type) {
    filtered = filtered.filter(transaction => transaction.type === searchForm.value.type)
  }

  if (searchForm.value.propertyName) {
    filtered = filtered.filter(transaction => transaction.propertyName === searchForm.value.propertyName)
  }

  return filtered
})

// ライフサイクル
onMounted(() => {
  loadTransactions()
  loadProperties()
})

// データ取得
const loadTransactions = async () => {
  try {
    loading.value = true
    // 実際のAPIからデータを取得
    // const response = await transactionApi.getAll()
    // transactions.value = response.data || []
    
    // モックデータ（一時的）
    loadDummyTransactions()
    totalCount.value = transactions.value.length
  } catch (error) {
    console.error('取引データの取得に失敗しました:', error)
    loadDummyTransactions()
  } finally {
    loading.value = false
  }
}

const loadProperties = async () => {
  try {
    // 実際のAPIからデータを取得
    // const response = await propertyApi.getAll()
    // properties.value = response.data || []
    
    // モックデータ（一時的）
    properties.value = [
      { id: 1, name: '青山マンション 101号室' },
      { id: 2, name: '代官山一戸建て' },
      { id: 3, name: '新宿オフィスビル' },
      { id: 4, name: '六本木ヒルズレジデンス' },
      { id: 5, name: '品川倉庫施設' }
    ]
  } catch (error) {
    console.error('物件データの取得に失敗しました:', error)
  }
}

const loadDummyTransactions = () => {
  const dummyTransactions = [
    {
      id: 1,
      transactionNumber: 'TXN001',
      type: 'PAYMENT',
      customerName: '田中太郎',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=T',
      propertyName: '青山マンション 101号室',
      amount: 45000000,
      fee: 2250000,
      status: 'COMPLETED',
      createdAt: '2024/1/15',
      selected: false
    },
    {
      id: 2,
      transactionNumber: 'TXN002',
      type: 'COMMISSION',
      customerName: '佐藤花子',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=S',
      propertyName: '代官山一戸建て',
      amount: 120000000,
      fee: 6000000,
      status: 'COMPLETED',
      createdAt: '2024/1/20',
      selected: false
    },
    {
      id: 3,
      transactionNumber: 'TXN003',
      type: 'MAINTENANCE',
      customerName: '鈴木一郎',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=S',
      propertyName: '新宿オフィスビル',
      amount: 5000000,
      fee: 250000,
      status: 'PENDING',
      createdAt: '2024/2/1',
      selected: false
    },
    {
      id: 4,
      transactionNumber: 'TXN004',
      type: 'INSURANCE',
      customerName: '高橋美咲',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=T',
      propertyName: '六本木ヒルズレジデンス',
      amount: 8000000,
      fee: 400000,
      status: 'COMPLETED',
      createdAt: '2024/2/15',
      selected: false
    },
    {
      id: 5,
      transactionNumber: 'TXN005',
      type: 'TAX',
      customerName: '伊藤健太',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=I',
      propertyName: '品川倉庫施設',
      amount: 15000000,
      fee: 750000,
      status: 'COMPLETED',
      createdAt: '2024/2/20',
      selected: false
    },
    {
      id: 6,
      transactionNumber: 'TXN006',
      type: 'REFUND',
      customerName: '渡辺恵子',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=W',
      propertyName: '目黒オフィス',
      amount: 3000000,
      fee: 0,
      status: 'COMPLETED',
      createdAt: '2024/2/25',
      selected: false
    },
    {
      id: 7,
      transactionNumber: 'TXN007',
      type: 'PAYMENT',
      customerName: '中村雄一',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=N',
      propertyName: '中野マンション',
      amount: 35000000,
      fee: 1750000,
      status: 'PENDING',
      createdAt: '2024/3/1',
      selected: false
    },
    {
      id: 8,
      transactionNumber: 'TXN008',
      type: 'COMMISSION',
      customerName: '小林麻美',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=K',
      propertyName: '杉並一戸建て',
      amount: 95000000,
      fee: 4750000,
      status: 'COMPLETED',
      createdAt: '2024/3/5',
      selected: false
    },
    {
      id: 9,
      transactionNumber: 'TXN009',
      type: 'MAINTENANCE',
      customerName: '加藤大輔',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=K',
      propertyName: '豊島商業ビル',
      amount: 12000000,
      fee: 600000,
      status: 'FAILED',
      createdAt: '2024/3/10',
      selected: false
    },
    {
      id: 10,
      transactionNumber: 'TXN010',
      type: 'INSURANCE',
      customerName: '山田真理',
      customerAvatar: 'https://via.placeholder.com/24/9c27b0/ffffff?text=Y',
      propertyName: '池袋マンション',
      amount: 6000000,
      fee: 300000,
      status: 'CANCELLED',
      createdAt: '2024/3/15',
      selected: false
    }
  ]
  
  transactions.value = dummyTransactions
  totalCount.value = dummyTransactions.length
}

// イベントハンドラー
const handleSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchForm.value = {
    transactionNumber: '',
    status: '',
    type: '',
    propertyName: ''
  }
  currentPage.value = 1
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 取引操作
const viewTransaction = (transaction: any) => {
  selectedTransaction.value = transaction
  transactionDetailDialogVisible.value = true
}

const editTransaction = (transaction: any) => {
  selectedTransaction.value = transaction
  editTransactionForm.value = { ...transaction }
  transactionEditDialogVisible.value = true
}

const deleteTransaction = (transaction: any) => {
  transactionToDelete.value = transaction
  deleteDialogVisible.value = true
}

const confirmDeleteTransaction = async () => {
  if (!transactionToDelete.value) return
  
  try {
    // 実際のAPIで削除
    // await transactionApi.delete(transactionToDelete.value.id)
    
    // ローカルデータから削除
    const index = transactions.value.findIndex(t => t.id === transactionToDelete.value!.id)
    if (index > -1) {
      transactions.value.splice(index, 1)
      totalCount.value--
    }
    
    (ElMessage as any).success('取引が削除されました')
    deleteDialogVisible.value = false
    transactionToDelete.value = null
  } catch (error) {
    console.error('取引の削除に失敗しました:', error)
    (ElMessage as any).error('取引の削除に失敗しました')
  }
}

// 取引編集
const submitEditTransaction = async () => {
  try {
    // 実際のAPIで更新
    // await transactionApi.update(selectedTransaction.value.id, editTransactionForm.value)
    
    // ローカルデータを更新
    const index = transactions.value.findIndex(t => t.id === selectedTransaction.value!.id)
    if (index > -1) {
      transactions.value[index] = {
        ...transactions.value[index],
        ...editTransactionForm.value,
        customerAvatar: `https://via.placeholder.com/24/9c27b0/ffffff?text=${editTransactionForm.value.customerName.charAt(0)}`
      }
    }
    
    (ElMessage as any).success('取引が更新されました')
    transactionEditDialogVisible.value = false
    selectedTransaction.value = null
  } catch (error) {
    console.error('取引の更新に失敗しました:', error)
    (ElMessage as any).error('取引の更新に失敗しました')
  }
}

// 新規取引登録
const showNewTransactionDialog = () => {
  newTransactionForm.value = {
    transactionNumber: '',
    type: '',
    customerName: '',
    propertyName: '',
    amount: 0,
    fee: 0,
    status: ''
  }
  newTransactionDialogVisible.value = true
}

const submitNewTransaction = async () => {
  try {
    // 実際のAPIで作成
    // await transactionApi.create(newTransactionForm.value)
    
    // ローカルデータに追加
    const newTransaction = {
      id: transactions.value.length + 1,
      ...newTransactionForm.value,
      customerAvatar: `https://via.placeholder.com/24/9c27b0/ffffff?text=${newTransactionForm.value.customerName.charAt(0)}`,
      createdAt: new Date().toISOString().split('T')[0].replace(/-/g, '/'),
      selected: false
    }
    
    transactions.value.unshift(newTransaction)
    totalCount.value++
    
    (ElMessage as any).success('新規取引が登録されました')
    newTransactionDialogVisible.value = false
  } catch (error) {
    console.error('取引の登録に失敗しました:', error)
    (ElMessage as any).error('取引の登録に失敗しました')
  }
}

// エクスポート
const exportData = async () => {
  try {
    exportLoading.value = true
    
    const csvContent = 'data:text/csv;charset=utf-8,' + 
      'ID,取引番号,取引タイプ,顧客名,物件名,取引金額,手数料,ステータス,作成日\n' +
      filteredTransactions.value.map(t => 
        `${t.id},${t.transactionNumber},${getTypeLabel(t.type)},${t.customerName},${t.propertyName},${t.amount},${t.fee || '-'},${getStatusLabel(t.status)},${t.createdAt}`
      ).join('\n')
    
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `transactions_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    (ElMessage as any).success('データをエクスポートしました')
  } catch (error) {
    console.error('エクスポートに失敗しました:', error)
    (ElMessage as any).error('エクスポートに失敗しました')
  } finally {
    exportLoading.value = false
  }
}

// ユーティリティ関数
const getTypeLabel = (type: string) => {
  const found = transactionTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getStatusLabel = (status: string) => {
  const found = transactionStatuses.find(s => s.value === status)
  return found ? found.label : status
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    PAYMENT: 'primary',
    COMMISSION: 'success',
    MAINTENANCE: 'warning',
    INSURANCE: 'info',
    TAX: 'danger',
    REFUND: 'warning'
  }
  return typeMap[type] || 'info'
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    COMPLETED: 'success',
    PENDING: 'warning',
    FAILED: 'danger',
    CANCELLED: 'danger'
  }
  return statusMap[status] || 'info'
}

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatDate = (dateString: string) => {
  return dateString
}

const getRowClassName = ({ row, rowIndex }: { row: any; rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 取引情報追加
const addTransactionInfo = (transaction: any) => {
  transactionInfoForm.value.transactionNumber = transaction.transactionNumber
  transactionInfoForm.value.infoType = ''
  transactionInfoForm.value.title = ''
  transactionInfoForm.value.content = ''
  transactionInfoForm.value.priority = 3
  transactionInfoForm.value.tags = []
  transactionInfoDialogVisible.value = true
}

const submitTransactionInfo = async () => {
  try {
    // 実際のAPIで情報を保存
    // await transactionInfoApi.create({
    //   transactionId: selectedTransaction.value.id.toString(),
    //   ...transactionInfoForm.value
    // })
    
    (ElMessage as any).success('取引情報が追加されました')
    transactionInfoDialogVisible.value = false
  } catch (error) {
    console.error('取引情報の追加に失敗しました:', error)
    (ElMessage as any).error('取引情報の追加に失敗しました')
  }
}
</script>

<style scoped>
.transaction-list {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  margin-bottom: 24px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 20px;
  color: white;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h1 {
  font-size: 2.5rem;
  margin: 0 0 8px 0;
  font-weight: 800;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.header-left p {
  font-size: 1.1rem;
  margin: 0;
  opacity: 0.9;
}

.add-btn {
  padding: 12px 24px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.add-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 35px rgba(255, 255, 255, 0.4);
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.search-form {
  padding: 20px 0;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-actions {
  display: flex;
  gap: 12px;
}

.reset-btn, .search-btn {
  border-radius: 12px;
}

.list-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2rem;
  font-weight: 600;
}

.transaction-table {
  border-radius: 12px;
  overflow: hidden;
}

.customer-avatar {
  display: flex;
  justify-content: center;
}

.amount-text {
  font-weight: 600;
  color: #e74c3c;
}

.fee-text {
  font-weight: 600;
  color: #27ae60;
}

.no-fee {
  color: #95a5a6;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.view-btn, .edit-btn, .delete-btn {
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
}

.pagination-info {
  display: flex;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.delete-target {
  font-weight: 600;
  color: #e74c3c;
  text-align: center;
  margin: 16px 0;
}

/* 行毎の背景色 */
.transaction-table .el-table__body tr.even-row {
  background-color: #f8f9fa !important;
}

.transaction-table .el-table__body tr.odd-row {
  background-color: #ffffff !important;
}

.transaction-table .el-table__body tr.even-row:hover {
  background-color: #e9ecef !important;
}

.transaction-table .el-table__body tr.odd-row:hover {
  background-color: #f1f3f4 !important;
}

/* 取引詳細スタイル */
.transaction-detail {
  padding: 16px 0;
}

.transaction-history h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 1.1rem;
  font-weight: 600;
}

.transaction-history .el-timeline-item__content {
  padding-left: 16px;
}

.transaction-history .el-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.transaction-history .el-card h4 {
  margin: 0 0 8px 0;
  color: #409eff;
  font-size: 1rem;
  font-weight: 600;
}

.transaction-history .el-card p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* ボタンスタイル */
.view-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.view-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-1px);
}

.edit-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #e085e8 0%, #e04e5f 100%);
  transform: translateY(-1px);
}

.info-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.info-btn:hover {
  background: linear-gradient(135deg, #45a1e5 0%, #00e0e0 100%);
  transform: translateY(-1px);
}

.delete-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #e8658b 0%, #e5d130 100%);
  transform: translateY(-1px);
}

/* ローディングオーバーレイ */
.loading-overlay {
  padding: 40px;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .transaction-list {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-left h1 {
    font-size: 2rem;
  }
  
  .search-form .el-col {
    margin-bottom: 16px;
  }
  
  .search-actions {
    flex-direction: column;
  }
  
  .pagination-container {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
