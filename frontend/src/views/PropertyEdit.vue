<template>
  <div class="property-edit">
    <div class="header">
      <el-button @click="$router.go(-1)">
        <el-icon><ArrowLeft /></el-icon>
        戻る
      </el-button>
      <h1>{{ isEdit ? '物件編集' : '新規物件登録' }}</h1>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <h3>基本情報</h3>
            
            <el-form-item label="物件名" prop="name">
              <el-input v-model="form.name" placeholder="物件名を入力" />
            </el-form-item>
            
            <el-form-item label="住所" prop="address">
              <el-input v-model="form.address" placeholder="住所を入力" />
            </el-form-item>
            
            <el-form-item label="説明" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="物件の説明を入力"
              />
            </el-form-item>
            
            <el-form-item label="物件タイプ" prop="type">
              <el-select v-model="form.type" placeholder="タイプを選択" style="width: 100%">
                <el-option
                  v-for="type in propertyTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="ステータス" prop="status">
              <el-select v-model="form.status" placeholder="ステータスを選択" style="width: 100%">
                <el-option
                  v-for="status in propertyStatuses"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <h3>価格・面積</h3>
            
            <el-form-item label="価格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="0"
                :step="10000"
                style="width: 100%"
                placeholder="価格を入力"
              />
            </el-form-item>
            
            <el-form-item label="面積" prop="area">
              <el-input-number
                v-model="form.area"
                :min="0"
                :precision="2"
                :step="0.01"
                style="width: 100%"
                placeholder="面積を入力"
              />
              <span class="unit">㎡</span>
            </el-form-item>
            
            <h3>設備・間取り</h3>
            
            <el-form-item label="部屋数" prop="rooms">
              <el-input-number
                v-model="form.rooms"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="部屋数を入力"
              />
            </el-form-item>
            
            <el-form-item label="浴室数" prop="bathrooms">
              <el-input-number
                v-model="form.bathrooms"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="浴室数を入力"
              />
            </el-form-item>
            
            <el-form-item label="駐車場" prop="parkingSpaces">
              <el-input-number
                v-model="form.parkingSpaces"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="駐車場台数を入力"
              />
            </el-form-item>
            
            <el-form-item label="築年数" prop="yearBuilt">
              <el-input-number
                v-model="form.yearBuilt"
                :min="1900"
                :max="new Date().getFullYear()"
                :precision="0"
                style="width: 100%"
                placeholder="築年数を入力"
              />
              <span class="unit">年</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <div class="form-actions">
          <el-button @click="$router.go(-1)">キャンセル</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '更新' : '登録' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { propertyApi } from '@/services/api'
import type { Property, PropertyType, PropertyStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  address: '',
  description: '',
  type: '' as PropertyType,
  status: 'AVAILABLE' as PropertyStatus,
  price: 0,
  area: 0,
  rooms: 0,
  bathrooms: 0,
  parkingSpaces: 0,
  yearBuilt: new Date().getFullYear()
})

const rules = {
  name: [
    { required: true, message: '物件名を入力してください', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '住所を入力してください', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '物件タイプを選択してください', trigger: 'change' }
  ],
  status: [
    { required: true, message: 'ステータスを選択してください', trigger: 'change' }
  ],
  price: [
    { required: true, message: '価格を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '価格は0以上で入力してください', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '面積を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '面積は0以上で入力してください', trigger: 'blur' }
  ],
  rooms: [
    { required: true, message: '部屋数を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '部屋数は0以上で入力してください', trigger: 'blur' }
  ],
  bathrooms: [
    { required: true, message: '浴室数を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '浴室数は0以上で入力してください', trigger: 'blur' }
  ],
  parkingSpaces: [
    { required: true, message: '駐車場台数を入力してください', trigger: 'blur' },
    { type: 'number', min: 0, message: '駐車場台数は0以上で入力してください', trigger: 'blur' }
  ],
  yearBuilt: [
    { required: true, message: '築年数を入力してください', trigger: 'blur' },
    { type: 'number', min: 1900, max: new Date().getFullYear(), message: '築年数は1900年から現在までで入力してください', trigger: 'blur' }
  ]
}

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
  if (isEdit.value) {
    loadProperty()
  }
})

const loadProperty = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const response = await propertyApi.getById(id)
    const property = response.data
    
    form.value = {
      name: property.name,
      address: property.address,
      description: property.description,
      type: property.type,
      status: property.status,
      price: property.price,
      area: property.area,
      rooms: property.rooms,
      bathrooms: property.bathrooms,
      parkingSpaces: property.parkingSpaces,
      yearBuilt: property.yearBuilt
    }
  } catch (error) {
    ElMessage.error('物件データの取得に失敗しました')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    if (isEdit.value) {
      const id = Number(route.params.id)
      await propertyApi.update(id, form.value)
      ElMessage.success('物件を更新しました')
    } else {
      await propertyApi.create(form.value)
      ElMessage.success('物件を登録しました')
    }
    
    router.push('/properties')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '物件の更新に失敗しました' : '物件の登録に失敗しました')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.property-edit {
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

h3 {
  color: #606266;
  margin-bottom: 15px;
  margin-top: 20px;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.el-divider {
  margin: 30px 0;
}
</style>
