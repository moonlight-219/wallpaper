import request from '@/utils/request'

export const getWallpaperList = (params = {}) => {
  return request({
    url: '/wallpaper/list',
    method: 'get',
    params
  })
}
