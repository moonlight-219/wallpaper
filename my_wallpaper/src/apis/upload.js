import request from '@/utils/request'
import config from '@/config/index.js'

/**
 * 上传图片
 * @param {string} filePath - 图片文件路径（uni.chooseImage 或 uni.canvasToTempFilePath 返回的临时路径）
 * @param {number} type - 类型：1=手机壁纸，2=平板壁纸，3=头像
 * @returns {Promise} 返回图片信息（url, thumbnailUrl, width, height, format, size）
 */
export function uploadImage(filePath, type = 1) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    console.log('[uploadImage] 上传图片:', {
      filePath,
      type,
      hasToken: !!token,
      tokenLength: token ? token.length : 0,
      platform: process.env.UNI_PLATFORM
    })

    let baseUrl = config.baseURL || ''

    // #ifdef H5
    if (process.env.NODE_ENV === 'development') {
      // H5 开发：走 Vite proxy，直接用 /api
      baseUrl = '/api'
      console.log('[uploadImage] H5开发模式，使用Vite代理')
    } else {
      // H5 生产：baseURL 为服务器地址，加 /api 前缀
      baseUrl = baseUrl + '/api'
    }
    // #endif

    // #ifndef H5
    // 小程序：所有环境都需要加 /api 前缀（后端 context-path 是 /api）
    baseUrl = baseUrl + '/api'
    // #endif

    console.log('[uploadImage] 上传地址:', baseUrl)

    uni.uploadFile({
      url: `${baseUrl}/upload`,  // 去掉 /api，因为 baseUrl 已经包含了
      filePath: filePath,
      name: 'file',
      formData: {
        type: type
      },
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        console.log('[uploadImage] 上传响应:', {
          statusCode: res.statusCode,
          data: res.data
        })

        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data)
            console.log('[uploadImage] 解析后的数据:', data)

            if (data.code === 200) {
              resolve(data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } catch (error) {
            console.error('[uploadImage] JSON解析失败:', error)
            console.error('[uploadImage] 原始响应:', res.data)
            uni.showToast({
              title: '服务器响应格式错误',
              icon: 'none'
            })
            reject({ error: 'JSON解析失败', raw: res.data })
          }
        } else if (res.statusCode === 401) {
          console.log('[uploadImage] 收到401，清除token')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          reject(res)
        } else {
          console.error('[uploadImage] 上传失败，状态码:', res.statusCode)
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('[uploadImage] 上传失败:', err)
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * 创建作品集
 * @param {Object} data - 作品集数据
 * @param {string} data.title - 作品集标题
 * @param {number} data.type - 作品类型：1=手机壁纸，2=平板壁纸，3=头像
 * @param {number} data.categoryId - 分类ID
 * @param {string} data.description - 描述
 * @param {Array} data.wallpapers - 壁纸列表
 * @returns {Promise} 返回创建的作品集信息
 */
export function createWork(data) {
  return request({
    url: '/works',
    method: 'post',
    data
  })
}
