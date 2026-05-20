/**
 * 项目配置文件
 * 通过 .env.development / .env.production 控制环境切换
 * 部署时修改对应 .env 文件中的 VITE_API_BASE_URL 即可
 */

// 服务器参考配置（仅作记录，实际 baseURL 由环境变量控制）
const SERVER_CONFIG = {
  // 服务器IP地址（生产环境）
  serverIP: '8.138.110.109',
  // 服务器端口（通过 Nginx 代理，使用 80 端口，留空即可）
  serverPort: '',
  // 是否使用域名（如果有域名，设置为 true 并填写域名）
  useDomain: false,
  // 域名地址（如果 useDomain 为 true，填写你的域名）
  domain: 'https://yourdomain.com',
  
  // 开发环境配置（小程序开发时使用）
  dev: {
    // 本地局域网地址（用于小程序开发调试）
    localIP: '192.168.2.240',
    localPort: '9999'
  }
}

// 从环境变量读取 baseURL（.env.development / .env.production）
const envBaseURL = import.meta.env.VITE_API_BASE_URL

// 根据平台和环境选择 baseURL
// - H5 开发：使用 Vite proxy，baseURL 为空，请求走 /api/xxx（无跨域）
// - 小程序开发：直连后端，baseURL 为完整地址（小程序无跨域限制）
// - 生产环境：使用环境变量中的地址，/api 前缀由 request.js 自动添加
let config

// #ifdef H5
config = {
  baseURL: process.env.NODE_ENV === 'development' ? '' : envBaseURL
}
// #endif

// #ifndef H5
config = {
  baseURL: envBaseURL
}
// #endif

// 导出配置和服务器信息
export default config
export { SERVER_CONFIG }