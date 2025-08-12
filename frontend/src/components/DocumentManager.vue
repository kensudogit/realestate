<template>
  <div class="document-manager">
    <div class="manager-header">
      <h3><el-icon><Document /></el-icon> 電子書類管理</h3>
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        書類をアップロード
      </el-button>
    </div>

    <!-- 検索・フィルター -->
    <div class="search-filters">
      <el-input
        v-model="searchQuery"
        placeholder="書類名、説明、タグで検索"
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
        placeholder="書類タイプ"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="type in documentTypes"
          :key="type.value"
          :label="type.label"
          :value="type.value"
        />
      </el-select>
      
      <el-select
        v-model="filterStatus"
        placeholder="ステータス"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="status in documentStatuses"
          :key="status.value"
          :label="status.label"
          :value="status.value"
        />
      </el-select>
    </div>

    <!-- 書類一覧 -->
    <div class="document-list">
      <div v-if="filteredDocuments.length === 0" class="no-documents">
        <el-empty description="書類が見つかりません" />
      </div>
      
      <div v-else class="documents-grid">
        <div
          v-for="document in filteredDocuments"
          :key="document.id"
          class="document-card"
        >
          <div class="document-icon">
            <el-icon v-if="document.mimeType.includes('pdf')"><Document /></el-icon>
            <el-icon v-else-if="document.mimeType.includes('image')"><Picture /></el-icon>
            <el-icon v-else-if (document.mimeType.includes('word'))><Document /></el-icon>
            <el-icon v-else-if (document.mimeType.includes('excel'))><Grid /></el-icon>
            <el-icon v-else><Document /></el-icon>
          </div>
          
          <div class="document-info">
            <h4 class="document-name">{{ document.name }}</h4>
            <div class="document-meta">
              <el-tag :type="getStatusType(document.status)" size="small">
                {{ getStatusLabel(document.status) }}
              </el-tag>
              <el-tag type="info" size="small">
                {{ getTypeLabel(document.type) }}
              </el-tag>
            </div>
            <div class="document-details">
              <span class="file-size">{{ formatFileSize(document.fileSize) }}</span>
              <span class="upload-date">{{ formatDate(document.uploadDate) }}</span>
            </div>
            <div class="document-tags" v-if="document.tags.length > 0">
              <el-tag
                v-for="tag in document.tags.slice(0, 3)"
                :key="tag"
                size="small"
                class="tag"
              >
                {{ tag }}
              </el-tag>
              <span v-if="document.tags.length > 3" class="more-tags">
                +{{ document.tags.length - 3 }}
              </span>
            </div>
          </div>
          
          <div class="document-actions">
            <el-button
              size="small"
              @click="downloadDocument(document.id)"
              :loading="downloading === document.id"
            >
              <el-icon><Download /></el-icon>
              ダウンロード
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="viewDocument(document)"
            >
              <el-icon><View /></el-icon>
              表示
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="editDocument(document)"
            >
              <el-icon><Edit /></el-icon>
              編集
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteDocument(document.id)"
            >
              <el-icon><Delete /></el-icon>
              削除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- アップロードダイアログ -->
    <el-dialog
      v-model="showUploadDialog"
      title="書類のアップロード"
      width="600px"
    >
      <div class="upload-form">
        <el-form :model="uploadForm" label-width="120px">
          <el-form-item label="書類名">
            <el-input v-model="uploadForm.name" placeholder="書類名を入力" />
          </el-form-item>
          
          <el-form-item label="書類タイプ">
            <el-select v-model="uploadForm.type" placeholder="タイプを選択">
              <el-option
                v-for="type in documentTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="説明">
            <el-input
              v-model="uploadForm.description"
              type="textarea"
              :rows="3"
              placeholder="書類の説明を入力"
            />
          </el-form-item>
          
          <el-form-item label="タグ">
            <el-input
              v-model="uploadForm.tagsInput"
              placeholder="カンマ区切りでタグを入力"
            />
          </el-form-item>
          
          <el-form-item label="関連契約">
            <el-select v-model="uploadForm.contractId" placeholder="契約を選択" clearable>
              <el-option
                v-for="contract in contracts"
                :key="contract.id"
                :label="`${contract.contractNumber} - ${contract.propertyName}`"
                :value="contract.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="関連物件">
            <el-select v-model="uploadForm.propertyId" placeholder="物件を選択" clearable>
              <el-option
                v-for="property in properties"
                :key="property.id"
                :label="property.name"
                :value="property.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="関連顧客">
            <el-select v-model="uploadForm.clientId" placeholder="顧客を選択" clearable>
              <el-option
                v-for="client in clients"
                :key="client.id"
                :label="`${client.firstName} ${client.lastName}`"
                :value="client.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="ファイル">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :on-change="handleFileChange"
              :file-list="fileList"
              :limit="1"
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.gif,.txt,.xls,.xlsx"
            >
              <el-button type="primary">ファイルを選択</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  PDF、Word、Excel、画像、テキストファイルをアップロードできます（最大50MB）
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showUploadDialog = false">キャンセル</el-button>
        <el-button
          type="primary"
          @click="uploadDocument"
          :loading="uploading"
          :disabled="!selectedFile"
        >
          アップロード
        </el-button>
      </template>
    </el-dialog>

    <!-- 書類表示ダイアログ -->
    <el-dialog
      v-model="showViewDialog"
      title="書類の表示"
      width="80%"
      :fullscreen="isFullscreen"
    >
      <div class="document-viewer">
        <div v-if="viewingDocument" class="viewer-content">
          <div class="viewer-header">
            <h4>{{ viewingDocument.name }}</h4>
            <div class="viewer-actions">
              <el-button @click="toggleFullscreen">
                <el-icon><FullScreen /></el-icon>
                {{ isFullscreen ? '縮小' : '全画面' }}
              </el-button>
              <el-button @click="downloadDocument(viewingDocument.id)">
                <el-icon><Download /></el-icon>
                ダウンロード
              </el-button>
            </div>
          </div>
          
          <div class="viewer-body">
            <iframe
              v-if="viewingDocument.mimeType === 'application/pdf'"
              :src="viewingDocument.url"
              width="100%"
              height="600"
              frameborder="0"
            />
            <img
              v-else-if="viewingDocument.mimeType.includes('image')"
              :src="viewingDocument.url"
              alt="書類画像"
              style="max-width: 100%; height: auto;"
            />
            <div v-else class="unsupported-format">
              このファイル形式は表示できません。ダウンロードしてご確認ください。
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 統計情報 -->
    <div class="statistics">
      <el-card class="stat-card">
        <template #header>
          <div class="stat-header">
            <span>統計情報</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-item">
            <span class="stat-label">総書類数</span>
            <span class="stat-value">{{ statistics.totalDocuments }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">総容量</span>
            <span class="stat-value">{{ formatFileSize(statistics.totalSize) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">署名済み</span>
            <span class="stat-value">{{ signedDocumentsCount }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Upload,
  Download,
  View,
  Edit,
  Delete,
  Search,
  Picture,
  Grid,
  FullScreen
} from '@element-plus/icons-vue'
import {
  globalDocumentManager,
  DocumentType,
  DocumentStatus,
  type DocumentInfo,
  SUPPORTED_FILE_TYPES,
  MAX_FILE_SIZE_MB
} from '@/utils/documentManager'

// 状態管理
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const showUploadDialog = ref(false)
const showViewDialog = ref(false)
const uploading = ref(false)
const downloading = ref<string | null>(null)
const selectedFile = ref<File | null>(null)
const fileList = ref<any[]>([])
const viewingDocument = ref<DocumentInfo | null>(null)
const isFullscreen = ref(false)

// アップロードフォーム
const uploadForm = ref({
  name: '',
  type: DocumentType.OTHER,
  description: '',
  tagsInput: '',
  contractId: '',
  propertyId: '',
  clientId: ''
})

// サンプルデータ（実際の実装ではAPIから取得）
const contracts = ref([
  { id: '1', contractNumber: 'CT001', propertyName: 'サンプル物件A' },
  { id: '2', contractNumber: 'CT002', propertyName: 'サンプル物件B' }
])

const properties = ref([
  { id: '1', name: 'サンプル物件A' },
  { id: '2', name: 'サンプル物件B' }
])

const clients = ref([
  { id: '1', firstName: '田中', lastName: '太郎' },
  { id: '2', firstName: '佐藤', lastName: '花子' }
])

// 書類タイプとステータスのオプション
const documentTypes = [
  { value: DocumentType.CONTRACT, label: '売買契約書' },
  { value: DocumentType.PROPERTY_REGISTRATION, label: '不動産登記簿謄本' },
  { value: DocumentType.SURVEY_REPORT, label: '測量図' },
  { value: DocumentType.BUILDING_CERTIFICATE, label: '建築確認書' },
  { value: DocumentType.TAX_CERTIFICATE, label: '固定資産税評価証明書' },
  { value: DocumentType.INSURANCE_POLICY, label: '火災保険証券' },
  { value: DocumentType.OTHER, label: 'その他' }
]

const documentStatuses = [
  { value: DocumentStatus.DRAFT, label: '下書き' },
  { value: DocumentStatus.PENDING_REVIEW, label: '審査中' },
  { value: DocumentStatus.APPROVED, label: '承認済み' },
  { value: DocumentStatus.REJECTED, label: '却下' },
  { value: DocumentStatus.ARCHIVED, label: 'アーカイブ' }
]

// フィルタリングされた書類
const filteredDocuments = computed(() => {
  return globalDocumentManager.searchDocuments(searchQuery.value, {
    type: filterType.value || undefined,
    status: filterStatus.value || undefined
  })
})

// 統計情報
const statistics = computed(() => {
  return globalDocumentManager.getStorageUsage()
})

const signedDocumentsCount = computed(() => {
  return filteredDocuments.value.filter(doc => doc.isSigned).length
})

// 検索処理
const handleSearch = () => {
  // 検索はcomputedで自動的に実行される
}

// ファイル選択処理
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
  
  // ファイル形式とサイズの検証
  if (!SUPPORTED_FILE_TYPES.includes(file.raw.type)) {
    ElMessage.error('サポートされていないファイル形式です')
    selectedFile.value = null
    return
  }
  
  if (file.raw.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
    ElMessage.error(`ファイルサイズは${MAX_FILE_SIZE_MB}MB以下にしてください`)
    selectedFile.value = null
    return
  }
}

// 書類のアップロード
const uploadDocument = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('ファイルを選択してください')
    return
  }

  try {
    uploading.value = true
    
    const tags = uploadForm.value.tagsInput
      .split(',')
      .map(tag => tag.trim())
      .filter(tag => tag.length > 0)

    const documentInfo = {
      name: uploadForm.value.name,
      type: uploadForm.value.type,
      contractId: uploadForm.value.contractId || undefined,
      propertyId: uploadForm.value.propertyId || undefined,
      clientId: uploadForm.value.clientId || undefined,
      description: uploadForm.value.description,
      tags,
      uploadedBy: 'current-user' // 実際の実装では認証情報から取得
    }

    const document = await globalDocumentManager.uploadDocument(
      selectedFile.value,
      documentInfo
    )

    ElMessage.success('書類のアップロードが完了しました')
    
    // フォームをリセット
    uploadForm.value = {
      name: '',
      type: DocumentType.OTHER,
      description: '',
      tagsInput: '',
      contractId: '',
      propertyId: '',
      clientId: ''
    }
    selectedFile.value = null
    fileList.value = []
    showUploadDialog.value = false
    
  } catch (error) {
    console.error('書類のアップロードに失敗しました:', error)
    ElMessage.error('書類のアップロードに失敗しました')
  } finally {
    uploading.value = false
  }
}

// 書類のダウンロード
const downloadDocument = async (documentId: string) => {
  try {
    downloading.value = documentId
    
    const result = await globalDocumentManager.downloadDocument(documentId)
    if (!result) {
      ElMessage.error('書類のダウンロードに失敗しました')
      return
    }

    const { document, blob } = result
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = document.originalFileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('書類のダウンロードが完了しました')
  } catch (error) {
    console.error('書類のダウンロードに失敗しました:', error)
    ElMessage.error('書類のダウンロードに失敗しました')
  } finally {
    downloading.value = null
  }
}

// 書類の表示
const viewDocument = (document: DocumentInfo) => {
  viewingDocument.value = document
  showViewDialog.value = true
}

// 書類の編集
const editDocument = (document: DocumentInfo) => {
  // 編集機能の実装
  ElMessage.info('編集機能は開発中です')
}

// 書類の削除
const deleteDocument = async (documentId: string) => {
  try {
    await ElMessageBox.confirm(
      'この書類を削除してもよろしいですか？',
      '削除の確認',
      {
        confirmButtonText: '削除',
        cancelButtonText: 'キャンセル',
        type: 'warning'
      }
    )

    const deleted = globalDocumentManager.deleteDocument(documentId)
    if (deleted) {
      ElMessage.success('書類を削除しました')
    } else {
      ElMessage.error('書類の削除に失敗しました')
    }
  } catch (error) {
    // ユーザーがキャンセルした場合
  }
}

// 全画面表示の切り替え
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// ユーティリティ関数
const getStatusType = (status: DocumentStatus): string => {
  switch (status) {
    case DocumentStatus.APPROVED: return 'success'
    case DocumentStatus.PENDING_REVIEW: return 'warning'
    case DocumentStatus.REJECTED: return 'danger'
    case DocumentStatus.ARCHIVED: return 'info'
    default: return 'default'
  }
}

const getStatusLabel = (status: DocumentStatus): string => {
  const statusOption = documentStatuses.find(s => s.value === status)
  return statusOption ? statusOption.label : status
}

const getTypeLabel = (type: DocumentType): string => {
  const typeOption = documentTypes.find(t => t.value === type)
  return typeOption ? typeOption.label : type
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (date: Date): string => {
  return new Date(date).toLocaleDateString('ja-JP')
}

// 初期化
onMounted(() => {
  // 初期データの読み込み
})
</script>

<style scoped>
.document-manager {
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

.search-filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 300px;
}

.filter-select {
  min-width: 150px;
}

.document-list {
  margin-bottom: 30px;
}

.no-documents {
  text-align: center;
  padding: 40px;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.document-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.document-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.document-icon {
  width: 50px;
  height: 50px;
  background: #e3f2fd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1976d2;
  margin-bottom: 15px;
}

.document-name {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.document-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.document-details {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
  color: #7f8c8d;
}

.document-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 15px;
}

.tag {
  margin: 0;
}

.more-tags {
  font-size: 11px;
  color: #95a5a6;
  align-self: center;
}

.document-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.upload-form {
  max-height: 60vh;
  overflow-y: auto;
}

.document-viewer {
  height: 100%;
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

.viewer-actions {
  display: flex;
  gap: 10px;
}

.viewer-body {
  min-height: 400px;
}

.unsupported-format {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.statistics {
  margin-top: 30px;
}

.stat-card {
  max-width: 400px;
}

.stat-header {
  font-weight: 600;
  color: #2c3e50;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #7f8c8d;
}

.stat-value {
  font-weight: 600;
  color: #2c3e50;
}

/* ダークモード対応 */
.dark-mode .document-manager {
  background: #1a202c;
}

.dark-mode .manager-header h3 {
  color: #ecf0f1;
}

.dark-mode .document-card {
  background: #2d3748;
  border-color: #4a5568;
}

.dark-mode .document-name {
  color: #ecf0f1;
}

.dark-mode .document-icon {
  background: #4a5568;
  color: #63b3ed;
}

.dark-mode .viewer-header {
  border-bottom-color: #4a5568;
}

.dark-mode .viewer-header h4 {
  color: #ecf0f1;
}

.dark-mode .stat-header {
  color: #ecf0f1;
}

.dark-mode .stat-value {
  color: #ecf0f1;
}
</style>
