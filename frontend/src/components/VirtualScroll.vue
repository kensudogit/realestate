<template>
  <div 
    ref="containerRef"
    class="virtual-scroll-container"
    @scroll="handleScroll"
    @resize="handleResize"
  >
    <div 
      class="virtual-scroll-spacer"
      :style="{ height: `${totalHeight}px` }"
    />
    <div 
      class="virtual-scroll-content"
      :style="{ transform: `translateY(${offsetY}px)` }"
    >
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item)"
        class="virtual-scroll-item"
        :style="{ height: `${itemHeight}px` }"
      >
        <slot :item="item" :index="getItemIndex(item)">
          {{ item }}
        </slot>
      </div>
    </div>
    
    <!-- ローディングインジケーター -->
    <div v-if="loading" class="virtual-scroll-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      読み込み中...
    </div>
    
    <!-- エラー表示 -->
    <div v-if="error" class="virtual-scroll-error">
      <el-icon><Warning /></el-icon>
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Loading, Warning } from '@element-plus/icons-vue'

interface VirtualScrollProps {
  items: any[]
  itemHeight: number
  containerHeight?: number
  overscan?: number
  getItemKey?: (item: any) => string | number
  loading?: boolean
  error?: string | null
}

const props = withDefaults(defineProps<VirtualScrollProps>(), {
  containerHeight: 400,
  overscan: 5,
  getItemKey: (item: any) => item.id || item
})

const emit = defineEmits<{
  'scroll': [event: Event]
  'resize': [event: Event]
  'item-visible': [item: any, index: number]
  'load-more': []
}>()

// コンテナ参照
const containerRef = ref<HTMLElement>()

// スクロール状態
const scrollTop = ref(0)
const containerHeight = ref(props.containerHeight)

// 仮想スクロール計算
const totalHeight = computed(() => props.items.length * props.itemHeight)
const visibleCount = computed(() => Math.ceil(containerHeight.value / props.itemHeight) + props.overscan * 2)
const startIndex = computed(() => Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.overscan))
const endIndex = computed(() => Math.min(props.items.length, startIndex.value + visibleCount.value))
const offsetY = computed(() => startIndex.value * props.itemHeight)

// 表示するアイテム
const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value)
})

// スクロールハンドラー
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
  
  // スクロールイベントを発火
  emit('scroll', event)
  
  // 下部に近づいたら読み込みを要求
  const scrollBottom = target.scrollTop + target.clientHeight
  if (scrollBottom >= totalHeight.value - 100) {
    emit('load-more')
  }
}

// リサイズハンドラー
const handleResize = (event: Event) => {
  if (containerRef.value) {
    containerHeight.value = containerRef.value.clientHeight
  }
  emit('resize', event)
}

// アイテムのインデックスを取得
const getItemIndex = (item: any) => {
  return props.items.indexOf(item)
}

// アイテムが表示されているかチェック
const isItemVisible = (index: number) => {
  return index >= startIndex.value && index < endIndex.value
}

// 特定のアイテムまでスクロール
const scrollToItem = (index: number, behavior: ScrollBehavior = 'smooth') => {
  if (!containerRef.value) return
  
  const targetScrollTop = index * props.itemHeight
  containerRef.value.scrollTo({
    top: targetScrollTop,
    behavior
  })
}

// 特定のアイテムを表示
const scrollToItemById = (id: string | number, behavior: ScrollBehavior = 'smooth') => {
  const index = props.items.findIndex(item => props.getItemKey(item) === id)
  if (index !== -1) {
    scrollToItem(index, behavior)
  }
}

// スクロール位置をリセット
const resetScroll = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
    scrollTop.value = 0
  }
}

// リサイズオブザーバー
let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  if (containerRef.value) {
    containerHeight.value = containerRef.value.clientHeight
    
    // リサイズオブザーバーを設定
    resizeObserver = new ResizeObserver(() => {
      if (containerRef.value) {
        containerHeight.value = containerRef.value.clientHeight
      }
    })
    resizeObserver.observe(containerRef.value)
  }
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})

// アイテムの変更を監視
watch(() => props.items, () => {
  nextTick(() => {
    // アイテムが変更されたら、表示されているアイテムの可視性をチェック
    visibleItems.value.forEach((item, index) => {
      const globalIndex = startIndex.value + index
      if (isItemVisible(globalIndex)) {
        emit('item-visible', item, globalIndex)
      }
    })
  })
}, { deep: true })

// 外部から呼び出せるメソッド
defineExpose({
  scrollToItem,
  scrollToItemById,
  resetScroll,
  scrollTop,
  containerHeight
})
</script>

<style scoped>
.virtual-scroll-container {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  height: v-bind(containerHeight + 'px');
}

.virtual-scroll-spacer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  pointer-events: none;
}

.virtual-scroll-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  will-change: transform;
}

.virtual-scroll-item {
  width: 100%;
  box-sizing: border-box;
}

.virtual-scroll-loading {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-size: 14px;
  color: #666;
}

.virtual-scroll-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
  border-radius: 8px;
  color: #e74c3c;
  font-size: 14px;
}

/* スクロールバーのスタイリング */
.virtual-scroll-container::-webkit-scrollbar {
  width: 8px;
}

.virtual-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ダークモード対応 */
.dark-mode .virtual-scroll-container::-webkit-scrollbar-track {
  background: #2d3748;
}

.dark-mode .virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #4a5568;
}

.dark-mode .virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #718096;
}

.dark-mode .virtual-scroll-loading {
  background: rgba(44, 62, 80, 0.9);
  color: #ecf0f1;
}

.dark-mode .virtual-scroll-error {
  background: rgba(245, 108, 108, 0.2);
  border-color: rgba(245, 108, 108, 0.5);
  color: #fca5a5;
}
</style>
