// AI运动助手 - 性能优化工具

/**
 * 防抖函数 - 防止频繁调用
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 */
export function debounce(func, wait, immediate = false) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func.apply(this, args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func.apply(this, args)
  }
}

/**
 * 节流函数 - 限制函数调用频率
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 限制时间（毫秒）
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 延迟加载图片
 * @param {string} src - 图片URL
 * @param {HTMLElement} placeholder - 占位元素
 */
export function lazyLoadImage(src, placeholder) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => {
      placeholder.src = src
      placeholder.classList.add('loaded')
      resolve(img)
    }
    img.onerror = reject
    img.src = src
  })
}

/**
 * 检查网络连接状态
 */
export function checkNetworkStatus() {
  return {
    online: navigator.onLine,
    connection: navigator.connection || navigator.mozConnection || navigator.webkitConnection,
    effectiveType: navigator.connection?.effectiveType || 'unknown'
  }
}

/**
 * 性能监控
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {
      pageLoadTime: 0,
      domContentLoaded: 0,
      firstContentfulPaint: 0,
      largestContentfulPaint: 0,
      cumulativeLayoutShift: 0,
      firstInputDelay: 0
    }
    this.init()
  }

  init() {
    // 页面加载时间
    window.addEventListener('load', () => {
      this.metrics.pageLoadTime = performance.now()
    })

    // DOM内容加载时间
    document.addEventListener('DOMContentLoaded', () => {
      this.metrics.domContentLoaded = performance.now()
    })

    // 获取 Web Vitals
    this.measureWebVitals()
  }

  measureWebVitals() {
    // First Contentful Paint
    new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        if (entry.name === 'first-contentful-paint') {
          this.metrics.firstContentfulPaint = entry.startTime
        }
      }
    }).observe({ entryTypes: ['paint'] })

    // Largest Contentful Paint
    new PerformanceObserver((entryList) => {
      const entries = entryList.getEntries()
      const lastEntry = entries[entries.length - 1]
      this.metrics.largestContentfulPaint = lastEntry.startTime
    }).observe({ entryTypes: ['largest-contentful-paint'] })

    // Cumulative Layout Shift
    new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        if (!entry.hadRecentInput) {
          this.metrics.cumulativeLayoutShift += entry.value
        }
      }
    }).observe({ entryTypes: ['layout-shift'] })

    // First Input Delay
    new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        this.metrics.firstInputDelay = entry.processingStart - entry.startTime
      }
    }).observe({ entryTypes: ['first-input'] })
  }

  getMetrics() {
    return { ...this.metrics }
  }

  logMetrics() {
    console.group('🚀 Performance Metrics')
    console.log('Page Load Time:', `${this.metrics.pageLoadTime.toFixed(2)}ms`)
    console.log('DOM Content Loaded:', `${this.metrics.domContentLoaded.toFixed(2)}ms`)
    console.log('First Contentful Paint:', `${this.metrics.firstContentfulPaint.toFixed(2)}ms`)
    console.log('Largest Contentful Paint:', `${this.metrics.largestContentfulPaint.toFixed(2)}ms`)
    console.log('Cumulative Layout Shift:', this.metrics.cumulativeLayoutShift.toFixed(4))
    console.log('First Input Delay:', `${this.metrics.firstInputDelay.toFixed(2)}ms`)
    console.groupEnd()
  }
}

/**
 * 内存使用监控
 */
export function getMemoryUsage() {
  if (performance.memory) {
    return {
      used: Math.round(performance.memory.usedJSHeapSize / 1048576), // MB
      total: Math.round(performance.memory.totalJSHeapSize / 1048576), // MB
      limit: Math.round(performance.memory.jsHeapSizeLimit / 1048576) // MB
    }
  }
  return null
}

/**
 * 虚拟滚动优化
 */
export class VirtualScroller {
  constructor(container, itemHeight, buffer = 5) {
    this.container = container
    this.itemHeight = itemHeight
    this.buffer = buffer
    this.visibleStart = 0
    this.visibleEnd = 0
    this.totalHeight = 0
  }

  calculateVisibleRange(scrollTop, containerHeight, totalItems) {
    const visibleStart = Math.max(0, Math.floor(scrollTop / this.itemHeight) - this.buffer)
    const visibleEnd = Math.min(
      totalItems - 1,
      Math.ceil((scrollTop + containerHeight) / this.itemHeight) + this.buffer
    )
    
    this.visibleStart = visibleStart
    this.visibleEnd = visibleEnd
    this.totalHeight = totalItems * this.itemHeight
    
    return { visibleStart, visibleEnd }
  }

  getTransform(index) {
    return `translateY(${(index - this.visibleStart) * this.itemHeight}px)`
  }
}

/**
 * 资源预加载
 */
export function preloadResource(url, type = 'fetch') {
  return new Promise((resolve, reject) => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.href = url
    
    switch (type) {
      case 'image':
        link.as = 'image'
        break
      case 'font':
        link.as = 'font'
        link.crossOrigin = 'anonymous'
        break
      case 'script':
        link.as = 'script'
        break
      default:
        link.as = 'fetch'
        link.crossOrigin = 'anonymous'
    }
    
    link.onload = resolve
    link.onerror = reject
    document.head.appendChild(link)
  })
}

/**
 * 批量处理优化
 */
export function batchProcess(items, batchSize = 100, processor) {
  return new Promise((resolve) => {
    const results = []
    let index = 0
    
    function processBatch() {
      const batch = items.slice(index, index + batchSize)
      batch.forEach(processor)
      results.push(...batch)
      index += batchSize
      
      if (index < items.length) {
        requestIdleCallback(processBatch)
      } else {
        resolve(results)
      }
    }
    
    processBatch()
  })
}

/**
 * 智能缓存管理
 */
export class SmartCache {
  constructor(maxSize = 100) {
    this.cache = new Map()
    this.maxSize = maxSize
    this.accessCount = new Map()
  }

  get(key) {
    if (this.cache.has(key)) {
      // 更新访问计数
      this.accessCount.set(key, (this.accessCount.get(key) || 0) + 1)
      return this.cache.get(key)
    }
    return null
  }

  set(key, value) {
    // 如果缓存已满，删除访问次数最少的项
    if (this.cache.size >= this.maxSize) {
      const leastUsed = [...this.accessCount.entries()]
        .sort((a, b) => a[1] - b[1])[0][0]
      this.cache.delete(leastUsed)
      this.accessCount.delete(leastUsed)
    }
    
    this.cache.set(key, value)
    this.accessCount.set(key, 1)
  }

  clear() {
    this.cache.clear()
    this.accessCount.clear()
  }

  size() {
    return this.cache.size
  }
}

/**
 * 初始化性能监控
 */
export function initPerformanceMonitoring() {
  const monitor = new PerformanceMonitor()
  
  // 在开发环境下输出性能指标
  if (process.env.NODE_ENV === 'development') {
    setTimeout(() => {
      monitor.logMetrics()
      
      const memory = getMemoryUsage()
      if (memory) {
        console.log('💾 Memory Usage:', `${memory.used}MB / ${memory.total}MB`)
      }
      
      const network = checkNetworkStatus()
      console.log('🌐 Network:', network.effectiveType, network.online ? 'Online' : 'Offline')
    }, 3000)
  }
  
  return monitor
}

// 导出默认实例
export const performanceMonitor = new PerformanceMonitor()
export const smartCache = new SmartCache()