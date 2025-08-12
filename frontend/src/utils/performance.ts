/**
 * パフォーマンス最適化ユーティリティ
 * 不動産管理システムの利用性能向上のための機能を提供
 */

// デバウンス関数 - 連続した関数呼び出しを制限
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  
  return (...args: Parameters<T>) => {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

// スロットリング関数 - 関数呼び出しの頻度を制限
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean = false
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 仮想スクロール用のアイテム計算
export function calculateVisibleItems(
  scrollTop: number,
  containerHeight: number,
  itemHeight: number,
  totalItems: number
): { start: number; end: number } {
  const start = Math.floor(scrollTop / itemHeight)
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  const end = Math.min(start + visibleCount + 1, totalItems)
  
  return { start, end }
}

// メモリ使用量の監視
export function monitorMemoryUsage(): void {
  if ('memory' in performance) {
    const memory = (performance as any).memory
    const used = Math.round(memory.usedJSHeapSize / 1048576 * 100) / 100
    const total = Math.round(memory.totalJSHeapSize / 1048576 * 100) / 100
    
    if (used > total * 0.8) {
      console.warn('メモリ使用量が高いです:', `${used}MB / ${total}MB`)
    }
  }
}

// 画像の遅延読み込み
export function lazyLoadImage(img: HTMLImageElement, src: string): void {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        img.src = src
        observer.unobserve(img)
      }
    })
  })
  
  observer.observe(img)
}

// パフォーマンス測定
export function measurePerformance<T>(
  name: string,
  fn: () => T
): T {
  const start = performance.now()
  const result = fn()
  const end = performance.now()
  
  console.log(`${name} 実行時間: ${(end - start).toFixed(2)}ms`)
  return result
}

// バッチ処理 - 複数の処理をまとめて実行
export function batchProcess<T>(
  items: T[],
  processFn: (item: T) => Promise<void>,
  batchSize: number = 10
): Promise<void> {
  return new Promise(async (resolve) => {
    for (let i = 0; i < items.length; i += batchSize) {
      const batch = items.slice(i, i + batchSize)
      await Promise.all(batch.map(processFn))
      
      // 次のバッチの前に少し待機（UIブロッキング防止）
      if (i + batchSize < items.length) {
        await new Promise(resolve => setTimeout(resolve, 0))
      }
    }
    resolve()
  })
}

// キャッシュ管理
export class CacheManager {
  private cache = new Map<string, { data: any; timestamp: number; ttl: number }>()
  
  set(key: string, data: any, ttl: number = 5 * 60 * 1000): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }
  
  get(key: string): any | null {
    const item = this.cache.get(key)
    if (!item) return null
    
    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(key)
      return null
    }
    
    return item.data
  }
  
  clear(): void {
    this.cache.clear()
  }
  
  size(): number {
    return this.cache.size
  }
}

// グローバルキャッシュインスタンス
export const globalCache = new CacheManager()
