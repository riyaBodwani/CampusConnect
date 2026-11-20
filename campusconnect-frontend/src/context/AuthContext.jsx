/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect } from 'react'

export const AuthContext = createContext(null)

const AUTH_STORAGE_KEY = 'campusconnect_auth'

function getStoredAuth() {
  // Check sessionStorage first, then localStorage
  try {
    const sessionData = sessionStorage.getItem(AUTH_STORAGE_KEY)
    if (sessionData) {
      const parsed = JSON.parse(sessionData)
      if (parsed && typeof parsed === 'object' && parsed.token && parsed.user) {
        const cleanUser = { ...parsed.user }
        delete cleanUser.password
        return {
          token: String(parsed.token),
          user: cleanUser,
          role: parsed.role || null,
        }
      }
      sessionStorage.removeItem(AUTH_STORAGE_KEY)
    }
  } catch {
    try {
      sessionStorage.removeItem(AUTH_STORAGE_KEY)
    } catch {
      // Ignore storage errors
    }
  }

  try {
    const localData = localStorage.getItem(AUTH_STORAGE_KEY)
    if (localData) {
      const parsed = JSON.parse(localData)
      if (parsed && typeof parsed === 'object' && parsed.token && parsed.user) {
        const cleanUser = { ...parsed.user }
        delete cleanUser.password
        return {
          token: String(parsed.token),
          user: cleanUser,
          role: parsed.role || null,
        }
      }
      localStorage.removeItem(AUTH_STORAGE_KEY)
    }
  } catch {
    try {
      localStorage.removeItem(AUTH_STORAGE_KEY)
    } catch {
      // Ignore storage errors
    }
  }

  return { token: null, user: null, role: null }
}

// Module-level token holder for non-React contexts (e.g., Axios interceptors)
let currentAuthToken = null

try {
  const initial = getStoredAuth()
  currentAuthToken = initial.token || null
} catch {
  currentAuthToken = null
}

/**
 * Returns the current active authentication token.
 * Reads memory cache with safe fallback to persistent storage.
 */
export function getAuthToken() {
  if (currentAuthToken) {
    return currentAuthToken
  }
  try {
    const stored = getStoredAuth()
    if (stored?.token) {
      currentAuthToken = stored.token
      return currentAuthToken
    }
  } catch {
    // Ignore storage errors
  }
  return null
}

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(getStoredAuth)

  // Keep module-level token cache synchronized with React state
  useEffect(() => {
    currentAuthToken = authState.token || null
  }, [authState.token])

  const login = (newTokenOrObj, newUser, newRole = null, rememberMe = false) => {
    let t = newTokenOrObj
    let u = newUser
    let r = newRole
    let rem = rememberMe

    if (newTokenOrObj && typeof newTokenOrObj === 'object' && 'token' in newTokenOrObj && !newUser) {
      t = newTokenOrObj.token
      u = newTokenOrObj.user
      r = newTokenOrObj.role || null
      rem = Boolean(newTokenOrObj.rememberMe)
    }

    // Defensive normalization in case raw backend response is supplied
    let normalizedUser = u
    if (r === 'student' && u?.student) {
      normalizedUser = u.student
    } else if (u && typeof u === 'object' && 'token' in u) {
      const copy = { ...u }
      delete copy.token
      normalizedUser = copy
    }

    // Ensure password is never stored or kept in user state
    if (normalizedUser && typeof normalizedUser === 'object' && 'password' in normalizedUser) {
      const safeCopy = { ...normalizedUser }
      delete safeCopy.password
      normalizedUser = safeCopy
    }

    const nextState = {
      token: t,
      user: normalizedUser,
      role: r,
    }

    currentAuthToken = t || null
    setAuthState(nextState)

    if (rem) {
      try {
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextState))
        sessionStorage.removeItem(AUTH_STORAGE_KEY)
      } catch {
        // Ignore storage errors in restricted environments
      }
    } else {
      try {
        sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextState))
        localStorage.removeItem(AUTH_STORAGE_KEY)
      } catch {
        // Ignore storage errors in restricted environments
      }
    }
  }

  const logout = () => {
    currentAuthToken = null
    setAuthState({ token: null, user: null, role: null })
    try {
      localStorage.removeItem(AUTH_STORAGE_KEY)
    } catch {
      // Ignore
    }
    try {
      sessionStorage.removeItem(AUTH_STORAGE_KEY)
    } catch {
      // Ignore
    }
  }

  const value = {
    token: authState.token,
    user: authState.user,
    role: authState.role,
    isAuthenticated: Boolean(authState.token),
    login,
    logout,
    getAuthToken,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export default AuthProvider
