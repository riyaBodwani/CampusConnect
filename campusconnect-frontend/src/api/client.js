import axios from 'axios'
import { getAuthToken } from '../context/AuthContext.jsx'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor: attach Authorization header if token exists, remove if not
apiClient.interceptors.request.use(
  (config) => {
    const token = getAuthToken()
    if (token) {
      if (config.headers?.set) {
        config.headers.set('Authorization', `Bearer ${token}`)
      } else {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}`
      }
    } else if (config.headers) {
      if (config.headers.delete) {
        config.headers.delete('Authorization')
        config.headers.delete('authorization')
      }
      delete config.headers.Authorization
      delete config.headers.authorization
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default apiClient
