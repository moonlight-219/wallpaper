import request from '@/utils/request'

export const getSquareWorks = (params = {}) => {
  return request({
    url: '/square/works',
    method: 'get',
    params
  })
}

export const auditWork = (id, status, reason) => {
  return request({
    url: `/admin/work/${id}/audit`,
    method: 'put',
    data: { status, reason }
  })
}
