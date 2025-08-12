<template>
  <div class="digital-signature-panel">
    <div class="panel-header">
      <h3><el-icon><Stamp /></el-icon> 電子署名・スタンプ・押印</h3>
    </div>

    <div class="signature-tools">
      <!-- 電子署名 -->
      <div class="signature-tool">
        <h4>電子署名</h4>
        <div class="tool-content">
          <el-input
            v-model="signerName"
            placeholder="署名者名"
            class="signer-input"
          />
          <el-button
            type="primary"
            @click="createDigitalSignature"
            :loading="signing"
            class="signature-btn"
          >
            <el-icon><Edit /></el-icon>
            電子署名を作成
          </el-button>
        </div>
      </div>

      <!-- 電子スタンプ -->
      <div class="signature-tool">
        <h4>電子スタンプ</h4>
        <div class="tool-content">
          <el-select
            v-model="selectedStamp"
            placeholder="スタンプを選択"
            class="stamp-select"
          >
            <el-option
              v-for="stamp in availableStamps"
              :key="stamp.id"
              :label="stamp.name"
              :value="stamp.id"
            >
              <div class="stamp-option">
                <div v-html="stamp.svg" class="stamp-preview"></div>
                <span>{{ stamp.name }}</span>
              </div>
            </el-option>
          </el-select>
          <el-button
            type="success"
            @click="applyElectronicStamp"
            :loading="stamping"
            class="stamp-btn"
          >
            <el-icon><Stamp /></el-icon>
            スタンプを適用
          </el-button>
        </div>
      </div>

      <!-- 電子押印 -->
      <div class="signature-tool">
        <h4>電子押印</h4>
        <div class="tool-content">
          <el-select
            v-model="selectedSeal"
            placeholder="押印を選択"
            class="seal-select"
          >
            <el-option
              v-for="seal in availableSeals"
              :key="seal.id"
              :label="seal.name"
              :value="seal.id"
            >
              <div class="seal-option">
                <div v-html="seal.svg" class="seal-preview"></div>
                <span>{{ seal.name }}</span>
              </div>
            </el-option>
          </el-select>
          <el-button
            type="warning"
            @click="applyDigitalSeal"
            :loading="sealing"
            class="seal-btn"
          >
            <el-icon><CircleCheck /></el-icon>
            押印を適用
          </el-button>
        </div>
      </div>
    </div>

    <!-- 署名履歴 -->
    <div class="signature-history">
      <h4>署名履歴</h4>
      <div v-if="signatures.length === 0" class="no-signatures">
        まだ署名がありません
      </div>
      <div v-else class="signature-list">
        <div
          v-for="signature in signatures"
          :key="signature.id"
          class="signature-item"
        >
          <div class="signature-icon">
            <el-icon v-if="signature.type === 'DIGITAL_SIGNATURE'"><Edit /></el-icon>
            <el-icon v-else-if="signature.type === 'ELECTRONIC_STAMP'"><Stamp /></el-icon>
            <el-icon v-else><CircleCheck /></el-icon>
          </div>
          <div class="signature-info">
            <div class="signature-name">{{ signature.signerName }}</div>
            <div class="signature-type">{{ getSignatureTypeLabel(signature.type) }}</div>
            <div class="signature-time">{{ formatDateTime(signature.timestamp) }}</div>
          </div>
          <div class="signature-actions">
            <el-button
              size="small"
              @click="verifySignature(signature.id)"
              :type="signature.verified ? 'success' : 'info'"
            >
              {{ signature.verified ? '検証済み' : '検証' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="removeSignature(signature.id)"
            >
              削除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- カスタムスタンプ・押印の作成 -->
    <div class="custom-tools">
      <h4>カスタム作成</h4>
      <div class="custom-tool">
        <el-button @click="showCustomStampDialog = true" type="info">
          <el-icon><Plus /></el-icon>
          カスタムスタンプ作成
        </el-button>
        <el-button @click="showCustomSealDialog = true" type="info">
          <el-icon><Plus /></el-icon>
          カスタム押印作成
        </el-button>
      </div>
    </div>

    <!-- カスタムスタンプ作成ダイアログ -->
    <el-dialog
      v-model="showCustomStampDialog"
      title="カスタムスタンプ作成"
      width="600px"
    >
      <div class="custom-stamp-form">
        <el-input
          v-model="customStampName"
          placeholder="スタンプ名"
          class="custom-input"
        />
        <el-input
          v-model="customStampSvg"
          type="textarea"
          :rows="10"
          placeholder="SVGコードを入力してください"
          class="custom-input"
        />
        <div class="svg-preview" v-if="customStampSvg">
          <div v-html="customStampSvg" class="preview-content"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCustomStampDialog = false">キャンセル</el-button>
        <el-button type="primary" @click="createCustomStamp">作成</el-button>
      </template>
    </el-dialog>

    <!-- カスタム押印作成ダイアログ -->
    <el-dialog
      v-model="showCustomSealDialog"
      title="カスタム押印作成"
      width="600px"
    >
      <div class="custom-seal-form">
        <el-input
          v-model="customSealName"
          placeholder="押印名"
          class="custom-input"
        />
        <el-input
          v-model="customSealSvg"
          type="textarea"
          :rows="10"
          placeholder="SVGコードを入力してください"
          class="custom-input"
        />
        <div class="svg-preview" v-if="customSealSvg">
          <div v-html="customSealSvg" class="preview-content"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCustomSealDialog = false">キャンセル</el-button>
        <el-button type="primary" @click="createCustomSeal">作成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Stamp, Edit, CircleCheck, Plus } from '@element-plus/icons-vue'
import {
  globalSignatureManager,
  SignatureType,
  type SignatureInfo
} from '@/utils/digitalSignature'

interface Props {
  documentId: string
  documentContent: string
  signerId: string
}

const props = defineProps<Props>()

// 状態管理
const signerName = ref('')
const selectedStamp = ref('')
const selectedSeal = ref('')
const signing = ref(false)
const stamping = ref(false)
const sealing = ref(false)
const signatures = ref<SignatureInfo[]>([])

// カスタム作成ダイアログ
const showCustomStampDialog = ref(false)
const showCustomSealDialog = ref(false)
const customStampName = ref('')
const customStampSvg = ref('')
const customSealName = ref('')
const customSealSvg = ref('')

// 利用可能なスタンプ・押印
const availableStamps = ref([
  {
    id: 'approval',
    name: '承認スタンプ',
    svg: '<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><circle cx="50" cy="50" r="45" fill="none" stroke="#e74c3c" stroke-width="3"/><text x="50" y="55" text-anchor="middle" font-size="12" fill="#e74c3c">承認</text></svg>'
  },
  {
    id: 'review',
    name: '確認スタンプ',
    svg: '<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><circle cx="50" cy="50" r="45" fill="none" stroke="#3498db" stroke-width="3"/><text x="50" y="55" text-anchor="middle" font-size="12" fill="#3498db">確認</text></svg>'
  }
])

const availableSeals = ref([
  {
    id: 'company',
    name: '会社印',
    svg: '<svg width="80" height="80" xmlns="http://www.w3.org/2000/svg"><rect x="10" y="10" width="60" height="60" fill="none" stroke="#2c3e50" stroke-width="2"/><text x="40" y="35" text-anchor="middle" font-size="10" fill="#2c3e50">会社</text><text x="40" y="50" text-anchor="middle" font-size="8" fill="#2c3e50">印</text></svg>'
  },
  {
    id: 'personal',
    name: '個人印',
    svg: '<svg width="80" height="80" xmlns="http://www.w3.org/2000/svg"><rect x="10" y="10" width="60" height="60" fill="none" stroke="#e74c3c" stroke-width="2"/><text x="40" y="35" text-anchor="middle" font-size="10" fill="#e74c3c">個人</text><text x="40" y="50" text-anchor="middle" font-size="8" fill="#e74c3c">印</text></svg>'
  }
])

// 電子署名の作成
const createDigitalSignature = async () => {
  if (!signerName.value.trim()) {
    ElMessage.warning('署名者名を入力してください')
    return
  }

  try {
    signing.value = true
    
    // 署名位置（実際の実装では、ユーザーがクリックした位置を使用）
    const position = { x: 100, y: 100 }
    
    const signature = globalSignatureManager.createDigitalSignature(
      props.documentContent,
      props.signerId,
      signerName.value,
      position,
      props.documentId
    )

    signatures.value.push(signature)
    ElMessage.success('電子署名を作成しました')
    
    // 署名者名をクリア
    signerName.value = ''
  } catch (error) {
    console.error('電子署名の作成に失敗しました:', error)
    ElMessage.error('電子署名の作成に失敗しました')
  } finally {
    signing.value = false
  }
}

// 電子スタンプの適用
const applyElectronicStamp = async () => {
  if (!selectedStamp.value) {
    ElMessage.warning('スタンプを選択してください')
    return
  }

  try {
    stamping.value = true
    
    const position = { x: 200, y: 100 }
    const stamp = globalSignatureManager.applyElectronicStamp(
      props.documentId,
      position,
      props.signerId,
      signerName.value || 'Unknown'
    )

    signatures.value.push(stamp)
    ElMessage.success('電子スタンプを適用しました')
    
    selectedStamp.value = ''
  } catch (error) {
    console.error('電子スタンプの適用に失敗しました:', error)
    ElMessage.error('電子スタンプの適用に失敗しました')
  } finally {
    stamping.value = false
  }
}

// 電子押印の適用
const applyDigitalSeal = async () => {
  if (!selectedSeal.value) {
    ElMessage.warning('押印を選択してください')
    return
  }

  try {
    sealing.value = true
    
    const position = { x: 300, y: 100 }
    const seal = globalSignatureManager.applyDigitalSeal(
      props.documentId,
      position,
      props.signerId,
      signerName.value || 'Unknown'
    )

    signatures.value.push(seal)
    ElMessage.success('電子押印を適用しました')
    
    selectedSeal.value = ''
  } catch (error) {
    console.error('電子押印の適用に失敗しました:', error)
    ElMessage.error('電子押印の適用に失敗しました')
  } finally {
    sealing.value = false
  }
}

// 署名の検証
const verifySignature = async (signatureId: string) => {
  try {
    const isValid = globalSignatureManager.verifySignature(signatureId, props.documentContent)
    
    if (isValid) {
      ElMessage.success('署名の検証が完了しました')
      // 署名の検証状態を更新
      const signature = signatures.value.find(s => s.id === signatureId)
      if (signature) {
        signature.verified = true
      }
    } else {
      ElMessage.error('署名の検証に失敗しました')
    }
  } catch (error) {
    console.error('署名の検証に失敗しました:', error)
    ElMessage.error('署名の検証に失敗しました')
  }
}

// 署名の削除
const removeSignature = (signatureId: string) => {
  const removed = globalSignatureManager.removeSignature(signatureId)
  if (removed) {
    signatures.value = signatures.value.filter(s => s.id !== signatureId)
    ElMessage.success('署名を削除しました')
  } else {
    ElMessage.error('署名の削除に失敗しました')
  }
}

// カスタムスタンプの作成
const createCustomStamp = () => {
  if (!customStampName.value.trim() || !customStampSvg.value.trim()) {
    ElMessage.warning('スタンプ名とSVGコードを入力してください')
    return
  }

  // 新しいスタンプを追加
  availableStamps.value.push({
    id: `custom_${Date.now()}`,
    name: customStampName.value,
    svg: customStampSvg.value
  })

  ElMessage.success('カスタムスタンプを作成しました')
  showCustomStampDialog.value = false
  customStampName.value = ''
  customStampSvg.value = ''
}

// カスタム押印の作成
const createCustomSeal = () => {
  if (!customSealName.value.trim() || !customSealSvg.value.trim()) {
    ElMessage.warning('押印名とSVGコードを入力してください')
    return
  }

  // 新しい押印を追加
  availableSeals.value.push({
    id: `custom_${Date.now()}`,
    name: customSealName.value,
    svg: customSealSvg.value
  })

  ElMessage.success('カスタム押印を作成しました')
  showCustomSealDialog.value = false
  customSealName.value = ''
  customSealSvg.value = ''
}

// 署名タイプのラベル取得
const getSignatureTypeLabel = (type: SignatureType): string => {
  switch (type) {
    case SignatureType.DIGITAL_SIGNATURE:
      return '電子署名'
    case SignatureType.ELECTRONIC_STAMP:
      return '電子スタンプ'
    case SignatureType.DIGITAL_SEAL:
      return '電子押印'
    default:
      return '不明'
  }
}

// 日時フォーマット
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
  // 既存の署名を読み込み
  signatures.value = globalSignatureManager.getDocumentSignatures(props.documentId)
})
</script>

<style scoped>
.digital-signature-panel {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.panel-header {
  margin-bottom: 20px;
}

.panel-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
}

.signature-tools {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.signature-tool {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.signature-tool h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.tool-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.signer-input,
.stamp-select,
.seal-select {
  width: 100%;
}

.signature-btn,
.stamp-btn,
.seal-btn {
  width: 100%;
}

.signature-history {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.signature-history h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.no-signatures {
  text-align: center;
  color: #7f8c8d;
  padding: 20px;
}

.signature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.signature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.signature-icon {
  width: 40px;
  height: 40px;
  background: #e3f2fd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1976d2;
}

.signature-info {
  flex: 1;
}

.signature-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.signature-type {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 2px;
}

.signature-time {
  font-size: 11px;
  color: #95a5a6;
}

.signature-actions {
  display: flex;
  gap: 8px;
}

.custom-tools {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.custom-tools h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.custom-tool {
  display: flex;
  gap: 12px;
}

.custom-input {
  margin-bottom: 15px;
}

.svg-preview {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.preview-content {
  display: inline-block;
}

.stamp-option,
.seal-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stamp-preview,
.seal-preview {
  width: 30px;
  height: 30px;
}

/* ダークモード対応 */
.dark-mode .digital-signature-panel {
  background: #2d3748;
}

.dark-mode .signature-tool,
.dark-mode .signature-history,
.dark-mode .custom-tools {
  background: #1a202c;
  color: #ecf0f1;
}

.dark-mode .panel-header h3,
.dark-mode .signature-tool h4,
.dark-mode .signature-history h4,
.dark-mode .custom-tools h4 {
  color: #ecf0f1;
}

.dark-mode .signature-item {
  background: #4a5568;
  border-color: #718096;
}

.dark-mode .signature-name {
  color: #ecf0f1;
}

.dark-mode .svg-preview {
  background: #4a5568;
}
</style>
