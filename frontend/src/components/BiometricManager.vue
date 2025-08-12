<template>
  <div class="biometric-manager">
    <div class="manager-header">
      <h3><el-icon><Fingerprint /></el-icon> 生体認証管理</h3>
      <div class="header-actions">
        <el-button type="success" @click="showFingerprintDialog = true">
          <el-icon><Plus /></el-icon>
          指紋登録
        </el-button>
        <el-button type="primary" @click="showPalmPrintDialog = true">
          <el-icon><Plus /></el-icon>
          掌紋登録
        </el-button>
      </div>
    </div>

    <!-- 検索・フィルター -->
    <div class="search-filters">
      <el-input
        v-model="searchQuery"
        placeholder="顧客名、デバイス、メモで検索"
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select
        v-model="filterType"
        placeholder="認証タイプ"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="type in biometricTypes"
          :key="type.value"
          :label="type.label"
          :value="type.value"
        />
      </el-select>
      
      <el-select
        v-model="filterQuality"
        placeholder="品質レベル"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="quality in qualityLevels"
          :key="quality.value"
          :label="quality.label"
          :value="quality.value"
        />
      </el-select>
      
      <el-button type="info" @click="showSearchDialog = true">
        <el-icon><Search /></el-icon>
        詳細検索
      </el-button>
    </div>

    <!-- 統計情報 -->
    <div class="statistics-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon fingerprint">
            <el-icon><Fingerprint /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.fingerprints }}</div>
            <div class="stat-label">指紋データ</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon palmprint">
            <el-icon><Hand /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.palmPrints }}</div>
            <div class="stat-label">掌紋データ</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon verified">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.verifiedRecords }}</div>
            <div class="stat-label">検証済み</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon quality">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.averageQuality }}</div>
            <div class="stat-label">平均品質</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 生体認証データ一覧 -->
    <div class="biometric-list">
      <div v-if="filteredBiometrics.length === 0" class="no-data">
        <el-empty description="生体認証データが見つかりません" />
      </div>
      
      <div v-else class="biometrics-grid">
        <div
          v-for="biometric in filteredBiometrics"
          :key="biometric.id"
          class="biometric-card"
        >
          <div class="card-header">
            <div class="biometric-type">
              <el-icon v-if="biometric.type === 'FINGERPRINT'"><Fingerprint /></el-icon>
              <el-icon v-else><Hand /></el-icon>
              <span>{{ getBiometricTypeLabel(biometric.type) }}</span>
            </div>
            <div class="quality-badge">
              <el-tag :type="getQualityType(biometric.quality)" size="small">
                {{ biometric.quality }}%
              </el-tag>
            </div>
          </div>
          
          <div class="card-body">
            <div class="client-info">
              <h4>{{ biometric.clientName }}</h4>
              <p class="client-id">ID: {{ biometric.clientId }}</p>
            </div>
            
            <div class="biometric-details">
              <div class="detail-item">
                <span class="label">タイプ:</span>
                <span class="value">
                  {{ getFingerprintTypeLabel(biometric.fingerprintType) || getPalmPrintTypeLabel(biometric.palmPrintType) }}
                </span>
              </div>
              <div class="detail-item">
                <span class="label">デバイス:</span>
                <span class="value">{{ biometric.captureDevice }}</span>
              </div>
              <div class="detail-item">
                <span class="label">取得日:</span>
                <span class="value">{{ formatDate(biometric.captureDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">解像度:</span>
                <span class="value">{{ biometric.metadata.resolution }}</span>
              </div>
            </div>
            
            <div class="verification-status">
              <el-tag
                :type="biometric.isVerified ? 'success' : 'warning'"
                size="small"
              >
                {{ biometric.isVerified ? '検証済み' : '未検証' }}
              </el-tag>
              <span v-if="biometric.verificationDate" class="verification-date">
                {{ formatDate(biometric.verificationDate) }}
              </span>
            </div>
          </div>
          
          <div class="card-actions">
            <el-button
              size="small"
              @click="viewBiometric(biometric)"
              type="primary"
            >
              <el-icon><View /></el-icon>
              表示
            </el-button>
            <el-button
              size="small"
              @click="verifyBiometric(biometric.id)"
              :type="biometric.isVerified ? 'success' : 'warning'"
              :disabled="biometric.isVerified"
            >
              <el-icon><Check /></el-icon>
              {{ biometric.isVerified ? '検証済み' : '検証' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteBiometric(biometric.id)"
            >
              <el-icon><Delete /></el-icon>
              削除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 指紋登録ダイアログ -->
    <el-dialog
      v-model="showFingerprintDialog"
      title="指紋登録"
      width="600px"
    >
      <div class="registration-form">
        <el-form :model="fingerprintForm" label-width="120px">
          <el-form-item label="顧客ID">
            <el-input v-model="fingerprintForm.clientId" placeholder="顧客IDを入力" />
          </el-form-item>
          
          <el-form-item label="顧客名">
            <el-input v-model="fingerprintForm.clientName" placeholder="顧客名を入力" />
          </el-form-item>
          
          <el-form-item label="指紋タイプ">
            <el-select v-model="fingerprintForm.fingerprintType" placeholder="指紋タイプを選択">
              <el-option
                v-for="type in fingerprintTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="取得デバイス">
            <el-input v-model="fingerprintForm.captureDevice" placeholder="取得デバイスを入力" />
          </el-form-item>
          
          <el-form-item label="メモ">
            <el-input
              v-model="fingerprintForm.notes"
              type="textarea"
              :rows="3"
              placeholder="メモを入力"
            />
          </el-form-item>
          
          <el-form-item label="指紋画像">
            <el-upload
              ref="fingerprintUploadRef"
              :auto-upload="false"
              :on-change="handleFingerprintFileChange"
              :file-list="fingerprintFileList"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary">画像を選択</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  JPEG、PNG、GIF、BMP形式の画像をアップロードできます（最大10MB）
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showFingerprintDialog = false">キャンセル</el-button>
        <el-button
          type="primary"
          @click="registerFingerprint"
          :loading="registering"
          :disabled="!selectedFingerprintFile"
        >
          登録
        </el-button>
      </template>
    </el-dialog>

    <!-- 掌紋登録ダイアログ -->
    <el-dialog
      v-model="showPalmPrintDialog"
      title="掌紋登録"
      width="600px"
    >
      <div class="registration-form">
        <el-form :model="palmPrintForm" label-width="120px">
          <el-form-item label="顧客ID">
            <el-input v-model="palmPrintForm.clientId" placeholder="顧客IDを入力" />
          </el-form-item>
          
          <el-form-item label="顧客名">
            <el-input v-model="palmPrintForm.clientName" placeholder="顧客名を入力" />
          </el-form-item>
          
          <el-form-item label="掌紋タイプ">
            <el-select v-model="palmPrintForm.palmPrintType" placeholder="掌紋タイプを選択">
              <el-option
                v-for="type in palmPrintTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="取得デバイス">
            <el-input v-model="palmPrintForm.captureDevice" placeholder="取得デバイスを入力" />
          </el-form-item>
          
          <el-form-item label="メモ">
            <el-input
              v-model="palmPrintForm.notes"
              type="textarea"
              :rows="3"
              placeholder="メモを入力"
            />
          </el-form-item>
          
          <el-form-item label="掌紋画像">
            <el-upload
              ref="palmPrintUploadRef"
              :auto-upload="false"
              :on-change="handlePalmPrintFileChange"
              :file-list="palmPrintFileList"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary">画像を選択</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  JPEG、PNG、GIF、BMP形式の画像をアップロードできます（最大10MB）
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showPalmPrintDialog = false">キャンセル</el-button>
        <el-button
          type="primary"
          @click="registerPalmPrint"
          :loading="registering"
          :disabled="!selectedPalmPrintFile"
        >
          登録
        </el-button>
      </template>
    </el-dialog>

    <!-- 詳細検索ダイアログ -->
    <el-dialog
      v-model="showSearchDialog"
      title="詳細検索"
      width="500px"
    >
      <div class="advanced-search-form">
        <el-form :model="advancedSearchForm" label-width="120px">
          <el-form-item label="日付範囲">
            <el-date-picker
              v-model="advancedSearchForm.dateRange"
              type="daterange"
              range-separator="～"
              start-placeholder="開始日"
              end-placeholder="終了日"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="品質範囲">
            <el-slider
              v-model="advancedSearchForm.qualityRange"
              range
              :min="0"
              :max="100"
              :marks="{ 0: '0%', 50: '50%', 100: '100%' }"
            />
          </el-form-item>
          
          <el-form-item label="検証状態">
            <el-radio-group v-model="advancedSearchForm.verificationStatus">
              <el-radio label="">全て</el-radio>
              <el-radio label="verified">検証済み</el-radio>
              <el-radio label="unverified">未検証</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showSearchDialog = false">キャンセル</el-button>
        <el-button type="primary" @click="performAdvancedSearch">検索実行</el-button>
      </template>
    </el-dialog>

    <!-- 生体認証データ表示ダイアログ -->
    <el-dialog
      v-model="showViewDialog"
      title="生体認証データ詳細"
      width="700px"
    >
      <div v-if="viewingBiometric" class="biometric-viewer">
        <div class="viewer-header">
          <h4>{{ viewingBiometric.clientName }}</h4>
          <el-tag :type="getQualityType(viewingBiometric.quality)">
            {{ viewingBiometric.quality }}%
          </el-tag>
        </div>
        
        <div class="viewer-content">
          <div class="image-section">
            <h5>生体認証画像</h5>
            <div class="image-container">
              <img
                v-if="viewingBiometric.imageData"
                :src="viewingBiometric.imageData"
                alt="生体認証画像"
                class="biometric-image"
              />
              <div v-else class="no-image">
                画像データがありません
              </div>
            </div>
          </div>
          
          <div class="details-section">
            <h5>詳細情報</h5>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="ID">
                {{ viewingBiometric.id }}
              </el-descriptions-item>
              <el-descriptions-item label="タイプ">
                {{ getBiometricTypeLabel(viewingBiometric.type) }}
              </el-descriptions-item>
              <el-descriptions-item label="顧客ID">
                {{ viewingBiometric.clientId }}
              </el-descriptions-item>
              <el-descriptions-item label="取得日">
                {{ formatDateTime(viewingBiometric.captureDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="デバイス">
                {{ viewingBiometric.captureDevice }}
              </el-descriptions-item>
              <el-descriptions-item label="品質">
                {{ viewingBiometric.quality }}%
              </el-descriptions-item>
              <el-descriptions-item label="検証状態">
                <el-tag :type="viewingBiometric.isVerified ? 'success' : 'warning'">
                  {{ viewingBiometric.isVerified ? '検証済み' : '未検証' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="解像度">
                {{ viewingBiometric.metadata.resolution }}
              </el-descriptions-item>
              <el-descriptions-item label="アルゴリズム">
                {{ viewingBiometric.metadata.algorithm }}
              </el-descriptions-item>
              <el-descriptions-item label="バージョン">
                {{ viewingBiometric.metadata.version }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Fingerprint,
  Hand,
  Plus,
  Search,
  View,
  Check,
  Delete,
  Star
} from '@element-plus/icons-vue'
import {
  globalBiometricManager,
  BiometricType,
  FingerprintType,
  PalmPrintType,
  type BiometricData,
  SUPPORTED_IMAGE_TYPES,
  MAX_IMAGE_SIZE_MB
} from '@/utils/biometricManager'

// 状態管理
const searchQuery = ref('')
const filterType = ref('')
const filterQuality = ref('')
const showFingerprintDialog = ref(false)
const showPalmPrintDialog = ref(false)
const showSearchDialog = ref(false)
const showViewDialog = ref(false)
const registering = ref(false)
const selectedFingerprintFile = ref<File | null>(null)
const selectedPalmPrintFile = ref<File | null>(null)
const fingerprintFileList = ref<any[]>([])
const palmPrintFileList = ref<any[]>([])
const viewingBiometric = ref<BiometricData | null>(null)

// フォーム
const fingerprintForm = ref({
  clientId: '',
  clientName: '',
  fingerprintType: FingerprintType.THUMB,
  captureDevice: '',
  notes: ''
})

const palmPrintForm = ref({
  clientId: '',
  clientName: '',
  palmPrintType: PalmPrintType.RIGHT_HAND,
  captureDevice: '',
  notes: ''
})

const advancedSearchForm = ref({
  dateRange: [],
  qualityRange: [0, 100],
  verificationStatus: ''
})

// オプション
const biometricTypes = [
  { value: BiometricType.FINGERPRINT, label: '指紋' },
  { value: BiometricType.PALM_PRINT, label: '掌紋' }
]

const fingerprintTypes = [
  { value: FingerprintType.THUMB, label: '親指' },
  { value: FingerprintType.INDEX, label: '人差し指' },
  { value: FingerprintType.MIDDLE, label: '中指' },
  { value: FingerprintType.RING, label: '薬指' },
  { value: FingerprintType.LITTLE, label: '小指' }
]

const palmPrintTypes = [
  { value: PalmPrintType.RIGHT_HAND, label: '右手' },
  { value: PalmPrintType.LEFT_HAND, label: '左手' }
]

const qualityLevels = [
  { value: 'excellent', label: '優秀 (90-100%)' },
  { value: 'good', label: '良好 (80-89%)' },
  { value: 'fair', label: '普通 (70-79%)' },
  { value: 'poor', label: '不良 (0-69%)' }
]

// フィルタリングされたデータ
const filteredBiometrics = computed(() => {
  return globalBiometricManager.searchBiometrics(searchQuery.value, {
    type: filterType.value || undefined,
    quality: filterQuality.value ? getQualityRange(filterQuality.value) : undefined
  })
})

// 統計情報
const statistics = computed(() => {
  return globalBiometricManager.getStatistics()
})

// 検索処理
const handleSearch = () => {
  // 検索はcomputedで自動的に実行される
}

// 詳細検索の実行
const performAdvancedSearch = () => {
  const filters: any = {}
  
  if (advancedSearchForm.value.dateRange && advancedSearchForm.value.dateRange.length === 2) {
    filters.dateRange = {
      start: new Date(advancedSearchForm.value.dateRange[0]),
      end: new Date(advancedSearchForm.value.dateRange[1])
    }
  }
  
  if (advancedSearchForm.value.qualityRange) {
    filters.quality = {
      min: advancedSearchForm.value.qualityRange[0],
      max: advancedSearchForm.value.qualityRange[1]
    }
  }
  
  // 詳細検索の実装
  showSearchDialog.value = false
}

// ファイル選択処理
const handleFingerprintFileChange = (file: any) => {
  selectedFingerprintFile.value = file.raw
  validateImageFile(file.raw)
}

const handlePalmPrintFileChange = (file: any) => {
  selectedPalmPrintFile.value = file.raw
  validateImageFile(file.raw)
}

// 画像ファイルの検証
const validateImageFile = (file: File) => {
  if (!SUPPORTED_IMAGE_TYPES.includes(file.type)) {
    ElMessage.error('サポートされていない画像形式です')
    return false
  }
  
  if (file.size > MAX_IMAGE_SIZE_MB * 1024 * 1024) {
    ElMessage.error(`画像サイズは${MAX_IMAGE_SIZE_MB}MB以下にしてください`)
    return false
  }
  
  return true
}

// 指紋登録
const registerFingerprint = async () => {
  if (!selectedFingerprintFile.value) {
    ElMessage.warning('画像を選択してください')
    return
  }

  try {
    registering.value = true
    
    const biometric = await globalBiometricManager.registerFingerprint(
      fingerprintForm.value.clientId,
      fingerprintForm.value.clientName,
      fingerprintForm.value.fingerprintType,
      selectedFingerprintFile.value,
      fingerprintForm.value.captureDevice
    )

    if (fingerprintForm.value.notes) {
      globalBiometricManager.updateBiometricData(biometric.id, {
        notes: fingerprintForm.value.notes
      })
    }

    ElMessage.success('指紋の登録が完了しました')
    
    // フォームをリセット
    fingerprintForm.value = {
      clientId: '',
      clientName: '',
      fingerprintType: FingerprintType.THUMB,
      captureDevice: '',
      notes: ''
    }
    selectedFingerprintFile.value = null
    fingerprintFileList.value = []
    showFingerprintDialog.value = false
    
  } catch (error) {
    console.error('指紋登録に失敗しました:', error)
    ElMessage.error('指紋登録に失敗しました')
  } finally {
    registering.value = false
  }
}

// 掌紋登録
const registerPalmPrint = async () => {
  if (!selectedPalmPrintFile.value) {
    ElMessage.warning('画像を選択してください')
    return
  }

  try {
    registering.value = true
    
    const biometric = await globalBiometricManager.registerPalmPrint(
      palmPrintForm.value.clientId,
      palmPrintForm.value.clientName,
      palmPrintForm.value.palmPrintType,
      selectedPalmPrintFile.value,
      palmPrintForm.value.captureDevice
    )

    if (palmPrintForm.value.notes) {
      globalBiometricManager.updateBiometricData(biometric.id, {
        notes: palmPrintForm.value.notes
      })
    }

    ElMessage.success('掌紋の登録が完了しました')
    
    // フォームをリセット
    palmPrintForm.value = {
      clientId: '',
      clientName: '',
      palmPrintType: PalmPrintType.RIGHT_HAND,
      captureDevice: '',
      notes: ''
    }
    selectedPalmPrintFile.value = null
    palmPrintFileList.value = []
    showPalmPrintDialog.value = false
    
  } catch (error) {
    console.error('掌紋登録に失敗しました:', error)
    ElMessage.error('掌紋登録に失敗しました')
  } finally {
    registering.value = false
  }
}

// 生体認証データの表示
const viewBiometric = (biometric: BiometricData) => {
  viewingBiometric.value = biometric
  showViewDialog.value = true
}

// 生体認証データの検証
const verifyBiometric = async (id: string) => {
  try {
    const verified = globalBiometricManager.verifyBiometricData(id)
    if (verified) {
      ElMessage.success('生体認証データの検証が完了しました')
    } else {
      ElMessage.error('生体認証データの検証に失敗しました')
    }
  } catch (error) {
    console.error('検証に失敗しました:', error)
    ElMessage.error('検証に失敗しました')
  }
}

// 生体認証データの削除
const deleteBiometric = async (id: string) => {
  try {
    await ElMessageBox.confirm(
      'この生体認証データを削除してもよろしいですか？',
      '削除の確認',
      {
        confirmButtonText: '削除',
        cancelButtonText: 'キャンセル',
        type: 'warning'
      }
    )

    const deleted = globalBiometricManager.deleteBiometricData(id)
    if (deleted) {
      ElMessage.success('生体認証データを削除しました')
    } else {
      ElMessage.error('生体認証データの削除に失敗しました')
    }
  } catch (error) {
    // ユーザーがキャンセルした場合
  }
}

// ユーティリティ関数
const getBiometricTypeLabel = (type: BiometricType): string => {
  const typeOption = biometricTypes.find(t => t.value === type)
  return typeOption ? typeOption.label : type
}

const getFingerprintTypeLabel = (type: FingerprintType): string => {
  const typeOption = fingerprintTypes.find(t => t.value === type)
  return typeOption ? typeOption.label : type
}

const getPalmPrintTypeLabel = (type: PalmPrintType): string => {
  const typeOption = palmPrintTypes.find(t => t.value === type)
  return typeOption ? typeOption.label : type
}

const getQualityType = (quality: number): string => {
  if (quality >= 90) return 'success'
  if (quality >= 80) return 'warning'
  if (quality >= 70) return 'info'
  return 'danger'
}

const getQualityRange = (qualityLevel: string) => {
  switch (qualityLevel) {
    case 'excellent': return { min: 90, max: 100 }
    case 'good': return { min: 80, max: 89 }
    case 'fair': return { min: 70, max: 79 }
    case 'poor': return { min: 0, max: 69 }
    default: return undefined
  }
}

const formatDate = (date: Date): string => {
  return new Date(date).toLocaleDateString('ja-JP')
}

const formatDateTime = (date: Date): string => {
  return new Date(date).toLocaleString('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初期化
onMounted(() => {
  // 初期データの読み込み
})
</script>

<style scoped>
.biometric-manager {
  padding: 20px;
}

.manager-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.manager-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 300px;
}

.filter-select {
  min-width: 150px;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-icon.fingerprint {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.palmprint {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.verified {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.quality {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.biometric-list {
  margin-bottom: 30px;
}

.no-data {
  text-align: center;
  padding: 40px;
}

.biometrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.biometric-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.biometric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.biometric-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.client-info h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 16px;
}

.client-id {
  margin: 0;
  font-size: 12px;
  color: #7f8c8d;
}

.biometric-details {
  margin: 15px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-item .label {
  color: #7f8c8d;
  font-weight: 500;
}

.detail-item .value {
  color: #2c3e50;
  font-weight: 600;
}

.verification-status {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.verification-date {
  font-size: 12px;
  color: #7f8c8d;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.registration-form,
.advanced-search-form {
  max-height: 60vh;
  overflow-y: auto;
}

.biometric-viewer {
  padding: 20px;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.viewer-header h4 {
  margin: 0;
  color: #2c3e50;
}

.viewer-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.image-section h5,
.details-section h5 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.image-container {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.biometric-image {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.no-image {
  color: #7f8c8d;
  font-style: italic;
}

/* ダークモード対応 */
.dark-mode .biometric-manager {
  background: #1a202c;
}

.dark-mode .manager-header h3 {
  color: #ecf0f1;
}

.dark-mode .biometric-card {
  background: #2d3748;
  border-color: #4a5568;
}

.dark-mode .biometric-type {
  color: #ecf0f1;
}

.dark-mode .client-info h4 {
  color: #ecf0f1;
}

.dark-mode .detail-item .value {
  color: #ecf0f1;
}

.dark-mode .viewer-header {
  border-bottom-color: #4a5568;
}

.dark-mode .viewer-header h4 {
  color: #ecf0f1;
}

.dark-mode .image-section h5,
.dark-mode .details-section h5 {
  color: #ecf0f1;
}

.dark-mode .image-container {
  background: #4a5568;
  border-color: #718096;
}

.dark-mode .no-image {
  color: #a0aec0;
}
</style>
