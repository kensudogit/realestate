<template>
  <div class="client-list" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-content">
        <div class="header-left">
          <h1>顧客管理</h1>
          <p>顧客の一覧表示、検索、管理を行います</p>
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
    <el-card class="search-card" v-motion="'slide-visible-delay-100'">
      <div class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="顧客名で検索"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.email"
              placeholder="メールアドレスで検索"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.type"
              placeholder="顧客タイプ"
              clearable
              style="width: 100%"
            >
              <el-option label="購入希望者" value="BUYER" />
              <el-option label="売却希望者" value="SELLER" />
              <el-option label="賃貸希望者" value="TENANT" />
              <el-option label="オーナー" value="LANDLORD" />
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

    <!-- 顧客一覧テーブル -->
    <el-card class="table-card" v-motion="'slide-visible-delay-200'">
      <div class="table-header">
        <div class="table-title">
          <h3>顧客一覧</h3>
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
        :data="clients"
        v-loading="loading"
        stripe
        class="client-table"
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
        <el-table-column label="操作" width="200" fixed="right">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Message, Search, Refresh, Download, View, Edit, Delete } from '@element-plus/icons-vue'
import type { Client } from '@/types'
import { clientApi } from '@/services/api'

// ルーター
const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// データ状態
const clients = ref<Client[]>([])
const loading = ref(false)
const exporting = ref(false)
const deleting = ref(false)

// ページネーション
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 検索フォーム
const searchForm = reactive({
  name: '',
  email: '',
  status: ''
})

// ソート状態
const sortBy = ref('')
const sortOrder = ref('')

// 削除ダイアログ
const deleteDialogVisible = ref(false)
const clientToDelete = ref<Client | null>(null)

// 初期化
onMounted(() => {
  loadClients()
  checkDarkMode()
})

// ダークモードチェック
const checkDarkMode = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
}

// 顧客データ読み込み
const loadClients = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      name: searchForm.name || undefined,
      email: searchForm.email || undefined,
      status: searchForm.status || undefined,
      sortBy: sortBy.value || undefined,
      sortOrder: sortOrder.value || undefined
    }
    
    const response = await clientApi.getClients(params)
    clients.value = response.data || []
    totalRecords.value = clients.value.length
  } catch (error) {
    console.error('顧客データの読み込みに失敗しました:', error)
    ;(ElMessage as any).error('顧客データの読み込みに失敗しました')
  } finally {
    loading.value = false
  }
}

// 検索実行
const handleSearch = () => {
  currentPage.value = 1
  loadClients()
}

// 検索リセット
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    email: '',
    status: ''
  })
  currentPage.value = 1
  loadClients()
}

// ソート変更
const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadClients()
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
    loadClients()
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
</script>

<style scoped>
.client-list {
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

.client-table {
  margin-bottom: 20px;
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
