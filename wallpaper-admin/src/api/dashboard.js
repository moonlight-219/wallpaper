import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

export function getDashboardData() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}
