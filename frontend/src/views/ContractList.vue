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

      <!-- エラー表示 -->
      <el-alert
        v-if="searchError"
        :title="searchError"
        type="warning"
        :closable="false"
        show-icon
        class="error-alert"
      />

              <el-table
          :data="contracts"
          v-loading="loading"
          class="contract-table"
          :row-class-name="getRowClassName"
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
        <el-table-column label="電子認証" width="250">
          <template #default="{ row }">
            <div class="auth-buttons">
              <el-button size="small" type="primary" @click="showSignatures(row)">
                署名履歴
              </el-button>
              <el-button size="small" type="success" @click="showTimestamps(row)">
                タイムスタンプ
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
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
                @click="addContractInfo(row)"
                type="success"
                text
              >
                <el-icon><InfoFilled /></el-icon>
                情報追加
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

    <!-- 電子署名履歴ダイアログ -->
    <el-dialog v-model="signatureDialogVisible" title="電子署名履歴" width="800px">
      <div v-if="selectedContract">
        <h4>契約 {{ selectedContract.contractNumber }} の署名履歴</h4>
        <div class="signature-actions">
          <el-button type="primary" @click="createNewSignature">新規署名作成</el-button>
        </div>
        <el-table :data="contractSignatures" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="signerId" label="署名者ID" width="120" />
          <el-table-column prop="documentType" label="文書タイプ" width="120">
            <template #default="scope">
              <el-tag :type="getDocumentTypeColor(scope.row.documentType)">
                {{ getDocumentTypeLabel(scope.row.documentType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="signedAt" label="署名日時" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.signedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="ステータス" width="100">
            <template #default="scope">
              <el-tag :type="getSignatureStatusColor(scope.row.status)">
                {{ getSignatureStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="verifySignature(scope.row)">検証</el-button>
              <el-button size="small" type="danger" @click="deleteSignature(scope.row)">削除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 電子タイムスタンプダイアログ -->
    <el-dialog v-model="timestampDialogVisible" title="電子タイムスタンプ" width="800px">
      <div v-if="selectedContract">
        <h4>契約 {{ selectedContract.contractNumber }} のタイムスタンプ</h4>
        <div class="timestamp-actions">
          <el-button type="primary" @click="createNewTimestamp">新規タイムスタンプ作成</el-button>
        </div>
        <el-table :data="contractTimestamps" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="documentType" label="文書タイプ" width="120">
            <template #default="scope">
              <el-tag :type="getDocumentTypeColor(scope.row.documentType)">
                {{ getDocumentTypeLabel(scope.row.documentType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timestampAt" label="タイムスタンプ日時" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.timestampAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="timestampAuthority" label="認証局" width="150" />
          <el-table-column prop="status" label="ステータス" width="100">
            <template #default="scope">
              <el-tag :type="getTimestampStatusColor(scope.row.status)">
                {{ getTimestampStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="verifyTimestamp(scope.row)">検証</el-button>
              <el-button size="small" type="danger" @click="deleteTimestamp(scope.row)">削除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 新規署名作成ダイアログ -->
    <el-dialog v-model="newSignatureDialogVisible" title="新規署名作成" width="600px">
      <el-form :model="newSignatureForm" :rules="signatureRules" ref="signatureFormRef" label-width="120px">
        <el-form-item label="署名者ID" prop="signerId">
          <el-input v-model="newSignatureForm.signerId" placeholder="署名者IDを入力" />
        </el-form-item>
        <el-form-item label="文書タイプ" prop="documentType">
          <el-select v-model="newSignatureForm.documentType" placeholder="文書タイプを選択">
            <el-option label="契約書" value="CONTRACT" />
            <el-option label="同意書" value="AGREEMENT" />
            <el-option label="請求書" value="INVOICE" />
            <el-option label="その他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="署名データ" prop="signatureData">
          <el-input
            v-model="newSignatureForm.signatureData"
            type="textarea"
            :rows="4"
            placeholder="Base64エンコードされた署名データを入力"
          />
        </el-form-item>
        <el-form-item label="文書ハッシュ" prop="documentHash">
          <el-input v-model="newSignatureForm.documentHash" placeholder="SHA-256ハッシュを入力" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newSignatureDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitSignatureCreation">作成</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新規タイムスタンプ作成ダイアログ -->
    <el-dialog v-model="newTimestampDialogVisible" title="新規タイムスタンプ作成" width="600px">
      <el-form :model="newTimestampForm" :rules="timestampRules" ref="timestampFormRef" label-width="120px">
        <el-form-item label="文書タイプ" prop="documentType">
          <el-select v-model="newTimestampForm.documentType" placeholder="文書タイプを選択">
            <el-option label="契約書" value="CONTRACT" />
            <el-option label="同意書" value="AGREEMENT" />
            <el-option label="請求書" value="INVOICE" />
            <el-option label="その他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="認証局" prop="timestampAuthority">
          <el-input v-model="newTimestampForm.timestampAuthority" placeholder="タイムスタンプ認証局名を入力" />
        </el-form-item>
        <el-form-item label="証明書" prop="timestampCertificate">
          <el-input
            v-model="newTimestampForm.timestampCertificate"
            type="textarea"
            :rows="4"
            placeholder="Base64エンコードされた証明書を入力"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newTimestampDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitTimestampCreation">作成</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 契約情報追加ダイアログ -->
    <el-dialog v-model="contractInfoDialogVisible" title="契約情報追加" width="600px">
      <el-form :model="contractInfoForm" :rules="contractInfoRules" ref="contractInfoFormRef" label-width="120px">
        <el-form-item label="契約番号" prop="contractNumber">
          <el-input v-model="contractInfoForm.contractNumber" disabled />
        </el-form-item>
        <el-form-item label="情報タイプ" prop="infoType">
          <el-select v-model="contractInfoForm.infoType" placeholder="情報タイプを選択">
            <el-option label="契約変更履歴" value="CONTRACT_CHANGE" />
            <el-option label="支払い履歴" value="PAYMENT_HISTORY" />
            <el-option label="メモ・備考" value="NOTE" />
            <el-option label="重要度" value="IMPORTANCE" />
            <el-option label="その他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="タイトル" prop="title">
          <el-input v-model="contractInfoForm.title" placeholder="情報のタイトルを入力" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="contractInfoForm.content"
            type="textarea"
            :rows="4"
            placeholder="詳細な内容を入力"
          />
        </el-form-item>
        <el-form-item label="重要度" prop="priority">
          <el-rate v-model="contractInfoForm.priority" :max="5" />
        </el-form-item>
        <el-form-item label="タグ" prop="tags">
          <el-select
            v-model="contractInfoForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="タグを選択または作成"
          >
            <el-option label="重要" value="重要" />
            <el-option label="緊急" value="緊急" />
            <el-option label="変更" value="変更" />
            <el-option label="支払い" value="支払い" />
            <el-option label="延長" value="延長" />
            <el-option label="解約" value="解約" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contractInfoDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitContractInfo">追加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, Search, Refresh, Download, View, Edit, Delete, InfoFilled } from '@element-plus/icons-vue'
import type { Contract } from '@/types'
import { contractApi } from '@/services/api'
import { ContractType, ContractStatus } from '@/types'

// ルーター
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ状態
const contracts = ref<Contract[]>([])
const filteredContracts = ref<Contract[]>([])
const loading = ref(false)
const exporting = ref(false)
const deleting = ref(false)

// 検索状態
const isSearching = ref(false)
const lastSearchQuery = ref('')
const lastSearchResults = ref<Contract[]>([])
const searchError = ref<string | null>(null)

// ページネーション
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// ローカルストレージのキー
const STORAGE_KEYS = {
  LAST_SEARCH_QUERY: 'contract_last_search_query',
  LAST_SEARCH_RESULTS: 'contract_last_search_results',
  SEARCH_TIMESTAMP: 'contract_search_timestamp'
}

// ローカルストレージから検索状態を復元
const restoreSearchState = () => {
  try {
    const savedQuery = localStorage.getItem(STORAGE_KEYS.LAST_SEARCH_QUERY)
    const savedResults = localStorage.getItem(STORAGE_KEYS.LAST_SEARCH_RESULTS)
    const savedTimestamp = localStorage.getItem(STORAGE_KEYS.SEARCH_TIMESTAMP)
    
    if (savedQuery && savedResults && savedTimestamp) {
      const timestamp = parseInt(savedTimestamp)
      const now = Date.now()
      // 30分以内の検索結果のみ復元
      if (now - timestamp < 30 * 60 * 1000) {
        lastSearchQuery.value = savedQuery
        lastSearchResults.value = JSON.parse(savedResults)
        isSearching.value = true
        contracts.value = [...lastSearchResults.value]
        totalRecords.value = lastSearchResults.value.length
        return true
      }
    }
  } catch (error) {
    console.warn('検索状態の復元に失敗しました:', error)
  }
  return false
}

// 検索状態をローカルストレージに保存
const saveSearchState = (query: string, results: Contract[]) => {
  try {
    localStorage.setItem(STORAGE_KEYS.LAST_SEARCH_QUERY, query)
    localStorage.setItem(STORAGE_KEYS.LAST_SEARCH_RESULTS, JSON.stringify(results))
    localStorage.setItem(STORAGE_KEYS.SEARCH_TIMESTAMP, Date.now().toString())
  } catch (error) {
    console.warn('検索状態の保存に失敗しました:', error)
  }
}

// 検索状態をクリア
const clearSearchState = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.LAST_SEARCH_QUERY)
    localStorage.removeItem(STORAGE_KEYS.LAST_SEARCH_RESULTS)
    localStorage.removeItem(STORAGE_KEYS.SEARCH_TIMESTAMP)
  } catch (error) {
    console.warn('検索状態のクリアに失敗しました:', error)
  }
}

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

// 電子認証関連の状態
const signatureDialogVisible = ref(false)
const timestampDialogVisible = ref(false)
const newSignatureDialogVisible = ref(false)
const newTimestampDialogVisible = ref(false)
const selectedContract = ref<any>(null)
const contractSignatures = ref([])
const contractTimestamps = ref([])

// 契約情報追加関連の状態
const contractInfoDialogVisible = ref(false)
const contractInfoForm = ref({
  contractNumber: '',
  infoType: '',
  title: '',
  content: '',
  priority: 3,
  tags: []
})

const contractInfoRules = {
  infoType: [{ required: true, message: '情報タイプを選択してください', trigger: 'change' }],
  title: [{ required: true, message: 'タイトルを入力してください', trigger: 'blur' }],
  content: [{ required: true, message: '内容を入力してください', trigger: 'blur' }]
}

// 新規署名フォーム
const newSignatureForm = ref({
  signerId: '',
  documentType: 'CONTRACT',
  signatureData: '',
  documentHash: ''
})

const signatureRules = {
  signerId: [{ required: true, message: '署名者IDを入力してください', trigger: 'blur' }],
  documentType: [{ required: true, message: '文書タイプを選択してください', trigger: 'change' }],
  signatureData: [{ required: true, message: '署名データを入力してください', trigger: 'blur' }],
  documentHash: [{ required: true, message: '文書ハッシュを入力してください', trigger: 'blur' }]
}

// 新規タイムスタンプフォーム
const newTimestampForm = ref({
  documentType: 'CONTRACT',
  timestampAuthority: '',
  timestampCertificate: ''
})

const timestampRules = {
  documentType: [{ required: true, message: '文書タイプを選択してください', trigger: 'change' }],
  timestampAuthority: [{ required: true, message: '認証局名を入力してください', trigger: 'blur' }],
  timestampCertificate: [{ required: true, message: '証明書を入力してください', trigger: 'blur' }]
}

// モックデータ（バックエンドが起動できない場合の代替）
const mockContracts: Contract[] = [
  {
    id: 1,
    contractNumber: 'CTR-2024-001',
    propertyId: 1,
    clientId: 1,
    propertyName: 'サンプルマンションA',
    clientName: '田中太郎',
    type: ContractType.SALE,
    status: ContractStatus.ACTIVE,
    amount: 5000000,
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    terms: '標準的な売買契約条件',
    createdAt: '2024-01-01T09:00:00Z',
    updatedAt: '2024-01-01T09:00:00Z'
  },
  {
    id: 2,
    contractNumber: 'CTR-2024-002',
    propertyId: 2,
    clientId: 2,
    propertyName: 'サンプルマンションB',
    clientName: '佐藤花子',
    type: ContractType.RENTAL,
    status: ContractStatus.ACTIVE,
    amount: 120000,
    startDate: '2024-01-15',
    endDate: '2025-01-14',
    terms: '標準的な賃貸契約条件',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z'
  },
  {
    id: 3,
    contractNumber: 'CTR-2024-003',
    propertyId: 3,
    clientId: 3,
    propertyName: 'サンプルオフィス',
    clientName: '株式会社サンプル',
    type: ContractType.MANAGEMENT,
    status: ContractStatus.ACTIVE,
    amount: 50000,
    startDate: '2024-02-01',
    endDate: '2025-01-31',
    terms: '標準的な管理契約条件',
    createdAt: '2024-02-01T11:00:00Z',
    updatedAt: '2024-02-01T11:00:00Z'
  },
  {
    id: 4,
    contractNumber: 'CTR-2024-004',
    propertyId: 4,
    clientId: 4,
    propertyName: 'サンプル一戸建て',
    clientName: '山田次郎',
    type: ContractType.SALE,
    status: ContractStatus.EXPIRED,
    amount: 8000000,
    startDate: '2023-06-01',
    endDate: '2024-05-31',
    terms: '標準的な売買契約条件',
    createdAt: '2023-06-01T08:00:00Z',
    updatedAt: '2023-06-01T08:00:00Z'
  },
  {
    id: 5,
    contractNumber: 'CTR-2024-005',
    propertyId: 5,
    clientId: 5,
    propertyName: 'サンプル倉庫',
    clientName: '物流株式会社',
    type: ContractType.LEASE,
    status: ContractStatus.PENDING,
    amount: 300000,
    startDate: '2024-03-01',
    endDate: '2025-02-28',
    terms: '標準的な賃貸契約条件',
    createdAt: '2024-03-01T12:00:00Z',
    updatedAt: '2024-03-01T12:00:00Z'
  }
]

// 初期化
onMounted(() => {
  // モックデータを初期設定
  contracts.value = mockContracts
  totalRecords.value = mockContracts.length
  
  // 実際のAPIが利用可能になったら以下のコードを使用
  // if (!restoreSearchState()) {
  //   loadContracts()
  // }
  
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
    searchError.value = null
    
    // モックデータを使用（バックエンドが起動できない場合）
    console.log('モックデータを使用します')
    
    // 検索条件に基づいてモックデータをフィルタリング
    let filteredData = [...mockContracts]
    
    if (searchForm.status) {
      filteredData = filteredData.filter(c => c.status === searchForm.status)
      console.log('ステータスフィルタリング:', searchForm.status, '結果件数:', filteredData.length)
    }
    
    if (searchForm.type) {
      filteredData = filteredData.filter(c => c.type === searchForm.type)
      console.log('タイプフィルタリング:', searchForm.type, '結果件数:', filteredData.length)
    }
    
    // ページネーション処理
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    contracts.value = filteredData.slice(startIndex, endIndex)
    totalRecords.value = filteredData.length
    
    console.log('フィルタリング結果:', {
      searchForm: searchForm,
      filteredCount: filteredData.length,
      displayedCount: contracts.value.length
    })
    
    // 実際のAPIが利用可能になったら以下のコードを使用
    /*
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: searchForm.status || undefined,
      type: searchForm.type || undefined,
      sortBy: sortBy.value || undefined,
      sortOrder: sortOrder.value || undefined
    }
    
    const response = await contractApi.getContracts(params)
    const newContracts = response.data || []
    contracts.value = newContracts
    totalRecords.value = newContracts.length
    
    // 検索結果を保存
    if (isSearching.value) {
      lastSearchResults.value = [...newContracts]
      const queryString = JSON.stringify(searchForm)
      saveSearchState(queryString, newContracts)
    }
    */
    
  } catch (error) {
    console.error('契約データの読み込みに失敗しました:', error)
    searchError.value = 'データの読み込みに失敗しました。前回の検索結果を表示します。'
    
    // エラー時もモックデータを使用
    contracts.value = mockContracts
    totalRecords.value = mockContracts.length
  } finally {
    loading.value = false
  }
}

// 検索実行
const handleSearch = async () => {
  try {
    currentPage.value = 1
    isSearching.value = true
    lastSearchQuery.value = JSON.stringify(searchForm)
    searchError.value = null
    
    // 検索条件がある場合は検索APIを使用
    if (searchForm.status || searchForm.type) {
      const searchQuery = [searchForm.status, searchForm.type]
        .filter(Boolean)
        .join(' ')
      
      if (searchQuery.trim()) {
        console.log('検索クエリ:', searchQuery) // デバッグ用
        const response = await contractApi.search(searchQuery)
        const searchResults = response.data || []
        contracts.value = searchResults
        totalRecords.value = searchResults.length
        
        // 検索結果を保存
        lastSearchResults.value = [...searchResults]
        saveSearchState(lastSearchQuery.value, searchResults)
        
        // 検索結果の表示
        if (searchResults.length === 0) {
          ;(ElMessage as any).info('検索条件に一致する契約が見つかりませんでした')
        } else {
          ;(ElMessage as any).success(`${searchResults.length}件の契約が見つかりました`)
        }
        return
      }
    }
    
    // 検索条件がない場合は全データを読み込み
    await loadContracts()
  } catch (error) {
    console.error('契約検索に失敗しました:', error)
    searchError.value = '検索に失敗しました。前回の検索結果を表示します。'
    
    // 前回の検索結果がある場合は表示
    if (lastSearchResults.value.length > 0) {
      contracts.value = [...lastSearchResults.value]
      totalRecords.value = lastSearchResults.value.length
    } else {
      // エラー時は全データを読み込み
      await loadContracts()
    }
  }
}

// 検索リセット
const resetSearch = () => {
  Object.assign(searchForm, {
    status: '',
    type: ''
  })
  currentPage.value = 1
  isSearching.value = false
  lastSearchQuery.value = ''
  lastSearchResults.value = []
  searchError.value = null
  clearSearchState()
  loadContracts()
}

// ソート変更
const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  // 検索状態を保持
  if (isSearching.value) {
    loadContracts()
  }
}

// ページサイズ変更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  // 検索状態を保持
  if (isSearching.value) {
    loadContracts()
  }
}

// ページ変更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  // 検索状態を保持
  if (isSearching.value) {
    loadContracts()
  }
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
    
    // 検索状態を保持してデータを再読み込み
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

// 行毎の背景色を設定
const getRowClassName = ({ row, rowIndex }: { row: any; rowIndex: number }) => {
  if (rowIndex % 2 === 0) {
    return 'even-row'
  } else {
    return 'odd-row'
  }
}

// 通貨フォーマット
const formatCurrency = (amount: number) => {
  return amount.toLocaleString()
}

// 電子署名履歴を表示
const showSignatures = async (contract: any) => {
  selectedContract.value = contract
  signatureDialogVisible.value = true
  try {
    // 実際のAPIが実装されたら、ここでデータを取得
    // const response = await signatureApi.getSignaturesByContract(contract.id.toString())
    // contractSignatures.value = response.data
    
    // モックデータ（一時的）
    contractSignatures.value = [
      {
        id: 1,
        signerId: 'USER001',
        documentType: 'CONTRACT',
        signedAt: new Date(),
        status: 'VALID'
      }
    ]
  } catch (error) {
    (ElMessage as any).error('署名履歴の取得に失敗しました')
  }
}

// 電子タイムスタンプを表示
const showTimestamps = async (contract: any) => {
  selectedContract.value = contract
  timestampDialogVisible.value = true
  try {
    // 実際のAPIが実装されたら、ここでデータを取得
    // const response = await timestampApi.getTimestampsByDocument(contract.id.toString())
    // contractTimestamps.value = response.data
    
    // モックデータ（一時的）
    contractTimestamps.value = [
      {
        id: 1,
        documentType: 'CONTRACT',
        timestampAt: new Date(),
        timestampAuthority: 'JPNIC',
        status: 'VALID'
      }
    ]
  } catch (error) {
    (ElMessage as any).error('タイムスタンプの取得に失敗しました')
  }
}

// 新規署名作成ダイアログを表示
const createNewSignature = () => {
  newSignatureDialogVisible.value = true
}

// 新規タイムスタンプ作成ダイアログを表示
const createNewTimestamp = () => {
  newTimestampDialogVisible.value = true
}

// 署名作成を実行
const submitSignatureCreation = async () => {
  try {
    // 実際のAPIが実装されたら、ここで作成処理
    // await signatureApi.createSignature({
    //   signerId: newSignatureForm.value.signerId,
    //   contractId: selectedContract.value.id.toString(),
    //   ...newSignatureForm.value
    // })
    
    (ElMessage as any).success('電子署名が作成されました')
    newSignatureDialogVisible.value = false
    showSignatures(selectedContract.value)
  } catch (error) {
    (ElMessage as any).error('電子署名の作成に失敗しました')
  }
}

// タイムスタンプ作成を実行
const submitTimestampCreation = async () => {
  try {
    // 実際のAPIが実装されたら、ここで作成処理
    // await timestampApi.createTimestamp({
    //   documentId: selectedContract.value.id.toString(),
    //   ...newTimestampForm.value
    // })
    
    (ElMessage as any).success('電子タイムスタンプが作成されました')
    newTimestampDialogVisible.value = false
    showTimestamps(selectedContract.value)
  } catch (error) {
    (ElMessage as any).error('電子タイムスタンプの作成に失敗しました')
  }
}

// 署名検証
const verifySignature = async (signature: any) => {
  try {
    // 実際のAPIが実装されたら、ここで検証処理
    // const response = await signatureApi.verifySignature(signature.id, {})
    (ElMessage as any).success('署名の検証が完了しました')
  } catch (error) {
    (ElMessage as any).error('署名の検証に失敗しました')
  }
}

// タイムスタンプ検証
const verifyTimestamp = async (timestamp: any) => {
  try {
    // 実際のAPIが実装されたら、ここで検証処理
    // const response = await timestampApi.verifyTimestamp(timestamp.id)
    (ElMessage as any).success('タイムスタンプの検証が完了しました')
  } catch (error) {
    (ElMessage as any).error('タイムスタンプの検証に失敗しました')
  }
}

// 署名削除
const deleteSignature = async (signature: any) => {
  try {
    await ElMessageBox.confirm('この署名を削除しますか？', '確認', {
      confirmButtonText: '削除',
      cancelButtonText: 'キャンセル',
      type: 'warning'
    })
    
    // 実際のAPIが実装されたら、ここで削除処理
    // await signatureApi.deleteSignature(signature.id)
    (ElMessage as any).success('署名が削除されました')
    showSignatures(selectedContract.value)
  } catch (error) {
    if (error !== 'cancel') {
      (ElMessage as any).error('署名の削除に失敗しました')
    }
  }
}

// タイムスタンプ削除
const deleteTimestamp = async (timestamp: any) => {
  try {
    await ElMessageBox.confirm('このタイムスタンプを削除しますか？', '確認', {
      confirmButtonText: '削除',
      cancelButtonText: 'キャンセル',
      type: 'warning'
    })
    
    // 実際のAPIが実装されたら、ここで削除処理
    // await timestampApi.deleteTimestamp(timestamp.id)
    (ElMessage as any).success('タイムスタンプが削除されました')
    showTimestamps(selectedContract.value)
  } catch (error) {
    if (error !== 'cancel') {
      (ElMessage as any).error('タイムスタンプの削除に失敗しました')
    }
  }
}

// ヘルパー関数
const getDocumentTypeColor = (type: string) => {
  const colors: Record<string, string> = { CONTRACT: 'primary', AGREEMENT: 'success', INVOICE: 'warning', OTHER: 'info' }
  return colors[type] || 'info'
}

const getDocumentTypeLabel = (type: string) => {
  const labels: Record<string, string> = { CONTRACT: '契約書', AGREEMENT: '同意書', INVOICE: '請求書', OTHER: 'その他' }
  return labels[type] || type
}

const getSignatureStatusColor = (status: string) => {
  const colors: Record<string, string> = { VALID: 'success', INVALID: 'danger', EXPIRED: 'warning' }
  return colors[status] || 'info'
}

const getSignatureStatusLabel = (status: string) => {
  const labels: Record<string, string> = { VALID: '有効', INVALID: '無効', EXPIRED: '期限切れ' }
  return labels[status] || status
}

const getTimestampStatusColor = (status: string) => {
  const colors: Record<string, string> = { VALID: 'success', INVALID: 'danger', EXPIRED: 'warning' }
  return colors[status] || 'info'
}

const getTimestampStatusLabel = (status: string) => {
  const labels: Record<string, string> = { VALID: '有効', INVALID: '無効', EXPIRED: '期限切れ' }
  return labels[status] || status
}

// formatDate関数は既に定義済み

// 契約情報追加
const addContractInfo = (contract: Contract) => {
  contractInfoForm.value.contractNumber = contract.contractNumber
  contractInfoForm.value.infoType = ''
  contractInfoForm.value.title = ''
  contractInfoForm.value.content = ''
  contractInfoForm.value.priority = 3
  contractInfoForm.value.tags = []
  contractInfoDialogVisible.value = true
}

const submitContractInfo = async () => {
  try {
    // 実際のAPIで情報を保存
    // await contractInfoApi.create({
    //   contractId: selectedContract.value.id.toString(),
    //   ...contractInfoForm.value
    // })
    
    (ElMessage as any).success('契約情報が追加されました')
    contractInfoDialogVisible.value = false
  } catch (error) {
    console.error('契約情報の追加に失敗しました:', error)
    (ElMessage as any).error('契約情報の追加に失敗しました')
  }
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

/* カスタム行色 */
.contract-table .el-table__row:nth-child(even) {
  background-color: #f0f8ff;
}

.contract-table .el-table__row:nth-child(odd) {
  background-color: #ffffff;
}

.contract-table .el-table__row:hover {
  background-color: #e8f4fd !important;
}

/* ダークモード対応 */
.dark-mode .contract-table .el-table__row:nth-child(even) {
  background-color: #2d3748;
}

.dark-mode .contract-table .el-table__row:nth-child(odd) {
  background-color: #1a202c;
}

.dark-mode .contract-table .el-table__row:hover {
  background-color: #4a5568 !important;
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

.error-alert {
  margin-bottom: 20px;
}

.auth-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.auth-buttons .el-button {
  min-width: 80px;
  font-size: 12px;
  padding: 6px 12px;
}

.signature-actions,
.timestamp-actions {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 行毎の背景色設定 */
.contract-table .even-row {
  background-color: #f8f9fa !important;
}

.contract-table .odd-row {
  background-color: #ffffff !important;
}

.contract-table .even-row:hover {
  background-color: #e9ecef !important;
}

.contract-table .odd-row:hover {
  background-color: #f1f3f4 !important;
}

/* Element Plusのデフォルトスタイルを上書き */
.contract-table .el-table__row.even-row {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__row.odd-row {
  background-color: #ffffff !important;
}

/* より強力なスタイル上書き */
.contract-table .el-table__body tr.even-row {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row {
  background-color: #ffffff !important;
}

.contract-table .el-table__body tr.even-row:hover {
  background-color: #e9ecef !important;
}

.contract-table .el-table__body tr.odd-row:hover {
  background-color: #f1f3f4 !important;
}

/* インラインスタイルを上書き */
.contract-table .el-table__body tr.even-row td {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row td {
  background-color: #ffffff !important;
}

/* 最高優先度での背景色設定 */
.contract-table tr.even-row {
  background-color: #f8f9fa !important;
}

.contract-table tr.odd-row {
  background-color: #ffffff !important;
}

/* インラインスタイルを強制的に上書き */
.contract-table .el-table__body tr.even-row td[style*="background"] {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row td[style*="background"] {
  background-color: #ffffff !important;
}

/* グローバルスタイルでの上書き */
body .contract-table tr.even-row {
  background-color: #f8f9fa !important;
}

body .contract-table tr.odd-row {
  background-color: #ffffff !important;
}

/* 最も強力なセレクタ - Element Plusの内部スタイルを完全に上書き */
.contract-table .el-table__body tr.even-row,
.contract-table .el-table__body tr.even-row td,
.contract-table .el-table__body tr.even-row th {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row,
.contract-table .el-table__body tr.odd-row td,
.contract-table .el-table__body tr.odd-row th {
  background-color: #ffffff !important;
}

/* インラインスタイルを強制的に上書き */
.contract-table .el-table__body tr.even-row[style*="background"],
.contract-table .el-table__body tr.even-row td[style*="background"] {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row[style*="background"],
.contract-table .el-table__body tr.odd-row td[style*="background"] {
  background-color: #ffffff !important;
}

/* 疑似要素での上書き */
.contract-table .el-table__body tr.even-row::before,
.contract-table .el-table__body tr.even-row::after {
  background-color: #f8f9fa !important;
}

.contract-table .el-table__body tr.odd-row::before,
.contract-table .el-table__body tr.odd-row::after {
  background-color: #ffffff !important;
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
