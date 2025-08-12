import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { debounce, throttle, globalCache, measurePerformance } from '@/utils/performance'

/**
 * 高性能データ管理フック
 * 仮想スクロール、キャッシュ、遅延読み込みなどの機能を提供
 */
export function usePerformanceData<T>(
  fetchFn: (params?: any) => Promise<T[]>,
  options: {
    pageSize?: number
    cacheKey?: string
    enableVirtualScroll?: boolean
    debounceDelay?: number
    throttleDelay?: number
  } = {}
) {
  const {
    pageSize = 50,
    cacheKey = 'default',
    enableVirtualScroll = false,
    debounceDelay = 300,
    throttleDelay = 100
  } = options

  // 状態管理
  const data = ref<T[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const totalCount = ref(0)
  const currentPage = ref(1)
  const hasMore = ref(true)

  // 仮想スクロール用
  const scrollTop = ref(0)
  const containerHeight = ref(0)
  const itemHeight = ref(60) // デフォルトのアイテム高さ

  // キャッシュキー
  const getCacheKey = (params?: any) => {
    const key = params ? `${cacheKey}_${JSON.stringify(params)}` : cacheKey
    return key
  }

  // データ取得（キャッシュ付き）
  const fetchData = async (params?: any, useCache = true) => {
    const cacheKey = getCacheKey(params)
    
    // キャッシュから取得を試行
    if (useCache) {
      const cached = globalCache.get(cacheKey)
      if (cached) {
        data.value = cached
        totalCount.value = cached.length
        return cached
      }
    }

    try {
      loading.value = true
      error.value = null
      
      const result = await measurePerformance('データ取得', () => fetchFn(params))
      
      if (Array.isArray(result)) {
        data.value = result
        totalCount.value = result.length
        hasMore.value = result.length >= pageSize
        
        // キャッシュに保存
        globalCache.set(cacheKey, result, 5 * 60 * 1000) // 5分間キャッシュ
        
        return result
      } else {
        throw new Error('データ形式が不正です')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'データ取得に失敗しました'
      console.error('データ取得エラー:', err)
      return []
    } finally {
      loading.value = false
    }
  }

  // ページネーション付きデータ取得
  const fetchPage = async (page: number, params?: any) => {
    currentPage.value = page
    const offset = (page - 1) * pageSize
    
    const pageParams = {
      ...params,
      offset,
      limit: pageSize
    }
    
    return await fetchData(pageParams, false) // ページネーションはキャッシュしない
  }

  // 次のページを読み込み
  const loadMore = async () => {
    if (loading.value || !hasMore.value) return
    
    const nextPage = currentPage.value + 1
    const newData = await fetchPage(nextPage)
    
    if (newData.length > 0) {
      data.value.push(...newData)
      currentPage.value = nextPage
      hasMore.value = newData.length >= pageSize
    } else {
      hasMore.value = false
    }
  }

  // 検索（デバウンス付き）
  const searchQuery = ref('')
  const searchResults = ref<T[]>([])
  const isSearching = ref(false)

  const debouncedSearch = debounce(async (query: string) => {
    if (!query.trim()) {
      searchResults.value = []
      isSearching.value = false
      return
    }

    try {
      isSearching.value = true
      const results = await fetchData({ search: query }, false)
      searchResults.value = results
    } catch (err) {
      console.error('検索エラー:', err)
      searchResults.value = []
    } finally {
      isSearching.value = false
    }
  }, debounceDelay)

  const search = (query: string) => {
    searchQuery.value = query
    debouncedSearch(query)
  }

  // フィルタリング（スロットリング付き）
  const filterParams = ref<Record<string, any>>({})
  
  const throttledFilter = throttle(async (params: Record<string, any>) => {
    try {
      const results = await fetchData(params, false)
      data.value = results
      totalCount.value = results.length
    } catch (err) {
      console.error('フィルタリングエラー:', err)
    }
  }, throttleDelay)

  const filter = (params: Record<string, any>) => {
    filterParams.value = { ...filterParams.value, ...params }
    throttledFilter(filterParams.value)
  }

  // 仮想スクロール用の表示アイテム計算
  const visibleItems = computed(() => {
    if (!enableVirtualScroll) return data.value
    
    const { start, end } = calculateVisibleItems(
      scrollTop.value,
      containerHeight.value,
      itemHeight.value,
      data.value.length
    )
    
    return data.value.slice(start, end)
  })

  // スクロール位置の更新
  const updateScrollPosition = (top: number, height: number) => {
    scrollTop.value = top
    containerHeight.value = height
  }

  // アイテム高さの更新
  const updateItemHeight = (height: number) => {
    itemHeight.value = height
  }

  // データの一括更新
  const batchUpdate = async (updates: Array<{ id: string | number; data: Partial<T> }>) => {
    const updatedData = [...data.value]
    
    for (const update of updates) {
      const index = updatedData.findIndex((item: any) => item.id === update.id)
      if (index !== -1) {
        updatedData[index] = { ...updatedData[index], ...update.data }
      }
    }
    
    data.value = updatedData
    
    // キャッシュを更新
    const cacheKey = getCacheKey()
    globalCache.set(cacheKey, updatedData, 5 * 60 * 1000)
  }

  // データの一括削除
  const batchDelete = async (ids: Array<string | number>) => {
    const filteredData = data.value.filter((item: any) => !ids.includes(item.id))
    data.value = filteredData
    totalCount.value = filteredData.length
    
    // キャッシュを更新
    const cacheKey = getCacheKey()
    globalCache.set(cacheKey, filteredData, 5 * 60 * 1000)
  }

  // キャッシュのクリア
  const clearCache = () => {
    globalCache.clear()
  }

  // 初期化
  onMounted(async () => {
    await fetchData()
  })

  // クリーンアップ
  onUnmounted(() => {
    // 必要に応じてリソースをクリーンアップ
  })

  return {
    // 状態
    data,
    loading,
    error,
    totalCount,
    currentPage,
    hasMore,
    searchQuery,
    searchResults,
    isSearching,
    filterParams,
    visibleItems,
    
    // メソッド
    fetchData,
    fetchPage,
    loadMore,
    search,
    filter,
    batchUpdate,
    batchDelete,
    clearCache,
    updateScrollPosition,
    updateItemHeight
  }
}

// 仮想スクロール用のヘルパー関数
function calculateVisibleItems(
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
