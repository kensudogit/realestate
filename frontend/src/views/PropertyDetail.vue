<template>
  <div class="property-detail">
    <div class="header">
      <el-button @click="$router.go(-1)">
        <el-icon><ArrowLeft /></el-icon>
        戻る
      </el-button>
      <h1>物件詳細</h1>
      <el-button type="primary" @click="editProperty">
        <el-icon><Edit /></el-icon>
        編集
      </el-button>
    </div>

    <el-card v-if="property" class="detail-card">
      <div class="property-header">
        <h2>{{ property.name }}</h2>
        <div class="status-badges">
          <el-tag :type="getTypeTagType(property.type)" size="large">
            {{ getTypeLabel(property.type) }}
          </el-tag>
          <el-tag :type="getStatusTagType(property.status)" size="large">
            {{ getStatusLabel(property.status) }}
          </el-tag>
        </div>
      </div>

      <el-divider />

      <el-row :gutter="24">
        <el-col :span="12">
          <h3>基本情報</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="物件名">{{ property.name }}</el-descriptions-item>
            <el-descriptions-item label="住所">{{ property.address }}</el-descriptions-item>
            <el-descriptions-item label="説明">{{ property.description || '説明なし' }}</el-descriptions-item>
            <el-descriptions-item label="価格">¥{{ property.price.toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="面積">{{ property.area }}㎡</el-descriptions-item>
            <el-descriptions-item label="築年数">{{ property.yearBuilt }}年</el-descriptions-item>
          </el-descriptions>
        </el-col>

        <el-col :span="12">
          <h3>設備・間取り</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="部屋数">{{ property.rooms }}部屋</el-descriptions-item>
            <el-descriptions-item label="浴室数">{{ property.bathrooms }}個</el-descriptions-item>
            <el-descriptions-item label="駐車場">{{ property.parkingSpaces }}台分</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="24">
        <el-col :span="12">
          <h3>システム情報</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="作成日時">
              {{ formatDateTime(property.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新日時">
              {{ formatDateTime(property.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-col>

        <el-col :span="12">
          <h3>アクション</h3>
          <div class="action-buttons">
            <el-button type="success" @click="changeStatus('AVAILABLE')" 
                       :disabled="property.status === 'AVAILABLE'">
              利用可能にする
            </el-button>
            <el-button type="warning" @click="changeStatus('MAINTENANCE')"
                       :disabled="property.status === 'MAINTENANCE'">
              メンテナンス中にする
            </el-button>
            <el-button type="danger" @click="deleteProperty">
              削除
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-empty v-else description="物件が見つかりません" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { propertyApi } from '@/services/api'
import type { Property, PropertyType, PropertyStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const property = ref<Property | null>(null)
const loading = ref(false)

const propertyTypes = [
  { value: 'APARTMENT', label: 'マンション' },
  { value: 'HOUSE', label: '一戸建て' },
  { value: 'COMMERCIAL', label: '商業施設' },
  { value: 'LAND', label: '土地' },
  { value: 'OFFICE', label: 'オフィス' },
  { value: 'WAREHOUSE', label: '倉庫' }
]

const propertyStatuses = [
  { value: 'AVAILABLE', label: '利用可能' },
  { value: 'SOLD', label: '売却済み' },
  { value: 'RENTED', label: '賃貸中' },
  { value: 'UNDER_CONTRACT', label: '契約中' },
  { value: 'MAINTENANCE', label: 'メンテナンス中' }
]

onMounted(() => {
  loadProperty()
})

const loadProperty = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const response = await propertyApi.getById(id)
    property.value = response.data
  } catch (error) {
    ElMessage.error('物件データの取得に失敗しました')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editProperty = () => {
  if (property.value?.id) {
    router.push(`/properties/${property.value.id}/edit`)
  }
}

const changeStatus = async (newStatus: PropertyStatus) => {
  if (!property.value?.id) return

  try {
    const updatedProperty = { ...property.value, status: newStatus }
    const response = await propertyApi.update(property.value.id, updatedProperty)
    property.value = response.data
    ElMessage.success('ステータスを更新しました')
  } catch (error) {
    ElMessage.error('ステータスの更新に失敗しました')
    console.error(error)
  }
}

const deleteProperty = async () => {
  if (!property.value?.id) return

  try {
    await ElMessageBox.confirm(
      `「${property.value.name}」を削除しますか？`,
      '確認',
      {
        confirmButtonText: '削除',
        cancelButtonText: 'キャンセル',
        type: 'warning'
      }
    )
    
    await propertyApi.delete(property.value.id)
    ElMessage.success('物件を削除しました')
    router.go(-1)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('物件の削除に失敗しました')
      console.error(error)
    }
  }
}

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
    UNDER_CONTRACT: 'primary',
    MAINTENANCE: 'info'
  }
  return statusMap[status] || ''
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '不明'
  return new Date(dateTime).toLocaleString('ja-JP')
}
</script>

<style scoped>
.property-detail {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #303133;
}

.detail-card {
  margin-bottom: 20px;
}

.property-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.property-header h2 {
  margin: 0;
  color: #303133;
}

.status-badges {
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

h3 {
  color: #606266;
  margin-bottom: 15px;
}

.el-divider {
  margin: 30px 0;
}
</style>
