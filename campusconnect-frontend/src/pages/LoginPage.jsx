import { useState } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import Logo from '../components/Logo'
import apiClient from '../api/client'
import { useAuth } from '../context/useAuth'
import './Auth.css'

export default function LoginPage() {
  const { login } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const initialRole = searchParams.get('role') === 'recruiter' ? 'recruiter' : 'student'

  const [role, setRole] = useState(initialRole)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [loginSuccess, setLoginSuccess] = useState(false)

  const handleRoleChange = (newRole) => {
    if (isLoading) return
    setRole(newRole)
    setSearchParams({ role: newRole })
    setErrorMessage('')
    setLoginSuccess(false)
  }

  const handleFillDemo = () => {
    if (role === 'student') {
      setEmail('student@campusconnect.com')
      setPassword('DemoPass123!')
    } else {
      setEmail('recruiter@technova.com')
      setPassword('DemoPass123!')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (isLoading) return

    setErrorMessage('')
    setLoginSuccess(false)

    const trimmedEmail = email.trim()
    if (!trimmedEmail || !password) {
      setErrorMessage('Please enter both email and password.')
      return
    }

    setIsLoading(true)

    try {
      const endpoint = role === 'student' ? '/students/login' : '/recruiters/login'
      const response = await apiClient.post(endpoint, {
        email: trimmedEmail,
        password,
      })

      const token = response.data?.token
      if (!token) {
        throw new Error('Authentication succeeded but no token was received from the server.')
      }

      // Normalize profile shape:
      let normalizedUser
      if (role === 'student') {
        normalizedUser = response.data.student
      } else {
        const recruiterProfile = { ...response.data }
        delete recruiterProfile.token
        normalizedUser = recruiterProfile
      }

      await login(token, normalizedUser, role, rememberMe)
      setLoginSuccess(true)
      navigate(role === 'recruiter' ? '/recruiter' : '/student', { replace: true })
    } catch (err) {
      setLoginSuccess(false)
      if (err.response) {
        const errorData = err.response.data
        if (errorData?.errors && typeof errorData.errors === 'object') {
          const fieldErrors = Object.values(errorData.errors).join(', ')
          setErrorMessage(fieldErrors || errorData.message || 'Validation failed.')
        } else if (errorData?.message) {
          setErrorMessage(errorData.message)
        } else {
          setErrorMessage(`Login failed (${err.response.status}). Please check credentials.`)
        }
      } else if (err.request) {
        setErrorMessage('Unable to connect to the server. Please ensure the backend is running.')
      } else {
        setErrorMessage(err.message || 'An unexpected error occurred during login.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          {/* Header with Brand */}
          <div className="auth-header-top">
            <Link to="/" className="auth-brand-link" aria-label="CampusConnect Home">
              <Logo size="sm" />
            </Link>
          </div>

          <h1 className="auth-title">Welcome back</h1>
          <p className="auth-subtitle">
            Sign in to access your placement dashboard and track activities in real time.
          </p>

          {/* Role Selection Tabs */}
          <div className="auth-role-tabs" role="tablist" aria-label="Select Login Role">
            <button
              type="button"
              role="tab"
              aria-selected={role === 'student'}
              className={`auth-role-tab ${role === 'student' ? 'active' : ''}`}
              onClick={() => handleRoleChange('student')}
              disabled={isLoading}
            >
              <span className="role-tab-icon" aria-hidden="true">🎓</span>
              <span>Student</span>
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={role === 'recruiter'}
              className={`auth-role-tab ${role === 'recruiter' ? 'active' : ''}`}
              onClick={() => handleRoleChange('recruiter')}
              disabled={isLoading}
            >
              <span className="role-tab-icon" aria-hidden="true">🏢</span>
              <span>Recruiter</span>
            </button>
          </div>

          {/* Inline Message / Error / Success Area */}
          <div className={`auth-message-area ${(errorMessage || loginSuccess) ? 'auth-message-area--visible' : ''}`} aria-live="polite">
            {errorMessage && (
              <div className="auth-error-banner" role="alert">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <span>{errorMessage}</span>
              </div>
            )}
            {loginSuccess && (
              <div className="auth-success-banner" role="status">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
                <span>Authentication successful! Opening dashboard...</span>
              </div>
            )}
          </div>

          {/* Login Form */}
          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            {/* Email Address */}
            <div className="auth-form-group">
              <label htmlFor="loginEmail" className="auth-label">
                Email Address
              </label>
              <input
                id="loginEmail"
                type="email"
                className="auth-input"
                autoComplete="email"
                placeholder={role === 'student' ? 'student@campusconnect.com' : 'recruiter@technova.com'}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            {/* Password */}
            <div className="auth-form-group">
              <div className="auth-label-row">
                <label htmlFor="loginPassword" className="auth-label">
                  Password
                </label>
                <button
                  type="button"
                  className="auth-demo-fill-btn"
                  onClick={handleFillDemo}
                  title="Fill verified demo account credentials"
                >
                  Fill Demo
                </button>
              </div>
              <div className="auth-password-wrap">
                <input
                  id="loginPassword"
                  type={showPassword ? 'text' : 'password'}
                  className="auth-input"
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setShowPassword((p) => !p)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            {/* Remember Me Checkbox */}
            <div className="auth-form-row">
              <label className="auth-checkbox-label">
                <input
                  type="checkbox"
                  className="auth-checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                <span>Keep me signed in</span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              className="btn btn-primary auth-btn-submit"
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <span className="auth-btn-spinner" aria-hidden="true"></span>
                  <span>Signing in...</span>
                </>
              ) : (
                `Sign in as ${role === 'student' ? 'Student' : 'Recruiter'}`
              )}
            </button>
          </form>

          {/* Footer Link */}
          <div className="auth-footer-prompt">
            Don&apos;t have an account?{' '}
            <Link to={`/register?role=${role}`} className="auth-link">
              Create an account
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
