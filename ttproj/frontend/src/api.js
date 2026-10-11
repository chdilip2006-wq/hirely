import axios from 'axios'
// Keep API calls same-origin in development (Vite proxies /api to Spring Boot)
// and production. This avoids browser CORS and cookie/CSRF inconsistencies.
const defaultApiUrl = '/api'
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || defaultApiUrl, withCredentials: true, timeout: 15000 })
let csrfToken = ''
api.interceptors.request.use(async config => {
  if (!csrfToken) {
    try { const response = await axios.get(`${api.defaults.baseURL}/csrf`, { withCredentials: true, timeout: 10000 }); csrfToken = response.data.token }
    catch { throw new Error('Could not reach the sign-in service. Check that the backend is running, then try again.') }
  }
  if (csrfToken && !['get','head','options'].includes((config.method || 'get').toLowerCase())) config.headers['X-XSRF-TOKEN'] = csrfToken
  return config
})
export const errorText = e => {
  if (e?.code === 'ECONNABORTED') return 'The server took too long to respond. Check that the backend and database are running, then try again.'
  const body = e?.response?.data
  if (typeof body === 'string' && body.trim()) return body
  const message = body?.detail || body?.message || body?.error?.message
  if (message) return message
  if (e?.response?.status === 403) return 'This action is not available for your account, or your security session expired. Refresh the page and try again.'
  return e?.message || 'Something went wrong. Please try again.'
}
