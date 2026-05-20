import request from '@/utils/request'

export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 1)
  
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getCategoryList(params = {}) {
  return request({
    url: '/category',
    method: 'get',
    params
  })
}

export function getCategoryDetail(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/category/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}
