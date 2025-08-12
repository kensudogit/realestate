/**
 * 指紋・掌紋管理機能
 * 不動産取引における本人確認のための生体認証機能を提供
 */

// 生体認証の種類
export enum BiometricType {
  FINGERPRINT = 'FINGERPRINT',     // 指紋
  PALM_PRINT = 'PALM_PRINT',       // 掌紋
  FACE_RECOGNITION = 'FACE_RECOGNITION' // 顔認識
}

// 指紋の種類
export enum FingerprintType {
  THUMB = 'THUMB',           // 親指
  INDEX = 'INDEX',           // 人差し指
  MIDDLE = 'MIDDLE',         // 中指
  RING = 'RING',             // 薬指
  LITTLE = 'LITTLE'          // 小指
}

// 掌紋の種類
export enum PalmPrintType {
  RIGHT_HAND = 'RIGHT_HAND', // 右手
  LEFT_HAND = 'LEFT_HAND'    // 左手
}

// 生体認証データのインターフェース
export interface BiometricData {
  id: string
  type: BiometricType
  fingerprintType?: FingerprintType
  palmPrintType?: PalmPrintType
  clientId: string
  clientName: string
  captureDate: Date
  captureDevice: string
  quality: number // 0-100
  template: string // 生体認証テンプレート（暗号化）
  imageData?: string // 画像データ（Base64）
  metadata: {
    resolution: string
    compression: string
    algorithm: string
    version: string
  }
  isVerified: boolean
  verificationDate?: Date
  notes?: string
}

// 生体認証管理クラス
export class BiometricManager {
  private biometricData: Map<string, BiometricData> = new Map()
  private templates: Map<string, string> = new Map()

  // 指紋データの登録
  async registerFingerprint(
    clientId: string,
    clientName: string,
    fingerprintType: FingerprintType,
    imageFile: File,
    captureDevice: string = 'Unknown Device'
  ): Promise<BiometricData> {
    try {
      // 画像の読み込みと処理
      const imageData = await this.processImageFile(imageFile)
      
      // 指紋テンプレートの生成（実際の実装では専用のライブラリを使用）
      const template = await this.generateFingerprintTemplate(imageData)
      
      // 品質スコアの計算
      const quality = this.calculateQuality(imageData)
      
      // 生体認証データの作成
      const biometricData: BiometricData = {
        id: this.generateBiometricId(),
        type: BiometricType.FINGERPRINT,
        fingerprintType,
        clientId,
        clientName,
        captureDate: new Date(),
        captureDevice,
        quality,
        template,
        imageData,
        metadata: {
          resolution: `${imageFile.width || 'Unknown'}x${imageFile.height || 'Unknown'}`,
          compression: 'JPEG',
          algorithm: 'MINUTIAE_EXTRACTION',
          version: '1.0'
        },
        isVerified: false
      }

      // データの保存
      this.biometricData.set(biometricData.id, biometricData)
      this.templates.set(biometricData.id, template)

      return biometricData
    } catch (error) {
      console.error('指紋登録に失敗しました:', error)
      throw new Error('指紋登録に失敗しました')
    }
  }

  // 掌紋データの登録
  async registerPalmPrint(
    clientId: string,
    clientName: string,
    palmPrintType: PalmPrintType,
    imageFile: File,
    captureDevice: string = 'Unknown Device'
  ): Promise<BiometricData> {
    try {
      // 画像の読み込みと処理
      const imageData = await this.processImageFile(imageFile)
      
      // 掌紋テンプレートの生成
      const template = await this.generatePalmPrintTemplate(imageData)
      
      // 品質スコアの計算
      const quality = this.calculateQuality(imageData)
      
      // 生体認証データの作成
      const biometricData: BiometricData = {
        id: this.generateBiometricId(),
        type: BiometricType.PALM_PRINT,
        palmPrintType,
        clientId,
        clientName,
        captureDate: new Date(),
        captureDevice,
        quality,
        template,
        imageData,
        metadata: {
          resolution: `${imageFile.width || 'Unknown'}x${imageFile.height || 'Unknown'}`,
          compression: 'JPEG',
          algorithm: 'PALM_PATTERN_EXTRACTION',
          version: '1.0'
        },
        isVerified: false
      }

      // データの保存
      this.biometricData.set(biometricData.id, biometricData)
      this.templates.set(biometricData.id, template)

      return biometricData
    } catch (error) {
      console.error('掌紋登録に失敗しました:', error)
      throw new Error('掌紋登録に失敗しました')
    }
  }

  // 指紋の検索・照合
  async searchFingerprint(
    queryTemplate: string,
    threshold: number = 0.8
  ): Promise<BiometricData[]> {
    const matches: Array<{ data: BiometricData; score: number }> = []
    
    for (const [id, data] of this.biometricData.entries()) {
      if (data.type === BiometricType.FINGERPRINT) {
        const template = this.templates.get(id)
        if (template) {
          const score = await this.compareTemplates(queryTemplate, template)
          if (score >= threshold) {
            matches.push({ data, score })
          }
        }
      }
    }

    // スコア順にソート
    matches.sort((a, b) => b.score - a.score)
    
    return matches.map(match => match.data)
  }

  // 掌紋の検索・照合
  async searchPalmPrint(
    queryTemplate: string,
    threshold: number = 0.8
  ): Promise<BiometricData[]> {
    const matches: Array<{ data: BiometricData; score: number }> = []
    
    for (const [id, data] of this.biometricData.entries()) {
      if (data.type === BiometricType.PALM_PRINT) {
        const template = this.templates.get(id)
        if (template) {
          const score = await this.compareTemplates(queryTemplate, template)
          if (score >= threshold) {
            matches.push({ data, score })
          }
        }
      }
    }

    // スコア順にソート
    matches.sort((a, b) => b.score - a.score)
    
    return matches.map(match => match.data)
  }

  // 顧客の生体認証データを取得
  getClientBiometrics(clientId: string): BiometricData[] {
    return Array.from(this.biometricData.values())
      .filter(data => data.clientId === clientId)
      .sort((a, b) => b.captureDate.getTime() - a.captureDate.getTime())
  }

  // 生体認証データの検索
  searchBiometrics(
    query: string,
    filters?: {
      type?: BiometricType
      fingerprintType?: FingerprintType
      palmPrintType?: PalmPrintType
      clientId?: string
      quality?: { min: number; max: number }
      dateRange?: { start: Date; end: Date }
    }
  ): BiometricData[] {
    const searchTerm = query.toLowerCase()
    
    return Array.from(this.biometricData.values())
      .filter(data => {
        // 基本検索
        const matchesQuery = 
          data.clientName.toLowerCase().includes(searchTerm) ||
          data.captureDevice.toLowerCase().includes(searchTerm) ||
          data.notes?.toLowerCase().includes(searchTerm)

        if (!matchesQuery) return false

        // フィルター適用
        if (filters) {
          if (filters.type && data.type !== filters.type) return false
          if (filters.fingerprintType && data.fingerprintType !== filters.fingerprintType) return false
          if (filters.palmPrintType && data.palmPrintType !== filters.palmPrintType) return false
          if (filters.clientId && data.clientId !== filters.clientId) return false
          if (filters.quality && (data.quality < filters.quality.min || data.quality > filters.quality.max)) return false
          if (filters.dateRange && (data.captureDate < filters.dateRange.start || data.captureDate > filters.dateRange.end)) return false
        }

        return true
      })
      .sort((a, b) => b.captureDate.getTime() - a.captureDate.getTime())
  }

  // 生体認証データの更新
  updateBiometricData(id: string, updates: Partial<BiometricData>): BiometricData | null {
    const data = this.biometricData.get(id)
    if (!data) return null

    const updatedData = { ...data, ...updates }
    this.biometricData.set(id, updatedData)

    return updatedData
  }

  // 生体認証データの削除
  deleteBiometricData(id: string): boolean {
    const deleted = this.biometricData.delete(id)
    if (deleted) {
      this.templates.delete(id)
    }
    return deleted
  }

  // 生体認証データの検証
  verifyBiometricData(id: string): boolean {
    const data = this.biometricData.get(id)
    if (!data) return false

    data.isVerified = true
    data.verificationDate = new Date()
    
    return true
  }

  // 統計情報の取得
  getStatistics(): {
    totalRecords: number
    fingerprints: number
    palmPrints: number
    verifiedRecords: number
    averageQuality: number
  } {
    const records = Array.from(this.biometricData.values())
    const fingerprints = records.filter(r => r.type === BiometricType.FINGERPRINT).length
    const palmPrints = records.filter(r => r.type === BiometricType.PALM_PRINT).length
    const verifiedRecords = records.filter(r => r.isVerified).length
    const averageQuality = records.length > 0 
      ? records.reduce((sum, r) => sum + r.quality, 0) / records.length 
      : 0

    return {
      totalRecords: records.length,
      fingerprints,
      palmPrints,
      verifiedRecords,
      averageQuality: Math.round(averageQuality * 100) / 100
    }
  }

  // データのエクスポート
  exportBiometricData(): string {
    const data = Array.from(this.biometricData.values())
    return JSON.stringify(data, null, 2)
  }

  // データのインポート
  importBiometricData(dataString: string): void {
    try {
      const data: BiometricData[] = JSON.parse(dataString)
      data.forEach(item => {
        item.captureDate = new Date(item.captureDate)
        if (item.verificationDate) {
          item.verificationDate = new Date(item.verificationDate)
        }
        this.biometricData.set(item.id, item)
        if (item.template) {
          this.templates.set(item.id, item.template)
        }
      })
    } catch (error) {
      console.error('生体認証データのインポートに失敗しました:', error)
    }
  }

  // 画像ファイルの処理
  private async processImageFile(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result)
        } else {
          reject(new Error('画像の読み込みに失敗しました'))
        }
      }
      reader.onerror = () => reject(new Error('画像の読み込みに失敗しました'))
      reader.readAsDataURL(file)
    })
  }

  // 指紋テンプレートの生成（模擬実装）
  private async generateFingerprintTemplate(imageData: string): Promise<string> {
    // 実際の実装では、専用の指紋認識ライブラリを使用
    // ここでは模擬的なテンプレートを生成
    const hash = btoa(imageData).substring(0, 100)
    return `fingerprint_template_${hash}_${Date.now()}`
  }

  // 掌紋テンプレートの生成（模擬実装）
  private async generatePalmPrintTemplate(imageData: string): Promise<string> {
    // 実際の実装では、専用の掌紋認識ライブラリを使用
    const hash = btoa(imageData).substring(0, 100)
    return `palmprint_template_${hash}_${Date.now()}`
  }

  // 品質スコアの計算（模擬実装）
  private calculateQuality(imageData: string): number {
    // 実際の実装では、画像の品質を分析
    // ここではランダムな品質スコアを生成
    return Math.floor(Math.random() * 30) + 70 // 70-100の範囲
  }

  // テンプレートの比較（模擬実装）
  private async compareTemplates(template1: string, template2: string): Promise<number> {
    // 実際の実装では、テンプレート間の類似度を計算
    // ここでは模擬的な類似度スコアを生成
    const similarity = Math.random()
    return similarity
  }

  // 生体認証IDの生成
  private generateBiometricId(): string {
    return `bio_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
}

// グローバル生体認証管理インスタンス
export const globalBiometricManager = new BiometricManager()

// 生体認証の品質レベル
export const QUALITY_LEVELS = {
  EXCELLENT: { min: 90, label: '優秀' },
  GOOD: { min: 80, label: '良好' },
  FAIR: { min: 70, label: '普通' },
  POOR: { min: 0, label: '不良' }
}

// サポートされている画像形式
export const SUPPORTED_IMAGE_TYPES = [
  'image/jpeg',
  'image/png',
  'image/gif',
  'image/bmp'
]

// 最大画像サイズ（MB）
export const MAX_IMAGE_SIZE_MB = 10
