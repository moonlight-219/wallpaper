import request from '@/utils/request'

export const createWork = (data = {}) => {
  return request({
    url: '/works',
    method: 'post',
    data
  })
}

export const updateWorkStatus = (id, status) => {
  return request({
    url: `/works/${id}/status`,
    method: 'put',
    data: { status }
  })
}
