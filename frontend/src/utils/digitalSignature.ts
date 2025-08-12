/**
 * 電子署名・スタンプ・押印機能ユーティリティ
 * 不動産売買契約の電子書類管理に必要な機能を提供
 */

import CryptoJS from 'crypto-js'

// 電子署名の種類
export enum SignatureType {
  DIGITAL_SIGNATURE = 'DIGITAL_SIGNATURE',  // 電子署名
  ELECTRONIC_STAMP = 'ELECTRONIC_STAMP',    // 電子スタンプ
  DIGITAL_SEAL = 'DIGITAL_SEAL'            // 電子押印
}

// 署名情報のインターフェース
export interface SignatureInfo {
  id: string
  type: SignatureType
  signerId: string
  signerName: string
  timestamp: Date
  position: { x: number; y: number }
  documentId: string
  hash: string
  certificate?: string
  imageData?: string
}

// 電子署名クラス
export class DigitalSignature {
  private privateKey: string
  private publicKey: string

  constructor(privateKey?: string, publicKey?: string) {
    this.privateKey = privateKey || this.generateKeyPair().privateKey
    this.publicKey = publicKey || this.generateKeyPair().publicKey
  }

  // キーペアの生成
  private generateKeyPair() {
    // 実際の実装では、より安全な暗号化ライブラリを使用
    const privateKey = CryptoJS.lib.WordArray.random(32).toString()
    const publicKey = CryptoJS.SHA256(privateKey).toString()
    
    return { privateKey, publicKey }
  }

  // 文書のハッシュ化
  private createDocumentHash(content: string): string {
    return CryptoJS.SHA256(content).toString()
  }

  // 電子署名の作成
  createSignature(
    documentContent: string,
    signerId: string,
    signerName: string,
    position: { x: number; y: number },
    documentId: string
  ): SignatureInfo {
    const hash = this.createDocumentHash(documentContent)
    const signature = CryptoJS.HmacSHA256(hash, this.privateKey).toString()
    
    return {
      id: this.generateSignatureId(),
      type: SignatureType.DIGITAL_SIGNATURE,
      signerId,
      signerName,
      timestamp: new Date(),
      position,
      documentId,
      hash: signature,
      certificate: this.publicKey
    }
  }

  // 署名の検証
  verifySignature(signatureInfo: SignatureInfo, documentContent: string): boolean {
    const expectedHash = this.createDocumentHash(documentContent)
    const expectedSignature = CryptoJS.HmacSHA256(expectedHash, this.privateKey).toString()
    
    return signatureInfo.hash === expectedSignature
  }

  // 署名IDの生成
  private generateSignatureId(): string {
    return `sig_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  // 公開鍵の取得
  getPublicKey(): string {
    return this.publicKey
  }
}

// 電子スタンプクラス
export class ElectronicStamp {
  private stampImage: string
  private stampId: string

  constructor(stampImage?: string) {
    this.stampImage = stampImage || this.createDefaultStamp()
    this.stampId = this.generateStampId()
  }

  // デフォルトスタンプの作成
  private createDefaultStamp(): string {
    // 基本的な円形スタンプのSVGを作成
    return `
      <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
        <circle cx="50" cy="50" r="45" fill="none" stroke="#e74c3c" stroke-width="3"/>
        <text x="50" y="55" text-anchor="middle" font-size="12" fill="#e74c3c">承認</text>
      </svg>
    `
  }

  // スタンプの適用
  applyStamp(
    documentId: string,
    position: { x: number; y: number },
    signerId: string,
    signerName: string
  ): SignatureInfo {
    return {
      id: this.generateStampId(),
      type: SignatureType.ELECTRONIC_STAMP,
      signerId,
      signerName,
      timestamp: new Date(),
      position,
      documentId,
      hash: CryptoJS.SHA256(`${documentId}_${position.x}_${position.y}_${Date.now()}`).toString(),
      imageData: this.stampImage
    }
  }

  // スタンプIDの生成
  private generateStampId(): string {
    return `stamp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  // スタンプ画像の取得
  getStampImage(): string {
    return this.stampImage
  }

  // カスタムスタンプの設定
  setCustomStamp(svgContent: string) {
    this.stampImage = svgContent
  }
}

// 電子押印クラス
export class DigitalSeal {
  private sealImage: string
  private sealId: string

  constructor(sealImage?: string) {
    this.sealImage = sealImage || this.createDefaultSeal()
    this.sealId = this.generateSealId()
  }

  // デフォルト押印の作成
  private createDefaultSeal(): string {
    // 基本的な角印のSVGを作成
    return `
      <svg width="80" height="80" xmlns="http://www.w3.org/2000/svg">
        <rect x="10" y="10" width="60" height="60" fill="none" stroke="#2c3e50" stroke-width="2"/>
        <text x="40" y="35" text-anchor="middle" font-size="10" fill="#2c3e50">印</text>
        <text x="40" y="50" text-anchor="middle" font-size="8" fill="#2c3e50">押印</text>
      </svg>
    `
  }

  // 押印の適用
  applySeal(
    documentId: string,
    position: { x: number; y: number },
    signerId: string,
    signerName: string
  ): SignatureInfo {
    return {
      id: this.generateSealId(),
      type: SignatureType.DIGITAL_SEAL,
      signerId,
      signerName,
      timestamp: new Date(),
      position,
      documentId,
      hash: CryptoJS.SHA256(`${documentId}_${position.x}_${position.y}_${Date.now()}`).toString(),
      imageData: this.sealImage
    }
  }

  // 押印IDの生成
  private generateSealId(): string {
    return `seal_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  // 押印画像の取得
  getSealImage(): string {
    return this.sealImage
  }

  // カスタム押印の設定
  setCustomSeal(svgContent: string) {
    this.sealImage = svgContent
  }
}

// 署名管理クラス
export class SignatureManager {
  private signatures: Map<string, SignatureInfo> = new Map()
  private digitalSignature: DigitalSignature
  private electronicStamp: ElectronicStamp
  private digitalSeal: DigitalSeal

  constructor() {
    this.digitalSignature = new DigitalSignature()
    this.electronicStamp = new ElectronicStamp()
    this.digitalSeal = new DigitalSeal()
  }

  // 署名の追加
  addSignature(signature: SignatureInfo): void {
    this.signatures.set(signature.id, signature)
  }

  // 署名の取得
  getSignature(id: string): SignatureInfo | undefined {
    return this.signatures.get(id)
  }

  // 文書の署名一覧を取得
  getDocumentSignatures(documentId: string): SignatureInfo[] {
    return Array.from(this.signatures.values())
      .filter(sig => sig.documentId === documentId)
      .sort((a, b) => a.timestamp.getTime() - b.timestamp.getTime())
  }

  // 署名の削除
  removeSignature(id: string): boolean {
    return this.signatures.delete(id)
  }

  // 署名の検証
  verifySignature(id: string, documentContent: string): boolean {
    const signature = this.signatures.get(id)
    if (!signature) return false

    return this.digitalSignature.verifySignature(signature, documentContent)
  }

  // 電子署名の作成
  createDigitalSignature(
    documentContent: string,
    signerId: string,
    signerName: string,
    position: { x: number; y: number },
    documentId: string
  ): SignatureInfo {
    const signature = this.digitalSignature.createSignature(
      documentContent,
      signerId,
      signerName,
      position,
      documentId
    )
    this.addSignature(signature)
    return signature
  }

  // 電子スタンプの適用
  applyElectronicStamp(
    documentId: string,
    position: { x: number; y: number },
    signerId: string,
    signerName: string
  ): SignatureInfo {
    const stamp = this.electronicStamp.applyStamp(
      documentId,
      position,
      signerId,
      signerName
    )
    this.addSignature(stamp)
    return stamp
  }

  // 電子押印の適用
  applyDigitalSeal(
    documentId: string,
    position: { x: number; y: number },
    signerId: string,
    signerName: string
  ): SignatureInfo {
    const seal = this.digitalSeal.applySeal(
      documentId,
      position,
      signerId,
      signerName
    )
    this.addSignature(seal)
    return seal
  }

  // 署名履歴のエクスポート
  exportSignatureHistory(documentId: string): string {
    const signatures = this.getDocumentSignatures(documentId)
    return JSON.stringify(signatures, null, 2)
  }

  // 署名履歴のインポート
  importSignatureHistory(historyData: string): void {
    try {
      const signatures: SignatureInfo[] = JSON.parse(historyData)
      signatures.forEach(sig => {
        sig.timestamp = new Date(sig.timestamp)
        this.addSignature(sig)
      })
    } catch (error) {
      console.error('署名履歴のインポートに失敗しました:', error)
    }
  }
}

// グローバル署名管理インスタンス
export const globalSignatureManager = new SignatureManager()
