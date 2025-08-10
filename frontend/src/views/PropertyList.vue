<template>
  <div class="property-list" :class="{ 'dark-mode': isDarkMode }">
    <!-- ヘッダーセクション -->
    <div class="page-header" v-motion="'fade-visible'">
      <div class="header-content">
        <div class="header-left">
          <h1>物件管理</h1>
          <p>物件の一覧表示、検索、管理を行います</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="$router.push('/properties/new')" class="add-btn">
            <el-icon><Plus /></el-icon>
            新規物件登録
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
              v-model="searchQuery"
              placeholder="物件名・住所で検索..."
              clearable
              @input="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filters.type"
              placeholder="物件タイプ"
              clearable
              @change="handleFilterChange"
              class="filter-select"
            >
              <el-option
                v-for="type in propertyTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filters.status"
              placeholder="ステータス"
              clearable
              @change="handleFilterChange"
              class="filter-select"
            >
              <el-option
                v-for="status in propertyStatuses"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filters.priceRange"
              placeholder="価格帯"
              clearable
              @change="handleFilterChange"
              class="filter-select"
            >
              <el-option label="1000万円以下" value="0-10000000" />
              <el-option label="1000-3000万円" value="10000000-30000000" />
              <el-option label="3000-5000万円" value="30000000-50000000" />
              <el-option label="5000万円以上" value="50000000-999999999" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <div class="search-actions">
              <el-button @click="resetFilters" class="reset-btn">
                <el-icon><Refresh /></el-icon>
                リセット
              </el-button>
              <el-button type="primary" @click="exportData" :loading="exportLoading">
                <el-icon><Download /></el-icon>
                エクスポート
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 物件一覧 -->
    <el-card class="list-card" v-motion="'slide-visible-delay-200'">
      <template #header>
        <div class="card-header">
          <span>物件一覧 ({{ totalCount }}件)</span>
          <div class="view-controls">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="table">テーブル</el-radio-button>
              <el-radio-button label="grid">グリッド</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- ローディング状態 -->
      <div v-if="loading" class="loading-overlay">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- テーブルビュー -->
      <div v-else-if="viewMode === 'table'" class="table-view">
        <el-table
          :data="filteredProperties"
          style="width: 100%"
          @row-click="handleRowClick"
          class="property-table"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="物件名" min-width="200">
            <template #default="{ row }">
              <div class="property-name">
                <span class="name-text">{{ row.name }}</span>
                <el-tag :type="getTypeTagType(row.type)" size="small" class="type-tag">
                  {{ getTypeLabel(row.type) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="住所" min-width="250" />
          <el-table-column prop="price" label="価格" width="150">
            <template #default="{ row }">
              <span class="price-text">¥{{ formatNumber(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="area" label="面積" width="100">
            <template #default="{ row }">
              {{ row.area }}㎡
            </template>
          </el-table-column>
          <el-table-column prop="status" label="ステータス" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="登録日" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  size="small"
                  @click.stop="viewProperty(row)"
                  class="view-btn"
                >
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  @click.stop="editProperty(row)"
                  class="edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click.stop="deleteProperty(row)"
                  class="delete-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- グリッドビュー -->
      <div v-else class="grid-view">
        <el-row :gutter="20">
          <el-col
            v-for="property in filteredProperties"
            :key="property.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            class="property-col"
          >
            <el-card
              class="property-card"
              @click="viewProperty(property)"
              :class="{ 'sold': property.status === 'SOLD' }"
            >
              <div class="property-image">
                <el-image
                  :src="getPropertyImage(property)"
                  fit="cover"
                  class="image"
                  :preview-src-list="[getPropertyImage(property)]"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="property-badge">
                  <el-tag :type="getTypeTagType(property.type)" size="small">
                    {{ getTypeLabel(property.type) }}
                  </el-tag>
                </div>
              </div>
              <div class="property-info">
                <h3 class="property-title">{{ property.name }}</h3>
                <p class="property-address">{{ property.address }}</p>
                <div class="property-details">
                  <span class="price">¥{{ formatNumber(property.price) }}</span>
                  <span class="area">{{ property.area }}㎡</span>
                </div>
                <div class="property-status">
                  <el-tag :type="getStatusTagType(property.status)" size="small">
                    {{ getStatusLabel(property.status) }}
                  </el-tag>
                </div>
              </div>
              <div class="property-actions">
                <el-button size="small" @click.stop="editProperty(property)">
                  <el-icon><Edit /></el-icon>
                  編集
                </el-button>
                <el-button size="small" type="danger" @click.stop="deleteProperty(property)">
                  <el-icon><Delete /></el-icon>
                  削除
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- ページネーション -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 削除確認ダイアログ -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="物件削除の確認"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>以下の物件を削除してもよろしいですか？</p>
      <p class="delete-target">{{ propertyToDelete?.name }}</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">キャンセル</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">
            削除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { propertyApi } from '@/services/api'
import { Property, PropertyType, PropertyStatus } from '@/types'
import {
  Plus,
  Search,
  Refresh,
  Download,
  View,
  Edit,
  Delete,
  Picture
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 状態管理
const loading = ref(false)
const exportLoading = ref(false)
const deleteLoading = ref(false)
const deleteDialogVisible = ref(false)
const propertyToDelete = ref<Property | null>(null)
const isDarkMode = ref(false)

// データ
const properties = ref<Property[]>([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const viewMode = ref<'table' | 'grid'>('table')

// 検索・フィルター
const searchQuery = ref('')
const filters = ref({
  type: '',
  status: '',
  priceRange: ''
})

// 物件タイプとステータスの定義
const propertyTypes = [
  { value: PropertyType.APARTMENT, label: 'マンション' },
  { value: PropertyType.HOUSE, label: '一戸建て' },
  { value: PropertyType.COMMERCIAL, label: '商業施設' },
  { value: PropertyType.LAND, label: '土地' },
  { value: PropertyType.OFFICE, label: 'オフィス' },
  { value: PropertyType.WAREHOUSE, label: '倉庫' }
]

const propertyStatuses = [
  { value: PropertyStatus.AVAILABLE, label: '空室' },
  { value: PropertyStatus.SOLD, label: '売却済み' },
  { value: PropertyStatus.RENTED, label: '賃貸中' },
  { value: PropertyStatus.MAINTENANCE, label: 'メンテナンス中' }
]

// フィルタリングされた物件
const filteredProperties = computed(() => {
  let filtered = properties.value

  // 検索クエリでフィルタリング
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(property =>
      property.name.toLowerCase().includes(query) ||
      property.address.toLowerCase().includes(query)
    )
  }

  // タイプでフィルタリング
  if (filters.value.type) {
    filtered = filtered.filter(property => property.type === filters.value.type)
  }

  // ステータスでフィルタリング
  if (filters.value.status) {
    filtered = filtered.filter(property => property.status === filters.value.status)
  }

  // 価格帯でフィルタリング
  if (filters.value.priceRange) {
    const [min, max] = filters.value.priceRange.split('-').map(Number)
    filtered = filtered.filter(property => 
      property.price >= min && property.price <= max
    )
  }

  return filtered
})

// ライフサイクル
onMounted(() => {
  loadProperties()
})

// データ取得
const loadProperties = async () => {
  try {
    loading.value = true
    // 実際のAPIからデータを取得
    const response = await propertyApi.getAll()
    properties.value = response.data || []
    totalCount.value = properties.value.length
  } catch (error) {
    console.error('物件データの取得に失敗しました:', error)
    // ダミーデータを使用
    loadDummyData()
  } finally {
    loading.value = false
  }
}

const loadDummyData = () => {
  const dummyProperties: Property[] = [
    {
      id: 1,
      name: 'サンプルマンションA',
      address: '東京都渋谷区1-1-1',
      description: '駅徒歩5分の好立地マンション',
      type: PropertyType.APARTMENT,
      status: PropertyStatus.AVAILABLE,
      price: 25000000,
      area: 65,
      rooms: 2,
      bathrooms: 1,
      parkingSpaces: 1,
      yearBuilt: 2015,
      createdAt: '2024-01-15'
    },
    {
      id: 2,
      name: 'サンプル一戸建てB',
      address: '東京都世田谷区2-2-2',
      description: '閑静な住宅街の一戸建て',
      type: PropertyType.HOUSE,
      status: PropertyStatus.SOLD,
      price: 35000000,
      area: 120,
      rooms: 3,
      bathrooms: 2,
      parkingSpaces: 2,
      yearBuilt: 2010,
      createdAt: '2024-01-10'
    },
    {
      id: 3,
      name: 'サンプル商業施設C',
      address: '東京都新宿区3-3-3',
      description: '繁華街の商業ビル',
      type: PropertyType.COMMERCIAL,
      status: PropertyStatus.AVAILABLE,
      price: 50000000,
      area: 200,
      rooms: 5,
      bathrooms: 3,
      parkingSpaces: 5,
      yearBuilt: 2018,
      createdAt: '2024-01-05'
    }
  ]
  
  properties.value = dummyProperties
  totalCount.value = dummyProperties.length
}

// イベントハンドラー
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchQuery.value = ''
  filters.value = {
    type: '',
    status: '',
    priceRange: ''
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

const handleRowClick = (row: Property) => {
  viewProperty(row)
}

// 物件操作
const viewProperty = (property: Property) => {
  if (property.id) {
    router.push(`/properties/${property.id}`)
  }
}

const editProperty = (property: Property) => {
  if (property.id) {
    router.push(`/properties/${property.id}/edit`)
  }
}

const deleteProperty = (property: Property) => {
  propertyToDelete.value = property
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!propertyToDelete.value) return
  
  try {
    deleteLoading.value = true
    // 実際のAPIで削除
    await propertyApi.delete(propertyToDelete.value.id!)
    
    // ローカルデータから削除
    const index = properties.value.findIndex(p => p.id === propertyToDelete.value!.id)
    if (index > -1) {
      properties.value.splice(index, 1)
      totalCount.value--
    }
    
    ElMessage.success('物件を削除しました')
    deleteDialogVisible.value = false
    propertyToDelete.value = null
  } catch (error) {
    console.error('物件の削除に失敗しました:', error)
    ElMessage.error('物件の削除に失敗しました')
  } finally {
    deleteLoading.value = false
  }
}

// エクスポート
const exportData = async () => {
  try {
    exportLoading.value = true
    
    const csvContent = 'data:text/csv;charset=utf-8,' + 
      'ID,物件名,住所,タイプ,価格,面積,ステータス,登録日\n' +
      filteredProperties.value.map(p => 
        `${p.id},${p.name},${p.address},${getTypeLabel(p.type)},${p.price},${p.area},${getStatusLabel(p.status)},${p.createdAt}`
      ).join('\n')
    
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `properties_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('データをエクスポートしました')
  } catch (error) {
    console.error('エクスポートに失敗しました:', error)
    ElMessage.error('エクスポートに失敗しました')
  } finally {
    exportLoading.value = false
  }
}

// ユーティリティ関数
const getTypeLabel = (type: PropertyType) => {
  const found = propertyTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getStatusLabel = (status: PropertyStatus) => {
  const found = propertyStatuses.find(s => s.value === status)
  return found ? found.label : status
}

const getTypeTagType = (type: PropertyType) => {
  const typeMap: Record<PropertyType, string> = {
    APARTMENT: 'primary',
    HOUSE: 'success',
    COMMERCIAL: 'warning',
    LAND: 'info',
    OFFICE: 'danger',
    WAREHOUSE: ''
  }
  return typeMap[type] || ''
}

const getStatusTagType = (status: PropertyStatus) => {
  const statusMap: Record<PropertyStatus, string> = {
    AVAILABLE: 'success',
    SOLD: 'danger',
    RENTED: 'warning',
    MAINTENANCE: 'info'
  }
  return statusMap[status] || ''
}

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')}`
}

const getPropertyImage = (property: Property) => {
  // 実際の実装では、物件の画像URLを返す
  return `https://via.placeholder.com/300x200/667eea/ffffff?text=${encodeURIComponent(property.name)}`
}

const getRowClassName = ({ row }: { row: Property }) => {
  if (row.status === PropertyStatus.SOLD) return 'sold-row'
  if (row.status === PropertyStatus.MAINTENANCE) return 'maintenance-row'
  return ''
}
</script>

<style scoped>
.property-list {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.property-list.dark-mode {
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
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

.reset-btn {
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

.property-table {
  border-radius: 12px;
  overflow: hidden;
}

.property-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-text {
  font-weight: 600;
}

.type-tag {
  margin-left: 8px;
}

.price-text {
  font-weight: 600;
  color: #e74c3c;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.view-btn, .edit-btn, .delete-btn {
  border-radius: 8px;
}

.grid-view {
  padding: 20px 0;
}

.property-col {
  margin-bottom: 24px;
}

.property-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.property-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.property-card.sold {
  opacity: 0.7;
}

.property-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.image {
  width: 100%;
  height: 100%;
  transition: transform 0.4s ease;
}

.property-card:hover .image {
  transform: scale(1.1);
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 3rem;
}

.property-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.property-info {
  padding: 20px;
}

.property-title {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

.property-address {
  margin: 0 0 12px 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.property-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.price {
  font-weight: 600;
  color: #e74c3c;
  font-size: 1.1rem;
}

.area {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.property-status {
  text-align: center;
}

.property-actions {
  padding: 0 20px 20px;
  display: flex;
  gap: 12px;
}

.property-actions .el-button {
  flex: 1;
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* テーブル行のスタイル */
.sold-row {
  background-color: rgba(245, 108, 108, 0.1);
}

.maintenance-row {
  background-color: rgba(230, 162, 60, 0.1);
}

/* ローディングオーバーレイ */
.loading-overlay {
  padding: 40px;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .property-list {
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
  
  .property-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .header-left h1 {
    font-size: 1.8rem;
  }
  
  .search-input, .filter-select {
    width: 100%;
  }
}
</style>
