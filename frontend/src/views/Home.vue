<template>
  <div class="home" :class="{ 'dark-mode': isDarkMode }">
    <!-- ダークモード切り替えボタン -->
    <div class="theme-toggle">
      <el-button 
        :icon="isDarkMode ? 'Sunny' : 'Moon'" 
        circle 
        @click="toggleDarkMode"
        class="theme-btn"
        :aria-label="isDarkMode ? 'ライトモードに切り替え' : 'ダークモードに切り替え'"
      />
    </div>

    <!-- 通知バナー -->
    <el-alert
      v-if="notifications.length > 0"
      v-for="notification in notifications"
      :key="notification.id"
      :title="notification.title"
      :description="notification.description"
      :type="notification.type"
      :closable="true"
      @close="removeNotification(notification.id)"
      class="notification-banner"
      show-icon
    />

    <div class="header" v-motion="'fade-visible'">
      <div class="header-content">
        <h2>不動産管理システム</h2>
        <p>物件、クライアント、契約を一元管理</p>
        <div class="header-stats">
          <span class="header-stat">
            <el-icon><TrendCharts /></el-icon>
            総物件数: {{ stats.propertyCount }}
          </span>
          <span class="header-stat">
            <el-icon><User /></el-icon>
            総クライアント数: {{ stats.clientCount }}
          </span>
        </div>
      </div>
    </div>

    <div class="dashboard">
      <!-- ローディング状態 -->
      <div v-if="isLoading" class="loading-overlay">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 統計カード -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" v-for="(stat, index) in statCards" :key="stat.key">
          <el-card class="stat-card" v-motion="`slide-visible-delay-${index * 100}`">
            <div class="stat-content">
              <div class="stat-icon" :class="stat.iconClass">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number" :aria-label="`${stat.label}: ${stats[stat.key as keyof typeof stats]}`">
                  {{ formatStatValue(stats[stat.key as keyof typeof stats], stat.key) }}
                </div>
                <div class="stat-label">{{ stat.label }}</div>
                <div v-if="stat.trend" class="stat-trend" :class="stat.trend > 0 ? 'positive' : 'negative'">
                  <el-icon><component :is="stat.trend > 0 ? ArrowUp : ArrowDown" /></el-icon>
                  {{ Math.abs(stat.trend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- チャートセクション -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card class="chart-card" v-motion="'slide-visible-delay-400'">
            <template #header>
              <div class="card-header">
                <span>月別売上推移</span>
                <div class="chart-controls">
                  <el-select v-model="selectedYear" size="small" @change="updateRevenueChart" :loading="chartLoading">
                    <el-option
                      v-for="year in availableYears"
                      :key="year"
                      :label="year + '年'"
                      :value="year"
                    />
                  </el-select>
                  <el-button size="small" @click="refreshChartData" :loading="chartLoading">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="chartLoading" class="chart-loading">
                <el-skeleton :rows="3" animated />
              </div>
              <canvas v-else ref="revenueChartRef" width="400" height="200" :aria-label="`${selectedYear}年の月別売上推移チャート`"></canvas>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card" v-motion="'slide-visible-delay-500'">
            <template #header>
              <div class="card-header">
                <span>物件タイプ別分布</span>
                <el-button size="small" @click="refreshPropertyTypeChart" :loading="chartLoading">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="chartLoading" class="chart-loading">
                <el-skeleton :rows="3" animated />
              </div>
              <canvas v-else ref="propertyTypeChartRef" width="400" height="200" aria-label="物件タイプ別分布チャート"></canvas>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- クイックアクション -->
      <el-row :gutter="20" class="action-row">
        <el-col :span="8">
          <el-card class="action-card" v-motion="'slide-visible-delay-600'">
            <template #header>
              <div class="card-header">
                <span>物件管理</span>
                <el-button type="text" @click="$router.push('/properties')">
                  すべて表示
                </el-button>
              </div>
            </template>
            <div class="action-buttons">
              <el-button type="primary" @click="$router.push('/properties/new')" class="action-btn">
                <el-icon><Plus /></el-icon>
                新規物件登録
              </el-button>
              <el-button @click="$router.push('/properties')" class="action-btn">
                <el-icon><List /></el-icon>
                物件一覧
              </el-button>
              <el-button @click="exportProperties" class="action-btn" :loading="exportLoading">
                <el-icon><Download /></el-icon>
                データ出力
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="action-card" v-motion="'slide-visible-delay-700'">
            <template #header>
              <div class="card-header">
                <span>クライアント管理</span>
                <el-button type="text" @click="$router.push('/clients')">
                  すべて表示
                </el-button>
              </div>
            </template>
            <div class="action-buttons">
              <el-button type="primary" @click="$router.push('/clients/new')" class="action-btn">
                <el-icon><Plus /></el-icon>
                新規クライアント登録
              </el-button>
              <el-button @click="$router.push('/clients')" class="action-btn">
                <el-icon><List /></el-icon>
                クライアント一覧
              </el-button>
              <el-button @click="exportClients" class="action-btn" :loading="exportLoading">
                <el-icon><Download /></el-icon>
                データ出力
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="action-card" v-motion="'slide-visible-delay-800'">
            <template #header>
              <div class="card-header">
                <span>取引管理</span>
                <el-button type="text" @click="$router.push('/transactions')">
                  すべて表示
                </el-button>
              </div>
            </template>
            <div class="action-buttons">
              <el-button type="primary" @click="$router.push('/transactions/new')" class="action-btn">
                <el-icon><Plus /></el-icon>
                新規取引作成
              </el-button>
              <el-button @click="$router.push('/transactions')" class="action-btn">
                <el-icon><List /></el-icon>
                取引一覧
              </el-button>
              <el-button @click="exportTransactions" class="action-btn" :loading="exportLoading">
                <el-icon><Download /></el-icon>
                データ出力
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近の物件・契約 -->
      <el-row :gutter="20" class="recent-row">
        <el-col :span="12">
          <el-card class="recent-card" v-motion="'slide-visible-delay-800'">
            <template #header>
              <div class="card-header">
                <span>最近の物件</span>
                <div class="header-actions">
                  <el-input
                    v-model="propertySearch"
                    placeholder="物件を検索..."
                    size="small"
                    class="search-input"
                    clearable
                    @input="debouncePropertySearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-button type="text" @click="$router.push('/properties')">
                    すべて表示
                  </el-button>
                </div>
              </div>
            </template>
            <div class="recent-list">
              <div v-if="filteredRecentProperties.length === 0 && !propertySearchLoading" class="empty-state">
                <el-empty description="物件がありません" />
              </div>
              <div v-else-if="propertySearchLoading" class="loading-state">
                <el-skeleton :rows="3" animated />
              </div>
              <transition-group v-else name="list" tag="div">
                <div
                  v-for="(property, index) in filteredRecentProperties"
                  :key="`property-${property.id || index.toString()}`"
                  class="recent-item"
                  @click="viewProperty(property)"
                  tabindex="0"
                  @keydown.enter="viewProperty(property)"
                  @keydown.space="viewProperty(property)"
                  role="button"
                  :aria-label="`物件詳細を表示: ${property.name}`"
                >
                  <div class="item-info">
                    <div class="item-title">{{ property.name }}</div>
                    <div class="item-details">
                      <el-tag :type="getTypeTagType(property.type)" size="small">
                        {{ getTypeLabel(property.type) }}
                      </el-tag>
                      <span class="price">¥{{ formatNumber(property.price) }}</span>
                    </div>
                    <div class="item-meta">
                      <span class="address">{{ property.address }}</span>
                      <span class="area">{{ property.area }}㎡</span>
                    </div>
                  </div>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </transition-group>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="recent-card" v-motion="'slide-visible-delay-900'">
            <template #header>
              <div class="card-header">
                <span>最近の契約</span>
                <div class="header-actions">
                  <el-input
                    v-model="contractSearch"
                    placeholder="契約を検索..."
                    size="small"
                    class="search-input"
                    clearable
                    @input="debounceContractSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-button type="text" @click="$router.push('/contracts')">
                    すべて表示
                  </el-button>
                </div>
              </div>
            </template>
            <div class="recent-list">
              <div v-if="filteredRecentContracts.length === 0 && !contractSearchLoading" class="empty-state">
                <el-empty description="契約がありません" />
              </div>
              <div v-else-if="contractSearchLoading" class="loading-state">
                <el-skeleton :rows="3" animated />
              </div>
              <transition-group v-else name="list" tag="div">
                <div
                  v-for="contract in filteredRecentContracts"
                  :key="contract.id"
                  class="recent-item"
                  @click="viewContract(contract)"
                  tabindex="0"
                  @keydown.enter="viewContract(contract)"
                  @keydown.space="viewContract(contract)"
                  role="button"
                  :aria-label="`契約詳細を表示: 契約ID ${contract.id}`"
                >
                  <div class="item-info">
                    <div class="item-title">契約ID: {{ contract.id }}</div>
                    <div class="item-details">
                      <el-tag :type="getContractTypeTagType(contract.type)" size="small">
                        {{ getContractTypeLabel(contract.type) }}
                      </el-tag>
                      <span class="amount">¥{{ formatNumber(contract.amount) }}</span>
                    </div>
                    <div class="item-meta">
                      <span class="date">{{ formatDate(contract.startDate) }}</span>
                      <span class="status">{{ getContractStatusLabel(contract.status) }}</span>
                    </div>
                  </div>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </transition-group>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { propertyApi, clientApi, contractApi } from '@/services/api'
import { Property, Contract, PropertyType, PropertyStatus, ContractType, ContractStatus, Client, ClientType } from '@/types'
import { Chart } from 'chart.js'
import {
  House,
  User,
  Document,
  Money,
  TrendCharts,
  Plus,
  List,
  Download,
  Search,
  ArrowRight,
  ArrowUp,
  ArrowDown,
  Refresh,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

const router = useRouter()

// ダークモード状態
const isDarkMode = ref(false)

// 通知システム
const notifications = ref<Array<{
  id: string
  title: string
  description: string
  type: 'success' | 'warning' | 'info' | 'error'
}>>([])

// ローディング状態
const isLoading = ref(false)
const chartLoading = ref(false)
const exportLoading = ref(false)
const propertySearchLoading = ref(false)
const contractSearchLoading = ref(false)

// チャート関連
const selectedYear = ref(new Date().getFullYear())
const availableYears = ref<number[]>([])
const revenueChartRef = ref<HTMLCanvasElement>()
const propertyTypeChartRef = ref<HTMLCanvasElement>()
let revenueChart: Chart | null = null
let propertyTypeChart: Chart | null = null

// 検索フィルター
const propertySearch = ref('')
const contractSearch = ref('')

// デバウンス処理用のタイマー
let propertySearchTimer: NodeJS.Timeout | null = null
let contractSearchTimer: NodeJS.Timeout | null = null

const stats = ref({
  propertyCount: 0,
  clientCount: 0,
  contractCount: 0,
  totalRevenue: 0
})

const recentProperties = ref<Property[]>([])
const recentContracts = ref<Contract[]>([])

// 統計カードの設定
const statCards = [
  { key: 'propertyCount', label: '物件数', icon: House, iconClass: 'property', trend: 12 },
  { key: 'clientCount', label: 'クライアント数', icon: User, iconClass: 'client', trend: 8 },
  { key: 'contractCount', label: '契約数', icon: Document, iconClass: 'contract', trend: 15 },
  { key: 'totalRevenue', label: '総収益', icon: Money, iconClass: 'revenue', trend: 22 }
]

const propertyTypes = [
  { value: PropertyType.APARTMENT, label: 'マンション' },
  { value: PropertyType.HOUSE, label: '一戸建て' },
  { value: PropertyType.COMMERCIAL, label: '商業施設' },
  { value: PropertyType.LAND, label: '土地' },
  { value: PropertyType.OFFICE, label: 'オフィス' },
  { value: PropertyType.WAREHOUSE, label: '倉庫' }
]

const contractTypes = [
  { value: ContractType.SALE, label: '売買' },
  { value: ContractType.RENTAL, label: '賃貸' },
  { value: ContractType.LEASE, label: 'リース' },
  { value: ContractType.MANAGEMENT, label: '管理' }
]

// フィルタリングされたデータ
const filteredRecentProperties = computed(() => {
  if (!propertySearch.value) return recentProperties.value
  return recentProperties.value.filter((property: Property) =>
    property.name.toLowerCase().includes(propertySearch.value.toLowerCase()) ||
    getTypeLabel(property.type).includes(propertySearch.value)
  )
})

const filteredRecentContracts = computed(() => {
  if (!contractSearch.value) return recentContracts.value
  return recentContracts.value.filter((contract: Contract) =>
    (contract.id?.toString() || '').includes(contractSearch.value) ||
    getContractTypeLabel(contract.type).includes(contractSearch.value)
  )
})

onMounted(async () => {
  await loadDashboardData()
  generateAvailableYears()
  await setupCharts()
})

const loadDashboardData = async () => {
  try {
    // ダミーデータを使用（APIが利用できない場合）
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
    
    const dummyClients: Client[] = [
      { 
        id: 1, 
        firstName: '田中', 
        lastName: '太郎', 
        email: 'tanaka@example.com', 
        phone: '090-1234-5678', 
        address: '東京都渋谷区4-4-4',
        type: ClientType.BUYER,
        createdAt: '2024-01-15'
      },
      { 
        id: 2, 
        firstName: '佐藤', 
        lastName: '花子', 
        email: 'sato@example.com', 
        phone: '090-8765-4321', 
        address: '東京都世田谷区5-5-5',
        type: ClientType.SELLER,
        createdAt: '2024-01-10'
      }
    ]
    
    const dummyContracts: Contract[] = [
      { 
        id: 1, 
        propertyId: 1,
        clientId: 1,
        type: ContractType.SALE, 
        amount: 25000000, 
        status: ContractStatus.ACTIVE, 
        startDate: '2024-01-15',
        endDate: '2024-12-31',
        terms: '現金一括払い',
        createdAt: '2024-01-15' 
      },
      { 
        id: 2, 
        propertyId: 2,
        clientId: 2,
        type: ContractType.RENTAL, 
        amount: 800000, 
        status: ContractStatus.ACTIVE, 
        startDate: '2024-01-10',
        endDate: '2024-12-31',
        terms: '月額賃料',
        createdAt: '2024-01-10' 
      },
      { 
        id: 3, 
        propertyId: 3,
        clientId: 1,
        type: ContractType.SALE, 
        amount: 35000000, 
        status: ContractStatus.ACTIVE, 
        startDate: '2024-01-05',
        endDate: '2024-12-31',
        terms: 'ローン利用可',
        createdAt: '2024-01-05' 
      }
    ]
    
    stats.value = {
      propertyCount: dummyProperties.length,
      clientCount: dummyClients.length,
      contractCount: dummyContracts.length,
      totalRevenue: dummyContracts.reduce((sum, contract) => sum + contract.amount, 0)
    }
    
    // 最近の物件（最新5件）
    recentProperties.value = dummyProperties
      .sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(b.createdAt || '').getTime())
      .slice(0, 5)
    
    // 最近の契約（最新5件）
    recentContracts.value = dummyContracts
      .sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(b.createdAt || '').getTime())
      .slice(0, 5)
      
  } catch (error) {
    console.error('ダッシュボードデータの取得に失敗しました:', error)
  }
}

const generateAvailableYears = () => {
  const currentYear = new Date().getFullYear()
  availableYears.value = Array.from({ length: 5 }, (_, i) => currentYear - i)
}

const setupCharts = async () => {
  await nextTick()
  
  if (revenueChartRef.value && propertyTypeChartRef.value) {
    // 売上チャートの初期化
    const revenueCtx = revenueChartRef.value.getContext('2d')
    if (revenueCtx) {
      revenueChart = new Chart(revenueCtx, {
        type: 'line',
        data: {
          labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
          datasets: [{
            label: '売上',
            data: [12000000, 15000000, 18000000, 14000000, 20000000, 22000000, 25000000, 28000000, 30000000, 32000000, 35000000, 38000000],
            borderColor: isDarkMode.value ? '#67C23A' : '#409EFF',
            backgroundColor: isDarkMode.value ? 'rgba(103, 194, 58, 0.1)' : 'rgba(64, 158, 255, 0.1)',
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          }
        } as any
      })
    }

    // 物件タイプ別分布チャートの初期化
    const propertyTypeCtx = propertyTypeChartRef.value.getContext('2d')
    if (propertyTypeCtx) {
      propertyTypeChart = new Chart(propertyTypeCtx, {
        type: 'doughnut',
        data: {
          labels: ['マンション', '一戸建て', '商業施設', '土地', 'オフィス', '倉庫'],
          datasets: [{
            data: [30, 25, 15, 20, 8, 2],
            backgroundColor: [
              '#409EFF',
              '#67C23A',
              '#E6A23C',
              '#909399',
              '#F56C6C',
              '#9C27B0'
            ]
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom'
            }
          }
        }
      })
    }
  }
}

const updateRevenueChart = async () => {
  try {
    chartLoading.value = true
    // 実際のAPIからデータを取得
    const response = await contractApi.getAll()
    const contracts = response.data || []
    
    // 選択された年の月別データを集計
    const monthlyData = new Array(12).fill(0)
    contracts.forEach(contract => {
      const contractDate = new Date(contract.startDate)
      if (contractDate.getFullYear() === selectedYear.value) {
        const month = contractDate.getMonth()
        monthlyData[month] += contract.amount
      }
    })
    
    // チャートを更新
    if (revenueChart && revenueChart.data && revenueChart.data.datasets && revenueChart.data.datasets[0]) {
      revenueChart.data.datasets[0].data = monthlyData
      revenueChart.update()
    }
    
    showSuccess(`${selectedYear.value}年のデータを更新しました`)
  } catch (error) {
    handleError(error, 'チャートデータの更新に失敗しました')
  } finally {
    chartLoading.value = false
  }
}

const updateChartData = () => {
  updateRevenueChart()
}

const refreshChartData = () => {
  updateRevenueChart()
}

const refreshPropertyTypeChart = () => {
  // 物件タイプ別分布チャートのデータを更新
  if (propertyTypeChart && propertyTypeChart.data && propertyTypeChart.data.datasets && propertyTypeChart.data.datasets[0]) {
    propertyTypeChart.data.datasets[0].data = [
      Math.floor(Math.random() * 10) + 5, // マンション
      Math.floor(Math.random() * 10) + 5, // 一戸建て
      Math.floor(Math.random() * 10) + 5, // 商業施設
      Math.floor(Math.random() * 10) + 5, // 土地
      Math.floor(Math.random() * 10) + 5, // オフィス
      Math.floor(Math.random() * 10) + 5  // 倉庫
    ]
    propertyTypeChart.update()
    showSuccess('物件タイプ別分布チャートを更新しました')
  }
}

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  document.body.classList.toggle('dark-mode', isDarkMode.value)
  
  // チャートの色を更新
  if (revenueChart && revenueChart.data && revenueChart.data.datasets && revenueChart.data.datasets[0]) {
    const dataset = revenueChart.data.datasets[0]
    if (dataset) {
      dataset.borderColor = isDarkMode.value ? '#67C23A' : '#409EFF'
      dataset.backgroundColor = isDarkMode.value ? 'rgba(103, 194, 58, 0.1)' : 'rgba(64, 158, 255, 0.1)'
      revenueChart.update()
    }
  }
}

const viewProperty = (property: Property) => {
  if (property.id) {
    router.push(`/properties/${property.id}`)
  }
}

const viewContract = (contract: Contract) => {
  // 契約詳細表示の実装
  console.log('View contract:', contract)
}

const getTypeLabel = (type: PropertyType) => {
  const found = propertyTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getContractTypeLabel = (type: ContractType) => {
  const found = contractTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getContractStatusLabel = (status: ContractStatus) => {
  const found = Object.values(ContractStatus).find(s => s === status)
  return found || status
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月${date.getDate().toString().padStart(2, '0')}日`
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

const getContractTypeTagType = (type: ContractType) => {
  const typeMap: Record<ContractType, string> = {
    SALE: 'success',
    RENTAL: 'primary',
    LEASE: 'warning',
    MANAGEMENT: 'info'
  }
  return typeMap[type] || ''
}

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatStatValue = (value: any, key: string) => {
  if (key === 'totalRevenue') {
    return `¥${formatNumber(value)}`
  }
  return value
}

// 通知システムのメソッド
const addNotification = (title: string, description: string, type: 'success' | 'warning' | 'info' | 'error' = 'info') => {
  const id = Date.now().toString()
  notifications.value.push({ id, title, description, type })
  
  // 5秒後に自動削除
  setTimeout(() => {
    removeNotification(id)
  }, 5000)
}

const removeNotification = (id: string) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
  }
}

// エラーハンドリング
const handleError = (error: any, message: string = 'エラーが発生しました') => {
  console.error(message, error)
  addNotification('エラー', message, 'error')
}

// 成功通知
const showSuccess = (message: string) => {
  addNotification('成功', message, 'success')
}

// エクスポート機能
const exportProperties = async () => {
  try {
    exportLoading.value = true
    // CSVエクスポートの実装
    const csvContent = 'data:text/csv;charset=utf-8,' + 
      'ID,名前,住所,タイプ,価格,面積\n' +
      recentProperties.value.map(p => 
        `${p.id},${p.name},${p.address},${getTypeLabel(p.type)},${p.price},${p.area}`
      ).join('\n')
    
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `properties_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    showSuccess('物件データをエクスポートしました')
  } catch (error) {
    handleError(error, 'エクスポートに失敗しました')
  } finally {
    exportLoading.value = false
  }
}

const exportClients = async () => {
  try {
    exportLoading.value = true
    // CSVエクスポートの実装
    const csvContent = 'data:text/csv;charset=utf-8,' + 
      'ID,姓,名,メール,電話,住所,タイプ\n' +
      recentContracts.value.map(c => 
        `${c.id},${c.clientId},${getContractTypeLabel(c.type)},${c.amount},${c.startDate}`
      ).join('\n')
    
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `clients_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    showSuccess('クライアントデータをエクスポートしました')
  } catch (error) {
    handleError(error, 'エクスポートに失敗しました')
  } finally {
    exportLoading.value = false
  }
}

const exportTransactions = async () => {
  try {
    exportLoading.value = true
    // CSVエクスポートの実装
    const csvContent = 'data:text/csv;charset=utf-8,' + 
      'ID,物件ID,クライアントID,タイプ,金額,開始日,ステータス\n' +
      recentContracts.value.map(c => 
        `${c.id},${c.propertyId},${c.clientId},${getContractTypeLabel(c.type)},${c.amount},${c.startDate},${getContractStatusLabel(c.status)}`
      ).join('\n')
    
    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `transactions_${new Date().toISOString().split('T')[0]}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    showSuccess('取引データをエクスポートしました')
  } catch (error) {
    handleError(error, 'エクスポートに失敗しました')
  } finally {
    exportLoading.value = false
  }
}

// デバウンス検索
const debouncePropertySearch = () => {
  if (propertySearchTimer) {
    clearTimeout(propertySearchTimer)
  }
  propertySearchTimer = setTimeout(() => {
    propertySearchLoading.value = true
    // 実際の検索処理をここに実装
    setTimeout(() => {
      propertySearchLoading.value = false
    }, 500)
  }, 300)
}

const debounceContractSearch = () => {
  if (contractSearchTimer) {
    clearTimeout(contractSearchTimer)
  }
  contractSearchTimer = setTimeout(() => {
    contractSearchLoading.value = true
    // 実際の検索処理をここに実装
    setTimeout(() => {
      contractSearchLoading.value = false
    }, 500)
  }, 300)
}
</script>

<style scoped>
.home {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.home.dark-mode {
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
}

.theme-toggle {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 1000;
}

.theme-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 50px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.theme-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.4);
}

.header {
  text-align: center;
  margin-bottom: 24px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 16px;
  color: white;
  margin-top: 50px;
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.header h2 {
  font-size: 1.6rem;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  font-weight: 800;
  letter-spacing: -0.02em;
}

.header p {
  font-size: 0.85rem;
  margin: 0 0 8px 0;
  opacity: 0.95;
  font-weight: 300;
}

.header-stats {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 8px;
}

.header-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  height: 110px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
}

.dark-mode .stat-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.stat-icon {
  width: 55px;
  height: 55px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18px;
  font-size: 22px;
  color: white;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.stat-icon.property { 
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 50%, #2d8cf0 100%);
}
.stat-icon.client { 
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 50%, #95d475 100%);
}
.stat-icon.contract { 
  background: linear-gradient(135deg, #e6a23c 0%, #f0ad4e 50%, #f7ba2a 100%);
}
.stat-icon.revenue { 
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 50%, #ff9a9a 100%);
}

.stat-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 800;
  color: #2c3e50;
  margin-bottom: 6px;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dark-mode .stat-number {
  background: linear-gradient(135deg, #ffffff, #e0e0e0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dark-mode .stat-label {
  color: #bdc3c7;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  height: 320px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .chart-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart-container {
  height: 270px;
  position: relative;
  padding: 16px;
}

.chart-container canvas {
  width: 100% !important;
  height: 100% !important;
}

.action-row {
  margin-bottom: 24px;
}

.action-card {
  height: 140px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .action-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.action-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 5px 16px rgba(0, 0, 0, 0.15);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  justify-content: center;
  padding: 0 20px 18px;
}

.action-btn {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
  font-weight: 600;
  padding: 8px 16px;
}

.action-btn:hover {
  transform: translateX(6px) scale(1.03);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.recent-row {
  margin-bottom: 24px;
}

.recent-card {
  height: 360px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .recent-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.recent-list {
  height: 280px;
  overflow-y: auto;
  padding: 0 20px;
}

.recent-list::-webkit-scrollbar {
  width: 5px;
}

.recent-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.recent-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 3px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(235, 238, 245, 0.6);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
  margin-bottom: 6px;
}

.dark-mode .recent-item {
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.recent-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  transform: translateX(6px);
  box-shadow: 0 3px 16px rgba(0, 0, 0, 0.1);
}

.dark-mode .recent-item:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
}

.recent-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
  font-size: 1rem;
}

.dark-mode .item-title {
  color: #ffffff;
}

.item-details {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.85rem;
}

.price, .amount {
  color: #e74c3c;
  font-weight: 600;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.empty-state {
  text-align: center;
  color: #95a5a6;
  padding: 40px 0;
  font-size: 1rem;
}

.dark-mode .empty-state {
  color: #bdc3c7;
}

.el-icon {
  color: #95a5a6;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 1.1rem;
}

.recent-item:hover .el-icon {
  color: #667eea;
  transform: translateX(5px) scale(1.1);
}

/* カスタムスクロールバー */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a6fd8, #6a4c9a);
}

/* アニメーション */
.list-enter-active,
.list-leave-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-enter-from {
  opacity: 0;
  transform: translateX(40px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-40px);
}

/* パルスアニメーション */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.stat-card:hover .stat-icon {
  animation: pulse 2s infinite;
}

/* 通知バナー */
.notification-banner {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* ローディングオーバーレイ */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
}

.dark-mode .loading-overlay {
  background: rgba(0, 0, 0, 0.8);
}

/* 統計トレンド */
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-top: 4px;
}

.stat-trend.positive {
  color: #67c23a;
}

.stat-trend.negative {
  color: #f56c6c;
}

/* チャートローディング */
.chart-loading {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .home {
    padding: 12px;
  }
  
  .header {
    margin-top: 80px;
    padding: 12px 16px;
    margin-bottom: 20px;
  }
  
  .header h2 {
    font-size: 1.5rem;
  }
  
  .header p {
    font-size: 0.8rem;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-row {
    margin-bottom: 16px;
  }
  
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .chart-row {
    margin-bottom: 16px;
  }
  
  .chart-row .el-col {
    margin-bottom: 20px;
  }
  
  .action-row {
    margin-bottom: 16px;
  }
  
  .action-row .el-col {
    margin-bottom: 20px;
  }
  
  .recent-row {
    margin-bottom: 16px;
  }
  
  .recent-row .el-col {
    margin-bottom: 20px;
  }
  
  .search-input {
    width: 160px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
    margin-right: 16px;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .chart-card {
    height: 280px;
  }
  
  .chart-container {
    height: 230px;
  }
  
  .action-card {
    height: 120px;
  }
  
  .recent-card {
    height: 320px;
  }
  
  .recent-list {
    height: 240px;
  }
}

@media (max-width: 480px) {
  .home {
    padding: 8px;
  }
  
  .header {
    margin-top: 70px;
    padding: 10px 12px;
  }
  
  .header h2 {
    font-size: 1.3rem;
  }
  
  .header p {
    font-size: 0.75rem;
  }
  
  .stat-card {
    height: 90px;
  }
  
  .stat-icon {
    width: 45px;
    height: 45px;
    font-size: 18px;
    margin-right: 14px;
  }
  
  .stat-number {
    font-size: 1.3rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
  
  .chart-card {
    height: 250px;
  }
  
  .chart-container {
    height: 200px;
  }
  
  .action-card {
    height: 110px;
  }
  
  .recent-card {
    height: 280px;
  }
  
  .recent-list {
    height: 200px;
  }
  
  .search-input {
    width: 120px;
  }
  
  .action-btn {
    padding: 6px 12px;
    font-size: 0.9rem;
  }
  
  .card-header {
    padding: 14px 16px 10px;
  }
  
  .action-buttons {
    padding: 0 16px 14px;
    gap: 10px;
  }
  
  .recent-list {
    padding: 0 16px;
  }
  
  .recent-item {
    padding: 12px 0;
  }
  
  .item-title {
    font-size: 0.9rem;
  }
  
  .item-details {
    font-size: 0.8rem;
  }
}
</style>
