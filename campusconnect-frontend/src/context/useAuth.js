import { useContext } from 'react'
import { AuthContext } from './AuthContext.jsx'

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    return {
      token: null,
      user: null,
      role: null,
      isAuthenticated: false,
      login: () => {},
      logout: () => {},
    }
  }
  return context
}

export default useAuth
