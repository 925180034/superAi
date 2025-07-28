// AI运动助手 - 应用验证工具

/**
 * 验证应用完整性
 */
export function validateApplication() {
  const results = {
    passed: 0,
    failed: 0,
    warnings: 0,
    details: []
  }

  const tests = [
    {
      name: '检查必要的 DOM 元素',
      test: () => {
        const app = document.getElementById('app')
        return app !== null
      },
      critical: true
    },
    {
      name: '检查 CSS 变量',
      test: () => {
        const root = getComputedStyle(document.documentElement)
        const primaryColor = root.getPropertyValue('--primary-color')
        return primaryColor.trim() !== ''
      },
      critical: true
    },
    {
      name: '检查 FontAwesome 图标',
      test: () => {
        const faElements = document.querySelectorAll('[class*="fa-"]')
        return faElements.length > 0
      },
      critical: false
    },
    {
      name: '检查响应式断点',
      test: () => {
        const root = getComputedStyle(document.documentElement)
        const breakpointMd = root.getPropertyValue('--breakpoint-md')
        return breakpointMd.trim() === '768px'
      },
      critical: true
    },
    {
      name: '检查本地存储可用性',
      test: () => {
        try {
          localStorage.setItem('test', 'test')
          localStorage.removeItem('test')
          return true
        } catch (e) {
          return false
        }
      },
      critical: true
    },
    {
      name: '检查会话存储可用性',
      test: () => {
        try {
          sessionStorage.setItem('test', 'test')
          sessionStorage.removeItem('test')
          return true
        } catch (e) {
          return false
        }
      },
      critical: false
    }
  ]

  tests.forEach(test => {
    try {
      const passed = test.test()
      if (passed) {
        results.passed++
        results.details.push({
          name: test.name,
          status: 'pass',
          critical: test.critical
        })
      } else {
        if (test.critical) {
          results.failed++
        } else {
          results.warnings++
        }
        results.details.push({
          name: test.name,
          status: test.critical ? 'fail' : 'warning',
          critical: test.critical
        })
      }
    } catch (error) {
      results.failed++
      results.details.push({
        name: test.name,
        status: 'error',
        error: error.message,
        critical: test.critical
      })
    }
  })

  return results
}

/**
 * 验证表单数据
 */
export const validators = {
  required: (value) => {
    return value !== null && value !== undefined && String(value).trim() !== ''
  },

  email: (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  },

  password: (password) => {
    // 至少8位，包含字母和数字
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/
    return passwordRegex.test(password)
  },

  phone: (phone) => {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
  },

  url: (url) => {
    try {
      new URL(url)
      return true
    } catch {
      return false
    }
  },

  minLength: (value, min) => {
    return String(value).length >= min
  },

  maxLength: (value, max) => {
    return String(value).length <= max
  },

  number: (value) => {
    return !isNaN(value) && !isNaN(parseFloat(value))
  },

  integer: (value) => {
    return Number.isInteger(Number(value))
  },

  range: (value, min, max) => {
    const num = Number(value)
    return num >= min && num <= max
  }
}

/**
 * 表单验证器类
 */
export class FormValidator {
  constructor(rules = {}) {
    this.rules = rules
    this.errors = {}
  }

  validate(data) {
    this.errors = {}
    let isValid = true

    for (const field in this.rules) {
      const fieldRules = this.rules[field]
      const value = data[field]
      const fieldErrors = []

      for (const rule of fieldRules) {
        if (typeof rule === 'function') {
          if (!rule(value)) {
            fieldErrors.push('验证失败')
            isValid = false
          }
        } else if (typeof rule === 'object') {
          const { validator, message } = rule
          if (!validator(value)) {
            fieldErrors.push(message || '验证失败')
            isValid = false
          }
        }
      }

      if (fieldErrors.length > 0) {
        this.errors[field] = fieldErrors
      }
    }

    return isValid
  }

  getErrors() {
    return this.errors
  }

  getFieldError(field) {
    return this.errors[field] || []
  }

  hasError(field) {
    return field in this.errors
  }
}

/**
 * 验证文件类型和大小
 */
export function validateFile(file, options = {}) {
  const {
    maxSize = 5 * 1024 * 1024, // 5MB
    allowedTypes = ['image/jpeg', 'image/png', 'image/gif'],
    allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif']
  } = options

  const errors = []

  // 检查文件大小
  if (file.size > maxSize) {
    errors.push(`文件大小不能超过 ${Math.round(maxSize / 1024 / 1024)}MB`)
  }

  // 检查文件类型
  if (allowedTypes.length > 0 && !allowedTypes.includes(file.type)) {
    errors.push(`不支持的文件类型: ${file.type}`)
  }

  // 检查文件扩展名
  if (allowedExtensions.length > 0) {
    const extension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'))
    if (!allowedExtensions.includes(extension)) {
      errors.push(`不支持的文件扩展名: ${extension}`)
    }
  }

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 验证API响应
 */
export function validateApiResponse(response, expectedSchema = {}) {
  const errors = []

  if (!response) {
    errors.push('响应为空')
    return { valid: false, errors }
  }

  // 检查必需字段
  if (expectedSchema.required) {
    for (const field of expectedSchema.required) {
      if (!(field in response)) {
        errors.push(`缺少必需字段: ${field}`)
      }
    }
  }

  // 检查字段类型
  if (expectedSchema.types) {
    for (const [field, expectedType] of Object.entries(expectedSchema.types)) {
      if (field in response) {
        const actualType = typeof response[field]
        if (actualType !== expectedType) {
          errors.push(`字段 ${field} 类型错误: 期望 ${expectedType}, 实际 ${actualType}`)
        }
      }
    }
  }

  return {
    valid: errors.length === 0,
    errors,
    data: response
  }
}

/**
 * 验证环境配置
 */
export function validateEnvironment() {
  const checks = []

  // 检查 Node.js 版本（如果在构建环境中）
  if (typeof process !== 'undefined' && process.version) {
    const nodeVersion = process.version.replace('v', '')
    const [major] = nodeVersion.split('.')
    checks.push({
      name: 'Node.js 版本',
      status: parseInt(major) >= 16 ? 'pass' : 'warning',
      value: process.version,
      message: parseInt(major) >= 16 ? '版本符合要求' : '建议使用 Node.js 16+',
    })
  }

  // 检查浏览器特性支持
  const features = {
    'ES6 支持': () => {
      try {
        return eval('(x => x)(1)') === 1
      } catch {
        return false
      }
    },
    'Fetch API': () => typeof fetch !== 'undefined',
    'Local Storage': () => typeof localStorage !== 'undefined',
    'Service Worker': () => 'serviceWorker' in navigator,
    'Web Workers': () => typeof Worker !== 'undefined',
    'WebSocket': () => typeof WebSocket !== 'undefined',
  }

  for (const [name, test] of Object.entries(features)) {
    const supported = test()
    checks.push({
      name,
      status: supported ? 'pass' : 'fail',
      supported,
      message: supported ? '支持' : '不支持'
    })
  }

  return checks
}

/**
 * 性能验证
 */
export function validatePerformance() {
  const metrics = []

  // 检查页面加载时间
  if (performance && performance.timing) {
    const loadTime = performance.timing.loadEventEnd - performance.timing.navigationStart
    metrics.push({
      name: '页面加载时间',
      value: loadTime,
      unit: 'ms',
      status: loadTime < 3000 ? 'good' : loadTime < 5000 ? 'needs-improvement' : 'poor'
    })
  }

  // 检查内存使用
  if (performance.memory) {
    const memoryUsage = performance.memory.usedJSHeapSize / 1024 / 1024
    metrics.push({
      name: '内存使用',
      value: Math.round(memoryUsage),
      unit: 'MB',
      status: memoryUsage < 50 ? 'good' : memoryUsage < 100 ? 'needs-improvement' : 'poor'
    })
  }

  // 检查 DOM 节点数量
  const domNodes = document.querySelectorAll('*').length
  metrics.push({
    name: 'DOM 节点数量',
    value: domNodes,
    unit: '个',
    status: domNodes < 1500 ? 'good' : domNodes < 3000 ? 'needs-improvement' : 'poor'
  })

  return metrics
}

/**
 * 安全性检查
 */
export function validateSecurity() {
  const checks = []

  // 检查 HTTPS
  checks.push({
    name: 'HTTPS 连接',
    status: location.protocol === 'https:' || location.hostname === 'localhost' ? 'pass' : 'fail',
    message: location.protocol === 'https:' ? '使用安全连接' : '建议使用 HTTPS'
  })

  // 检查 CSP 头
  const cspMeta = document.querySelector('meta[http-equiv="Content-Security-Policy"]')
  checks.push({
    name: '内容安全策略 (CSP)',
    status: cspMeta ? 'pass' : 'warning',
    message: cspMeta ? '已配置 CSP' : '建议配置 CSP 提高安全性'
  })

  // 检查敏感信息泄露
  const scripts = Array.from(document.scripts)
  const hasApiKeys = scripts.some(script => 
    script.textContent && /api[_-]?key|secret|token/i.test(script.textContent)
  )
  checks.push({
    name: '敏感信息检查',
    status: hasApiKeys ? 'warning' : 'pass',
    message: hasApiKeys ? '可能包含敏感信息' : '未检测到明显的敏感信息'
  })

  return checks
}

// 综合验证函数
export function runFullValidation() {
  console.group('🔍 应用验证报告')
  
  const appValidation = validateApplication()
  console.log('📱 应用完整性:', appValidation)
  
  const envValidation = validateEnvironment()
  console.log('🌍 环境检查:', envValidation)
  
  const perfValidation = validatePerformance()
  console.log('⚡ 性能检查:', perfValidation)
  
  const securityValidation = validateSecurity()
  console.log('🔒 安全检查:', securityValidation)
  
  console.groupEnd()
  
  return {
    application: appValidation,
    environment: envValidation,
    performance: perfValidation,
    security: securityValidation
  }
}