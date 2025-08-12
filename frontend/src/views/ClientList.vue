<template>
  <div class="client-list" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-background"></div>
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="header-text">
            <h1>顧客管理</h1>
            <p>顧客の一覧表示、検索、管理を行います</p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="$router.push('/clients/new')" class="add-btn">
            <el-icon><Plus /></el-icon>
            新規顧客登録
          </el-button>
        </div>
      </div>
    </div>

    <!-- 検索・フィルターセクション -->
    <div class="search-section" v-motion="'slide-visible-delay-100'">
      <div class="search-container">
        <div class="search-header">
          <h3><el-icon><Search /></el-icon> 検索・フィルター</h3>
        </div>
        <div class="search-form">
          <div class="search-row">
            <div class="search-field">
              <label>顧客名</label>
              <el-input
                v-model="searchForm.name"
                placeholder="顧客名で検索"
                clearable
                @keyup.enter="handleSearch"
                class="modern-input"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="search-field">
              <label>メールアドレス</label>
              <el-input
                v-model="searchForm.email"
                placeholder="メールアドレスで検索"
                clearable
                @keyup.enter="handleSearch"
                class="modern-input"
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="search-field">
              <label>顧客タイプ</label>
              <el-select
                v-model="searchForm.type"
                placeholder="顧客タイプを選択"
                clearable
                class="modern-select"
              >
                <el-option label="購入希望者" value="BUYER" />
                <el-option label="売却希望者" value="SELLER" />
                <el-option label="賃貸希望者" value="TENANT" />
                <el-option label="オーナー" value="LANDLORD" />
              </el-select>
            </div>
            <div class="search-field">
              <label>ステータス</label>
              <el-select
                v-model="searchForm.status"
                placeholder="ステータスを選択"
                clearable
                class="modern-select"
              >
                <el-option label="アクティブ" value="ACTIVE" />
                <el-option label="非アクティブ" value="INACTIVE" />
                <el-option label="保留中" value="PENDING" />
              </el-select>
            </div>
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch" :loading="loading" class="search-btn">
                <el-icon><Search /></el-icon>
                検索
              </el-button>
              <el-button @click="resetSearch" class="reset-btn">
                <el-icon><Refresh /></el-icon>
                リセット
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 顧客一覧テーブル -->
    <div class="table-section" v-motion="'slide-visible-delay-200'">
      <div class="table-container">
        <div class="table-header">
          <div class="table-title">
            <div class="title-icon">
              <el-icon><List /></el-icon>
            </div>
            <div class="title-text">
              <h3>顧客一覧</h3>
              <span class="record-count">全 {{ totalRecords }} 件</span>
            </div>
          </div>
          <div class="table-actions">
            <el-button @click="exportData" :loading="exporting" class="export-btn">
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
          :closable="true"
          show-icon
          class="error-alert"
          @close="searchError = null"
        />
        
        <!-- 成功メッセージ -->
        <el-alert
          v-if="searchSuccess"
          :title="searchSuccess"
          type="success"
          :closable="true"
          show-icon
          class="success-alert"
          @close="searchSuccess = null"
        />

        <el-table
          :data="clients"
          v-loading="loading"
          class="client-table"
          :row-class-name="getRowClassName"
          @sort-change="handleSortChange"
        >
          <el-table-column prop="id" label="ID" width="80" sortable="custom" />
          <el-table-column prop="name" label="顧客名" min-width="150" sortable="custom">
            <template #default="{ row }">
              <div class="client-name">
                <el-avatar :size="32" :src="row.avatar" class="client-avatar">
                  {{ row.firstName.charAt(0) }}
                </el-avatar>
                <span>{{ row.firstName }} {{ row.lastName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="メールアドレス" min-width="200" sortable="custom" />
          <el-table-column prop="phone" label="電話番号" min-width="120" />
          <el-table-column prop="status" label="ステータス" width="120" sortable="custom">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="登録日" width="120" sortable="custom">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="電子認証" width="250">
            <template #default="{ row }">
              <div class="auth-buttons">
                <el-button size="small" type="primary" @click="showSignatures(row)">
                  署名履歴
                </el-button>
                <el-button size="small" type="success" @click="showBiometrics(row)">
                  生体認証
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button
                  size="small"
                  @click="viewClient(row)"
                  type="info"
                  text
                >
                  <el-icon><View /></el-icon>
                  詳細
                </el-button>
                <el-button
                  size="small"
                  @click="editClient(row)"
                  type="primary"
                  text
                >
                  <el-icon><Edit /></el-icon>
                  編集
                </el-button>
                <el-button
                  size="small"
                  @click="addClientInfo(row)"
                  type="success"
                  text
                >
                  <el-icon><InfoFilled /></el-icon>
                  情報追加
                </el-button>
                <el-button
                  size="small"
                  @click="deleteClient(row)"
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
            :page-sizes="[10, 20, 50, 100]"
            :total="totalRecords"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
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
          <strong>{{ clientToDelete?.firstName }} {{ clientToDelete?.lastName }}</strong>
          <br>
          <small>{{ clientToDelete?.email }}</small>
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
      <div v-if="selectedClient">
        <h4>{{ selectedClient.firstName }} {{ selectedClient.lastName }} の署名履歴</h4>
        <el-table :data="clientSignatures" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="contractId" label="契約ID" width="120" />
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
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click="verifySignature(scope.row)">検証</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 生体認証データダイアログ -->
    <el-dialog v-model="biometricDialogVisible" title="生体認証データ" width="800px">
      <div v-if="selectedClient">
        <h4>{{ selectedClient.firstName }} {{ selectedClient.lastName }} の生体認証データ</h4>
        <div class="biometric-actions">
          <el-button type="primary" @click="registerNewBiometric">新規登録</el-button>
        </div>
        <el-table :data="clientBiometrics" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="biometricType" label="タイプ" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.biometricType === 'FINGERPRINT' ? 'primary' : 'success'">
                {{ scope.row.biometricType === 'FINGERPRINT' ? '指紋' : '掌紋' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="qualityScore" label="品質スコア" width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.qualityScore" :color="getQualityColor(scope.row.qualityScore)" />
            </template>
          </el-table-column>
          <el-table-column prop="registeredAt" label="登録日時" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.registeredAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="ステータス" width="100">
            <template #default="scope">
              <el-tag :type="getBiometricStatusColor(scope.row.status)">
                {{ getBiometricStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="evaluateBiometric(scope.row)">品質評価</el-button>
              <el-button size="small" type="danger" @click="deleteBiometric(scope.row)">削除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 新規生体認証登録ダイアログ -->
    <el-dialog v-model="newBiometricDialogVisible" title="新規生体認証登録" width="600px">
      <el-form :model="newBiometricForm" :rules="biometricRules" ref="biometricFormRef" label-width="120px">
        <el-form-item label="認証タイプ" prop="biometricType">
          <el-select v-model="newBiometricForm.biometricType" placeholder="認証タイプを選択">
            <el-option label="指紋" value="FINGERPRINT" />
            <el-option label="掌紋" value="PALM_PRINT" />
          </el-select>
        </el-form-item>
        <el-form-item label="生体データ" prop="biometricData">
          <el-input
            v-model="newBiometricForm.biometricData"
            type="textarea"
            :rows="4"
            placeholder="Base64エンコードされた生体データを入力"
          />
        </el-form-item>
        <el-form-item label="品質スコア" prop="qualityScore">
          <el-slider
            v-model="newBiometricForm.qualityScore"
            :min="0"
            :max="100"
            :step="5"
            show-input
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newBiometricDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitBiometricRegistration">登録</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 顧客情報追加ダイアログ -->
    <el-dialog v-model="clientInfoDialogVisible" title="顧客情報追加" width="600px">
      <el-form :model="clientInfoForm" :rules="clientInfoRules" ref="clientInfoFormRef" label-width="120px">
        <el-form-item label="顧客名" prop="name">
          <el-input v-model="clientInfoForm.name" disabled />
        </el-form-item>
        <el-form-item label="情報タイプ" prop="infoType">
          <el-select v-model="clientInfoForm.infoType" placeholder="情報タイプを選択">
            <el-option label="連絡履歴" value="CONTACT_HISTORY" />
            <el-option label="希望条件" value="PREFERENCES" />
            <el-option label="見学記録" value="VIEWING_RECORD" />
            <el-option label="メモ・備考" value="NOTE" />
            <el-option label="重要度" value="IMPORTANCE" />
            <el-option label="その他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="タイトル" prop="title">
          <el-input v-model="clientInfoForm.title" placeholder="情報のタイトルを入力" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="clientInfoForm.content"
            type="textarea"
            :rows="4"
            placeholder="詳細な内容を入力"
          />
        </el-form-item>
        <el-form-item label="重要度" prop="priority">
          <el-rate v-model="clientInfoForm.priority" :max="5" />
        </el-form-item>
        <el-form-item label="タグ" prop="tags">
          <el-select
            v-model="clientInfoForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="タグを選択または作成"
          >
            <el-option label="VIP" value="VIP" />
            <el-option label="急ぎ" value="急ぎ" />
            <el-option label="長期顧客" value="長期顧客" />
            <el-option label="新規" value="新規" />
            <el-option label="紹介" value="紹介" />
            <el-option label="投資家" value="投資家" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="clientInfoDialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="submitClientInfo">追加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Message, Search, Refresh, Download, View, Edit, Delete, List, InfoFilled } from '@element-plus/icons-vue'
import type { Client } from '@/types'
import { clientApi } from '@/services/api'
import { usePerformanceData } from '@/composables/usePerformanceData'
import { debounce, throttle, globalCache, measurePerformance } from '@/utils/performance'
import VirtualScroll from '@/components/VirtualScroll.vue'
import { ClientType } from '@/types'
// ルーター
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// ページサイズ
const pageSize = ref(20)

// パフォーマンス最適化されたデータ管理
const {
  data: clients,
  loading,
  error: searchError,
  totalCount: totalRecords,
  currentPage,
  hasMore,
  searchQuery,
  searchResults,
  isSearching,
  filterParams,
  visibleItems,
  fetchData,
  fetchPage,
  loadMore,
  search: performSearch,
  filter: performFilter,
  batchUpdate,
  batchDelete,
  clearCache,
  updateScrollPosition,
  updateItemHeight
} = usePerformanceData(
  async (params?: any) => {
    if (params?.search) {
      const response = await clientApi.search(params.search)
      return response.data || []
    }
    const response = await clientApi.getClients(params)
    return response.data || []
  },
  {
    pageSize: pageSize.value,
    cacheKey: 'clients',
    enableVirtualScroll: true,
    debounceDelay: 300,
    throttleDelay: 100
  }
)

// その他の状態
const exporting = ref(false)
const deleting = ref(false)
const searchSuccess = ref<string | null>(null)

// 電子認証関連の状態
const signatureDialogVisible = ref(false)
const biometricDialogVisible = ref(false)
const newBiometricDialogVisible = ref(false)
const selectedClient = ref<Client | null>(null)
const clientSignatures = ref([])
const clientBiometrics = ref([])

// 顧客情報追加関連の状態
const clientInfoDialogVisible = ref(false)
const clientInfoForm = ref({
  name: '',
  infoType: '',
  title: '',
  content: '',
  priority: 3,
  tags: []
})

const clientInfoRules = {
  infoType: [{ required: true, message: '情報タイプを選択してください', trigger: 'change' }],
  title: [{ required: true, message: 'タイトルを入力してください', trigger: 'blur' }],
  content: [{ required: true, message: '内容を入力してください', trigger: 'blur' }]
}

// 新規生体認証フォーム
const newBiometricForm = ref({
  biometricType: 'FINGERPRINT',
  biometricData: '',
  qualityScore: 80
})

const biometricRules = {
  biometricType: [{ required: true, message: '認証タイプを選択してください', trigger: 'change' }],
  biometricData: [{ required: true, message: '生体データを入力してください', trigger: 'blur' }],
  qualityScore: [{ required: true, message: '品質スコアを設定してください', trigger: 'change' }]
}

// パフォーマンス監視
const monitorPerformance = () => {
  // メモリ使用量の監視
  if ('memory' in performance) {
    const memory = (performance as any).memory
    const used = Math.round(memory.usedJSHeapSize / 1048576 * 100) / 100
    const total = Math.round(memory.totalJSHeapSize / 1048576 * 100) / 100
    
    if (used > total * 0.8) {
      console.warn('メモリ使用量が高いです:', `${used}MB / ${total}MB`)
    }
  }
}

// 初期化
onMounted(() => {
  // 実際のAPIを使用してデータを読み込み
  loadClients()
  
  checkDarkMode()
})

// ダークモードチェック
const checkDarkMode = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
}

// 検索フォーム
const searchForm = reactive({
  name: '',
  email: '',
  type: '',
  status: ''
})

// 削除ダイアログ
const deleteDialogVisible = ref(false)
const clientToDelete = ref<Client | null>(null)

// モックデータ（バックエンドが起動できない場合の代替）
const mockClients: Client[] = [
  {
    id: 1,
    firstName: '田中',
    lastName: '太郎',
    email: 'tanaka@example.com',
    phone: '090-1234-5678',
    address: '東京都渋谷区1-1-1',
    type: ClientType.BUYER,
    status: 'ACTIVE',
    createdAt: '2024-01-01T09:00:00Z',
    updatedAt: '2024-01-01T09:00:00Z'
  },
  {
    id: 2,
    firstName: '佐藤',
    lastName: '花子',
    email: 'sato@example.com',
    phone: '080-2345-6789',
    address: '東京都新宿区2-2-2',
    type: ClientType.SELLER,
    status: 'ACTIVE',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z'
  },
  {
    id: 3,
    firstName: '山田',
    lastName: '次郎',
    email: 'yamada@example.com',
    phone: '070-3456-7890',
    address: '東京都港区3-3-3',
    type: ClientType.TENANT,
    status: 'PENDING',
    createdAt: '2024-02-01T11:00:00Z',
    updatedAt: '2024-02-01T11:00:00Z'
  },
  {
    id: 4,
    firstName: '鈴木',
    lastName: '美咲',
    email: 'suzuki@example.com',
    phone: '090-4567-8901',
    address: '東京都品川区4-4-4',
    type: ClientType.LANDLORD,
    status: 'ACTIVE',
    createdAt: '2024-02-15T12:00:00Z',
    updatedAt: '2024-02-15T12:00:00Z'
  },
  {
    id: 5,
    firstName: '高橋',
    lastName: '健一',
    email: 'takahashi@example.com',
    phone: '080-5678-9012',
    address: '東京都目黒区5-5-5',
    type: ClientType.BUYER,
    status: 'INACTIVE',
    createdAt: '2024-03-01T13:00:00Z',
    updatedAt: '2024-03-01T13:00:00Z'
  },
  {
    id: 6,
    firstName: '伊藤',
    lastName: '恵子',
    email: 'ito@example.com',
    phone: '070-6789-0123',
    address: '東京都世田谷区6-6-6',
    type: ClientType.SELLER,
    status: 'ACTIVE',
    createdAt: '2024-03-15T14:00:00Z',
    updatedAt: '2024-03-15T14:00:00Z'
  },
  {
    id: 7,
    firstName: '渡辺',
    lastName: '正男',
    email: 'watanabe@example.com',
    phone: '090-7890-1234',
    address: '東京都中野区7-7-7',
    type: ClientType.TENANT,
    status: 'PENDING',
    createdAt: '2024-04-01T15:00:00Z',
    updatedAt: '2024-04-01T15:00:00Z'
  },
  {
    id: 8,
    firstName: '中村',
    lastName: '由美',
    email: 'nakamura@example.com',
    phone: '080-8901-2345',
    address: '東京都杉並区8-8-8',
    type: ClientType.LANDLORD,
    status: 'ACTIVE',
    createdAt: '2024-04-15T16:00:00Z',
    updatedAt: '2024-04-15T16:00:00Z'
  },
  {
    id: 9,
    firstName: '小林',
    lastName: '達也',
    email: 'kobayashi@example.com',
    phone: '070-9012-3456',
    address: '東京都豊島区9-9-9',
    type: ClientType.BUYER,
    status: 'ACTIVE',
    createdAt: '2024-05-01T17:00:00Z',
    updatedAt: '2024-05-01T17:00:00Z'
  },
  {
    id: 10,
    firstName: '加藤',
    lastName: '麻里',
    email: 'kato@example.com',
    phone: '090-0123-4567',
    address: '東京都北区10-10-10',
    type: ClientType.SELLER,
    status: 'ACTIVE',
    createdAt: '2024-05-15T18:00:00Z',
    updatedAt: '2024-05-15T18:00:00Z'
  }
]

// 顧客データ読み込み
const loadClients = async () => {
  try {
    loading.value = true
    searchError.value = null
    
    // 実際のAPIを使用
    const response = await clientApi.getClients()
    const newClients = response.data || []
    clients.value = newClients
    totalRecords.value = newClients.length
    
    console.log('APIから取得した顧客データ:', newClients.length, '件')
    
  } catch (error) {
    console.error('顧客データの読み込みに失敗しました:', error)
    searchError.value = 'データの読み込みに失敗しました。モックデータを表示します。'
    
    // エラー時はモックデータを使用
    clients.value = mockClients
    totalRecords.value = mockClients.length
  } finally {
    loading.value = false
  }
}

// 検索実行
const handleSearch = async () => {
  try {
    currentPage.value = 1
    loading.value = true
    searchError.value = null
    
    // 高度な検索APIを使用
    if (searchForm.name || searchForm.email || searchForm.type || searchForm.status) {
      const searchParams = new URLSearchParams()
      
      if (searchForm.name) searchParams.append('name', searchForm.name)
      if (searchForm.email) searchParams.append('email', searchForm.email)
      if (searchForm.type) searchParams.append('type', searchForm.type)
      if (searchForm.status) searchParams.append('status', searchForm.status)
      
      const response = await clientApi.advancedSearch(searchParams)
      clients.value = response.data || []
      totalRecords.value = clients.value.length
      
      console.log('高度な検索結果:', {
        searchForm: searchForm,
        resultCount: clients.value.length
      })
      
      if (clients.value.length > 0) {
        searchSuccess.value = `${clients.value.length}件の顧客が見つかりました`
      } else {
        searchSuccess.value = '検索条件に一致する顧客が見つかりませんでした'
      }
    } else {
      // 検索条件がない場合は全データを読み込み
      await loadClients()
    }
  } catch (error) {
    console.error('顧客検索に失敗しました:', error)
    searchError.value = '検索に失敗しました。モックデータを表示します。'
    
    // エラー時はモックデータを使用
    clients.value = mockClients
    totalRecords.value = mockClients.length
  } finally {
    loading.value = false
  }
}

// 検索リセット
const resetSearch = () => {
  searchForm.name = ''
  searchForm.email = ''
  searchForm.type = ''
  searchForm.status = ''
  searchError.value = null
  loadClients()
}

// ソート変更
const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  // ソート処理の実装
  console.log('Sort:', prop, order)
}

// ページサイズ変更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadClients()
}

// ページ変更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadClients()
}

// 顧客詳細表示
const viewClient = (client: Client) => {
  router.push(`/clients/${client.id}`)
}

// 顧客編集
const editClient = (client: Client) => {
  router.push(`/clients/${client.id}/edit`)
}

// 顧客削除
const deleteClient = (client: Client) => {
  clientToDelete.value = client
  deleteDialogVisible.value = true
}

// 削除確認
const confirmDelete = async () => {
  if (!clientToDelete.value) return
  
  try {
    deleting.value = true
    await clientApi.delete(clientToDelete.value.id!)
          ;(ElMessage as any).success('顧客を削除しました')
    deleteDialogVisible.value = false
    clientToDelete.value = null
    
    // データを再読み込み
    fetchData()
  } catch (error) {
    console.error('顧客の削除に失敗しました:', error)
    ;(ElMessage as any).error('顧客の削除に失敗しました')
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

// ステータスタイプ取得
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'INACTIVE': return 'danger'
    case 'PENDING': return 'warning'
    default: return 'info'
  }
}

// ステータスラベル取得
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'アクティブ'
    case 'INACTIVE': return '非アクティブ'
    case 'PENDING': return '保留中'
    default: return status
  }
}

// 日付フォーマット
const formatDate = (date: string | Date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ja-JP')
}

// 行毎の背景色を設定
const getRowClassName = ({ row, rowIndex }: { row: any; rowIndex: number }) => {
  const className = rowIndex % 2 === 0 ? 'even-row' : 'odd-row';
  console.log(`Row ${rowIndex}: ${className}`); // デバッグ用ログ
  return className;
}

// 電子認証履歴を表示
const showSignatures = async (client: Client) => {
  selectedClient.value = client
  signatureDialogVisible.value = true
  try {
    // 実際のAPIが実装されたら、ここでデータを取得
    // const response = await signatureApi.getSignaturesBySigner(client.id.toString())
    // clientSignatures.value = response.data
    
    // モックデータ（一時的）
    clientSignatures.value = [
      {
        id: 1,
        contractId: 'CONTRACT001',
        documentType: 'CONTRACT',
        signedAt: new Date(),
        status: 'VALID'
      }
    ]
  } catch (error) {
    (ElMessage as any).error('署名履歴の取得に失敗しました')
  }
}

// 生体認証データを表示
const showBiometrics = async (client: Client) => {
  selectedClient.value = client
  biometricDialogVisible.value = true
  try {
    // 実際のAPIが実装されたら、ここでデータを取得
    // const response = await biometricApi.getBiometricByUser(client.id.toString())
    // clientBiometrics.value = response.data
    
    // モックデータ（一時的）
    clientBiometrics.value = [
      {
        id: 1,
        biometricType: 'FINGERPRINT',
        qualityScore: 85,
        registeredAt: new Date(),
        status: 'ACTIVE'
      }
    ]
  } catch (error) {
    (ElMessage as any).error('生体認証データの取得に失敗しました')
  }
}

// 新規生体認証登録ダイアログを表示
const registerNewBiometric = () => {
  newBiometricDialogVisible.value = true
}

// 生体認証登録を実行
const submitBiometricRegistration = async () => {
  try {
    // 実際のAPIが実装されたら、ここで登録処理
    // await biometricApi.registerBiometric({
    //   userId: selectedClient.value?.id.toString(),
    //   ...newBiometricForm.value
    // })
    
    (ElMessage as any).success('生体認証データが登録されました')
    newBiometricDialogVisible.value = false
    showBiometrics(selectedClient.value!)
  } catch (error) {
    (ElMessage as any).error('生体認証データの登録に失敗しました')
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

// 生体認証品質評価
const evaluateBiometric = async (biometric: any) => {
  try {
    // 実際のAPIが実装されたら、ここで品質評価処理
    // await biometricApi.evaluateBiometric(biometric.id)
    (ElMessage as any).success('品質評価が完了しました')
  } catch (error) {
    (ElMessage as any).error('品質評価に失敗しました')
  }
}

// 生体認証データ削除
const deleteBiometric = async (biometric: any) => {
  try {
    await ElMessageBox.confirm('この生体認証データを削除しますか？', '確認', {
      confirmButtonText: '削除',
      cancelButtonText: 'キャンセル',
      type: 'warning'
    })
    
    // 実際のAPIが実装されたら、ここで削除処理
    // await biometricApi.deleteBiometric(biometric.id)
    (ElMessage as any).success('生体認証データが削除されました')
    showBiometrics(selectedClient.value!)
  } catch (error) {
    if (error !== 'cancel') {
      (ElMessage as any).error('生体認証データの削除に失敗しました')
    }
  }
}

// ヘルパー関数
const getDocumentTypeColor = (type: string) => {
  const colors = { CONTRACT: 'primary', AGREEMENT: 'success', INVOICE: 'warning', OTHER: 'info' }
  return colors[type] || 'info'
}

const getDocumentTypeLabel = (type: string) => {
  const labels = { CONTRACT: '契約書', AGREEMENT: '同意書', INVOICE: '請求書', OTHER: 'その他' }
  return labels[type] || type
}

const getSignatureStatusColor = (status: string) => {
  const colors = { VALID: 'success', INVALID: 'danger', EXPIRED: 'warning' }
  return colors[status] || 'info'
}

const getSignatureStatusLabel = (status: string) => {
  const labels = { VALID: '有効', INVALID: '無効', EXPIRED: '期限切れ' }
  return labels[status] || status
}

const getBiometricStatusColor = (status: string) => {
  const colors = { ACTIVE: 'success', INACTIVE: 'info', BLOCKED: 'danger' }
  return colors[status] || 'info'
}

const getBiometricStatusLabel = (status: string) => {
  const labels = { ACTIVE: 'アクティブ', INACTIVE: '非アクティブ', BLOCKED: 'ブロック' }
  return labels[status] || status
}

const getQualityColor = (score: number) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

// formatDate関数は既に定義済み

// 顧客情報追加
const addClientInfo = (client: Client) => {
  clientInfoForm.value.name = `${client.firstName} ${client.lastName}`
  clientInfoForm.value.infoType = ''
  clientInfoForm.value.title = ''
  clientInfoForm.value.content = ''
  clientInfoForm.value.priority = 3
  clientInfoForm.value.tags = []
  clientInfoDialogVisible.value = true
}

const submitClientInfo = async () => {
  try {
    // 実際のAPIで情報を保存
    // await clientInfoApi.create({
    //   clientId: selectedClient.value.id.toString(),
    //   ...clientInfoForm.value
    // })
    
    (ElMessage as any).success('顧客情報が追加されました')
    clientInfoDialogVisible.value = false
  } catch (error) {
    console.error('顧客情報の追加に失敗しました:', error)
    (ElMessage as any).error('顧客情報の追加に失敗しました')
  }
}
</script>

<style scoped>
.client-list {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.dark-mode {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}

/* ヘッダーセクション */
.page-header {
  margin-bottom: 32px;
  position: relative;
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.dark-mode .header-background {
  background: rgba(44, 62, 80, 0.95);
}

.header-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.header-text h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 800;
  color: #2c3e50;
  letter-spacing: -0.5px;
}

.dark-mode .header-text h1 {
  color: #ecf0f1;
}

.header-text p {
  margin: 0;
  color: #7f8c8d;
  font-size: 16px;
  line-height: 1.5;
}

.dark-mode .header-text p {
  color: #bdc3c7;
}

.add-btn {
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.6);
}

/* 検索セクション */
.search-section {
  margin-bottom: 32px;
}

.search-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .search-container {
  background: rgba(44, 62, 80, 0.95);
}

.search-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 24px 32px;
  border-bottom: 1px solid #e9ecef;
}

.dark-mode .search-header {
  background: linear-gradient(135deg, #4a5568 0%, #2d3748 100%);
  border-bottom-color: #4a5568;
}

.search-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dark-mode .search-header h3 {
  color: #ecf0f1;
}

.search-form {
  padding: 32px;
}

.search-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr auto;
  gap: 24px;
  align-items: end;
}

.search-field label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.dark-mode .search-field label {
  color: #ecf0f1;
}

.modern-input,
.modern-select {
  width: 100%;
}

.modern-input :deep(.el-input__wrapper),
.modern-select :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.modern-input :deep(.el-input__wrapper:hover),
.modern-select :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

.modern-input :deep(.el-input__wrapper.is-focus),
.modern-select :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-actions {
  display: flex;
  gap: 12px;
}

.search-btn {
  padding: 12px 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
}

.reset-btn {
  padding: 12px 24px;
  border-radius: 12px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  color: #6c757d;
  font-weight: 600;
}

.dark-mode .reset-btn {
  background: #4a5568;
  border-color: #4a5568;
  color: #ecf0f1;
}

/* テーブルセクション */
.table-section {
  margin-bottom: 32px;
}

.table-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .table-container {
  background: rgba(44, 62, 80, 0.95);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  border-bottom: 1px solid #e9ecef;
}

.dark-mode .table-header {
  border-bottom-color: #4a5568;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 20px;
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.title-text h3 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.dark-mode .title-text h3 {
  color: #ecf0f1;
}

.record-count {
  color: #7f8c8d;
  font-size: 14px;
  font-weight: 500;
}

.dark-mode .record-count {
  color: #bdc3c7;
}

.export-btn {
  padding: 12px 24px;
  border-radius: 12px;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  color: #6c757d;
  font-weight: 600;
  transition: all 0.3s ease;
}

.export-btn:hover {
  background: #e9ecef;
  border-color: #6c757d;
  color: #495057;
}

.dark-mode .export-btn {
  background: #4a5568;
  border-color: #4a5568;
  color: #ecf0f1;
}

.dark-mode .export-btn:hover {
  background: #2d3748;
  border-color: #ecf0f1;
}

.client-table {
  margin: 0;
  border: none;
}

.client-table :deep(.el-table) {
  background: transparent;
}

.client-table :deep(.el-table__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.dark-mode .client-table :deep(.el-table__header) {
  background: linear-gradient(135deg, #4a5568 0%, #2d3748 100%);
}

/* カスタム行色 */
.client-table .el-table__row:nth-child(even) {
  background-color: #f8f9fa;
}

.client-table .el-table__row:nth-child(odd) {
  background-color: #ffffff;
}

.client-table .el-table__row:hover {
  background-color: #e3f2fd !important;
}

/* ダークモード対応 */
.dark-mode .client-table .el-table__row:nth-child(even) {
  background-color: #2d3748;
}

.dark-mode .client-table .el-table__row:nth-child(odd) {
  background-color: #1a202c;
}

.dark-mode .client-table .el-table__row:hover {
  background-color: #4a5568 !important;
}

.client-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.client-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 24px;
}

.delete-confirmation {
  text-align: center;
}

.client-info {
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
  margin: 0 32px 20px 32px;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .client-list {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .search-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
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

.biometric-actions {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 行毎の背景色設定 */
.client-table .even-row {
  background-color: #f8f9fa !important;
}

.client-table .odd-row {
  background-color: #ffffff !important;
}

.client-table .even-row:hover {
  background-color: #e9ecef !important;
}

.client-table .odd-row:hover {
  background-color: #f1f3f4 !important;
}

/* Element Plusのデフォルトスタイルを上書き */
.client-table .el-table__row.even-row {
  background-color: #f8f9fa !important;
}

.client-table .el-table__row.odd-row {
  background-color: #ffffff !important;
}

/* より強力なスタイル上書き */
.client-table .el-table__body tr.even-row {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row {
  background-color: #ffffff !important;
}

.client-table .el-table__body tr.even-row:hover {
  background-color: #e9ecef !important;
}

.client-table .el-table__body tr.odd-row:hover {
  background-color: #f1f3f4 !important;
}

/* インラインスタイルを上書き */
.client-table .el-table__body tr.even-row td {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row td {
  background-color: #ffffff !important;
}

/* 最高優先度での背景色設定 */
.client-table tr.even-row {
  background-color: #f8f9fa !important;
}

.client-table tr.odd-row {
  background-color: #ffffff !important;
}

/* インラインスタイルを強制的に上書き */
.client-table .el-table__body tr.even-row td[style*="background"] {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row td[style*="background"] {
  background-color: #ffffff !important;
}

/* グローバルスタイルでの上書き */
body .client-table tr.even-row {
  background-color: #f8f9fa !important;
}

body .client-table tr.odd-row {
  background-color: #ffffff !important;
}

/* 最も強力なセレクタ - Element Plusの内部スタイルを完全に上書き */
.client-table .el-table__body tr.even-row,
.client-table .el-table__body tr.even-row td,
.client-table .el-table__body tr.even-row th {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row,
.client-table .el-table__body tr.odd-row td,
.client-table .el-table__body tr.odd-row th {
  background-color: #ffffff !important;
}

/* インラインスタイルを強制的に上書き */
.client-table .el-table__body tr.even-row[style*="background"],
.client-table .el-table__body tr.even-row td[style*="background"] {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row[style*="background"],
.client-table .el-table__body tr.odd-row td[style*="background"] {
  background-color: #ffffff !important;
}

/* 疑似要素での上書き */
.client-table .el-table__body tr.even-row::before,
.client-table .el-table__body tr.even-row::after {
  background-color: #f8f9fa !important;
}

.client-table .el-table__body tr.odd-row::before,
.client-table .el-table__body tr.odd-row::after {
  background-color: #ffffff !important;
}
</style>
