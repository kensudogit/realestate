/**
 * 電子書類管理機能
 * 不動産売買契約の電子書類のアップロード・ダウンロード・管理を提供
 */

// 書類の種類
export enum DocumentType {
  CONTRACT = 'CONTRACT',                    // 売買契約書
  PROPERTY_REGISTRATION = 'PROPERTY_REGISTRATION', // 不動産登記簿謄本
  SURVEY_REPORT = 'SURVEY_REPORT',         // 測量図
  BUILDING_CERTIFICATE = 'BUILDING_CERTIFICATE', // 建築確認書
  TAX_CERTIFICATE = 'TAX_CERTIFICATE',     // 固定資産税評価証明書
  INSURANCE_POLICY = 'INSURANCE_POLICY',   // 火災保険証券
  OTHER = 'OTHER'                          // その他
}

// 書類のステータス
export enum DocumentStatus {
  DRAFT = 'DRAFT',           // 下書き
  PENDING_REVIEW = 'PENDING_REVIEW', // 審査中
  APPROVED = 'APPROVED',     // 承認済み
  REJECTED = 'REJECTED',     // 却下
  ARCHIVED = 'ARCHIVED'      // アーカイブ
}

// 書類情報のインターフェース
export interface DocumentInfo {
  id: string
  name: string
  type: DocumentType
  status: DocumentStatus
  contractId?: string
  propertyId?: string
  clientId?: string
  fileName: string
  originalFileName: string
  fileSize: number
  mimeType: string
  uploadDate: Date
  uploadedBy: string
  description?: string
  tags: string[]
  version: number
  isSigned: boolean
  signatureIds: string[]
  metadata: Record<string, any>
}

// 書類管理クラス
export class DocumentManager {
  private documents: Map<string, DocumentInfo> = new Map()
  private fileStorage: Map<string, ArrayBuffer> = new Map()

  // 書類のアップロード
  async uploadDocument(
    file: File,
    documentInfo: Omit<DocumentInfo, 'id' | 'fileName' | 'fileSize' | 'mimeType' | 'uploadDate' | 'version'>
  ): Promise<DocumentInfo> {
    try {
      // ファイルの読み込み
      const arrayBuffer = await file.arrayBuffer()
      
      // 書類情報の作成
      const document: DocumentInfo = {
        ...documentInfo,
        id: this.generateDocumentId(),
        fileName: `${documentInfo.id || this.generateDocumentId()}_${Date.now()}`,
        fileSize: file.size,
        mimeType: file.type,
        uploadDate: new Date(),
        version: 1,
        isSigned: false,
        signatureIds: [],
        metadata: {}
      }

      // ストレージに保存
      this.documents.set(document.id, document)
      this.fileStorage.set(document.id, arrayBuffer)

      return document
    } catch (error) {
      console.error('書類のアップロードに失敗しました:', error)
      throw new Error('書類のアップロードに失敗しました')
    }
  }

  // 書類のダウンロード
  async downloadDocument(documentId: string): Promise<{ document: DocumentInfo; blob: Blob } | null> {
    const document = this.documents.get(documentId)
    if (!document) return null

    const fileData = this.fileStorage.get(documentId)
    if (!fileData) return null

    const blob = new Blob([fileData], { type: document.mimeType })
    return { document, blob }
  }

  // 書類の取得
  getDocument(documentId: string): DocumentInfo | undefined {
    return this.documents.get(documentId)
  }

  // 契約に関連する書類一覧を取得
  getContractDocuments(contractId: string): DocumentInfo[] {
    return Array.from(this.documents.values())
      .filter(doc => doc.contractId === contractId)
      .sort((a, b) => b.uploadDate.getTime() - a.uploadDate.getTime())
  }

  // 物件に関連する書類一覧を取得
  getPropertyDocuments(propertyId: string): DocumentInfo[] {
    return Array.from(this.documents.values())
      .filter(doc => doc.propertyId === propertyId)
      .sort((a, b) => b.uploadDate.getTime() - a.uploadDate.getTime())
  }

  // 顧客に関連する書類一覧を取得
  getClientDocuments(clientId: string): DocumentInfo[] {
    return Array.from(this.documents.values())
      .filter(doc => doc.clientId === clientId)
      .sort((a, b) => b.uploadDate.getTime() - a.uploadDate.getTime())
  }

  // 書類の検索
  searchDocuments(query: string, filters?: Partial<DocumentInfo>): DocumentInfo[] {
    const searchTerm = query.toLowerCase()
    
    return Array.from(this.documents.values())
      .filter(doc => {
        // 基本検索
        const matchesQuery = 
          doc.name.toLowerCase().includes(searchTerm) ||
          doc.description?.toLowerCase().includes(searchTerm) ||
          doc.tags.some(tag => tag.toLowerCase().includes(searchTerm))

        if (!matchesQuery) return false

        // フィルター適用
        if (filters) {
          if (filters.type && doc.type !== filters.type) return false
          if (filters.status && doc.status !== filters.status) return false
          if (filters.contractId && doc.contractId !== filters.contractId) return false
          if (filters.propertyId && doc.propertyId !== filters.propertyId) return false
          if (filters.clientId && doc.clientId !== filters.clientId) return false
        }

        return true
      })
      .sort((a, b) => b.uploadDate.getTime() - a.uploadDate.getTime())
  }

  // 書類の更新
  updateDocument(documentId: string, updates: Partial<DocumentInfo>): DocumentInfo | null {
    const document = this.documents.get(documentId)
    if (!document) return null

    const updatedDocument = { ...document, ...updates }
    this.documents.set(documentId, updatedDocument)

    return updatedDocument
  }

  // 書類の削除
  deleteDocument(documentId: string): boolean {
    const deleted = this.documents.delete(documentId)
    if (deleted) {
      this.fileStorage.delete(documentId)
    }
    return deleted
  }

  // 書類の署名状態を更新
  updateDocumentSignature(documentId: string, signatureIds: string[]): boolean {
    const document = this.documents.get(documentId)
    if (!document) return false

    document.isSigned = signatureIds.length > 0
    document.signatureIds = signatureIds
    document.version += 1

    return true
  }

  // 書類のステータスを更新
  updateDocumentStatus(documentId: string, status: DocumentStatus): boolean {
    const document = this.documents.get(documentId)
    if (!document) return false

    document.status = status
    document.version += 1

    return true
  }

  // 書類のバージョン管理
  createDocumentVersion(documentId: string, newFile: File): DocumentInfo | null {
    const originalDocument = this.documents.get(documentId)
    if (!originalDocument) return null

    const newDocument: DocumentInfo = {
      ...originalDocument,
      id: this.generateDocumentId(),
      fileName: `${originalDocument.id}_v${originalDocument.version + 1}_${Date.now()}`,
      fileSize: newFile.size,
      mimeType: newFile.type,
      uploadDate: new Date(),
      version: originalDocument.version + 1,
      isSigned: false,
      signatureIds: []
    }

    // 新しいファイルをストレージに保存
    newFile.arrayBuffer().then(arrayBuffer => {
      this.fileStorage.set(newDocument.id, arrayBuffer)
    })

    this.documents.set(newDocument.id, newDocument)
    return newDocument
  }

  // 書類のエクスポート（メタデータ）
  exportDocumentMetadata(documentId: string): string {
    const document = this.documents.get(documentId)
    if (!document) return ''

    return JSON.stringify(document, null, 2)
  }

  // 書類の一括エクスポート
  exportAllDocuments(): string {
    const documents = Array.from(this.documents.values())
    return JSON.stringify(documents, null, 2)
  }

  // 書類の一括インポート
  importDocuments(documentsData: string): void {
    try {
      const documents: DocumentInfo[] = JSON.parse(documentsData)
      documents.forEach(doc => {
        doc.uploadDate = new Date(doc.uploadDate)
        this.documents.set(doc.id, doc)
      })
    } catch (error) {
      console.error('書類のインポートに失敗しました:', error)
    }
  }

  // ストレージ使用量の取得
  getStorageUsage(): { totalDocuments: number; totalSize: number } {
    let totalSize = 0
    this.fileStorage.forEach(buffer => {
      totalSize += buffer.byteLength
    })

    return {
      totalDocuments: this.documents.size,
      totalSize
    }
  }

  // 古い書類のクリーンアップ
  cleanupOldDocuments(daysOld: number): number {
    const cutoffDate = new Date()
    cutoffDate.setDate(cutoffDate.getDate() - daysOld)

    let deletedCount = 0
    for (const [id, document] of this.documents.entries()) {
      if (document.uploadDate < cutoffDate && document.status === DocumentStatus.ARCHIVED) {
        this.deleteDocument(id)
        deletedCount++
      }
    }

    return deletedCount
  }

  // 書類IDの生成
  private generateDocumentId(): string {
    return `doc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
}

// グローバル書類管理インスタンス
export const globalDocumentManager = new DocumentManager()

// ファイル形式の検証
export const validateFileFormat = (file: File, allowedTypes: string[]): boolean => {
  return allowedTypes.includes(file.type)
}

// ファイルサイズの検証
export const validateFileSize = (file: File, maxSizeMB: number): boolean => {
  const maxSizeBytes = maxSizeMB * 1024 * 1024
  return file.size <= maxSizeBytes
}

// サポートされているファイル形式
export const SUPPORTED_FILE_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'image/jpeg',
  'image/png',
  'image/gif',
  'text/plain',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
]

// 最大ファイルサイズ（MB）
export const MAX_FILE_SIZE_MB = 50
