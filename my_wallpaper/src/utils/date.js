/**
 * 日期时间格式化工具
 */

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期对象或日期字符串
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 * 
 * 支持的格式：
 * - YYYY: 四位年份
 * - MM: 两位月份
 * - DD: 两位日期
 * - HH: 两位小时（24小时制）
 * - mm: 两位分钟
 * - ss: 两位秒
 * 
 * 示例：
 * formatDate(new Date(), 'YYYY-MM-DD') => '2024-03-15'
 * formatDate('2024-03-15T10:30:00', 'YYYY-MM-DD HH:mm') => '2024-03-15 10:30'
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  // 检查日期是否有效
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化为相对时间（多久之前）
 * @param {string|Date} date - 日期对象或日期字符串
 * @returns {string} 相对时间描述
 * 
 * 示例：
 * formatRelativeTime(new Date()) => '刚刚'
 * formatRelativeTime(new Date(Date.now() - 60000)) => '1分钟前'
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  // 检查日期是否有效
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  
  // 小于1天
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // 小于7天
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }
  
  // 小于30天
  if (diff < 2592000000) {
    const weeks = Math.floor(diff / 604800000)
    return `${weeks}周前`
  }
  
  // 小于1年
  if (diff < 31536000000) {
    const months = Math.floor(diff / 2592000000)
    return `${months}个月前`
  }
  
  // 超过1年
  const years = Math.floor(diff / 31536000000)
  return `${years}年前`
}

/**
 * 格式化为友好的时间显示
 * @param {string|Date} date - 日期对象或日期字符串
 * @returns {string} 友好的时间描述
 * 
 * 规则：
 * - 今天：显示 HH:mm
 * - 昨天：显示 "昨天 HH:mm"
 * - 今年：显示 MM-DD HH:mm
 * - 往年：显示 YYYY-MM-DD
 * 
 * 示例：
 * formatFriendlyTime(new Date()) => '14:30'
 * formatFriendlyTime(yesterday) => '昨天 14:30'
 */
export function formatFriendlyTime(date) {
  if (!date) return ''
  
  const d = typeof date === 'string' ? new Date(date) : date
  
  // 检查日期是否有效
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const targetDate = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  
  const time = formatDate(d, 'HH:mm')
  
  // 今天
  if (targetDate.getTime() === today.getTime()) {
    return time
  }
  
  // 昨天
  if (targetDate.getTime() === yesterday.getTime()) {
    return `昨天 ${time}`
  }
  
  // 今年
  if (d.getFullYear() === now.getFullYear()) {
    return formatDate(d, 'MM-DD HH:mm')
  }
  
  // 往年
  return formatDate(d, 'YYYY-MM-DD')
}

/**
 * 解析后端返回的日期时间字符串
 * @param {string} dateStr - 后端返回的日期字符串
 * @returns {Date|null} Date对象或null
 */
export function parseDate(dateStr) {
  if (!dateStr) return null
  
  // 处理后端可能返回的各种格式
  // 例如：'2024-03-15T10:30:00', '2024-03-15 10:30:00', '2024-03-15'
  const d = new Date(dateStr)
  
  return isNaN(d.getTime()) ? null : d
}
