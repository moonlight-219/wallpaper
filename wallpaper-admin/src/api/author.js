import request from '@/utils/request'

export function getAllUsers(params = {}) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function getAuthorList(params = {}) {
  return request({
    url: '/author/list',
    method: 'get',
    params
  })
}

export function getAuthorDetail(id) {
  return request({
    url: `/author/${id}`,
    method: 'get'
  })
}

export function updateCreatorStatus(id, data) {
  return request({
    url: `/admin/user/${id}/creator-status`,
    method: 'put',
    data
  })
}

export function deleteAuthor(id) {
  return request({
    url: `/author/${id}`,
    method: 'delete'
  })
}
