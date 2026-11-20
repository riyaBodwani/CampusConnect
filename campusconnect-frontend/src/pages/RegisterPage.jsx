import { useState } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import Logo from '../components/Logo'
import apiClient from '../api/client'
import './Auth.css'

export default function RegisterPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const initialRole = searchParams.get('role') === 'recruiter' ? 'recruiter' : 'student'

  const [role, setRole] = useState(initialRole)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  // Form State
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    // Student-specific fields
    dateOfBirth: '2003-05-15',
    course: 'B.Tech',
    branch: 'Computer Science',
    cgpa: '8.5',
    // Recruiter-specific field
    companyName: '',
    designation: 'HR & Campus Talent Lead',
  })

  const handleRoleChange = (newRole) => {
    if (isLoading) return
    setRole(newRole)
    setSearchParams({ role: newRole })
    setErrorMessage('')
    setSuccessMessage('')
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (isLoading) return

    setErrorMessage('')
    setSuccessMessage('')

    // Basic Validation
    if (!formData.fullName.trim() || !formData.email.trim() || !formData.password) {
      setErrorMessage('Please complete all required fields.')
      return
    }

    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Passwords do not match. Please verify your password entry.')
      return
    }

    if (formData.password.length < 6) {
      setErrorMessage('Password must be at least 6 characters long.')
      return
    }

    if (role === 'recruiter' && !formData.companyName.trim()) {
      setErrorMessage('Company Name is required for recruiter registration.')
      return
    }

    setIsLoading(true)

    try {
      if (role === 'student') {
        await apiClient.post('/students/register', {
          fullName: formData.fullName.trim(),
          email: formData.email.trim(),
          password: formData.password,
          phoneNumber: formData.phoneNumber.trim() || '9876543210',
          dateOfBirth: formData.dateOfBirth || '2003-01-01',
          course: formData.course || 'B.Tech',
          branch: formData.branch || 'Computer Science',
          cgpa: Number(formData.cgpa) || 8.0,
        })
      } else {
        await apiClient.post('/recruiters/register', {
          fullName: formData.fullName.trim(),
          email: formData.email.trim(),
          password: formData.password,
          phoneNumber: formData.phoneNumber.trim() || '9876543210',
          companyName: formData.companyName.trim(),
          designation: formData.designation.trim() || 'Talent Lead',
        })
      }

      setSuccessMessage('Registration successful! Redirecting to sign in...')
      setTimeout(() => {
        navigate(`/login?role=${role}`)
      }, 1500)
    } catch (err) {
      if (err.response?.data?.message) {
        setErrorMessage(err.response.data.message)
      } else if (err.response?.data?.errors) {
        const firstErr = Object.values(err.response.data.errors)[0]
        setErrorMessage(firstErr || 'Registration validation failed.')
      } else if (err.request) {
        setErrorMessage('Cannot connect to server. Ensure the backend is running.')
      } else {
        setErrorMessage(err.message || 'An unexpected error occurred during registration.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-container auth-container--wide">
        <div className="auth-card">
          {/* Header with Brand */}
          <div className="auth-header-top">
            <Link to="/" className="auth-brand-link" aria-label="CampusConnect Home">
              <Logo size="sm" />
            </Link>
          </div>

          <h1 className="auth-title">Create your account</h1>
          <p className="auth-subtitle">
            Join CampusConnect to connect academic potential with corporate opportunity.
          </p>

          {/* Role Selection Tabs */}
          <div className="auth-role-tabs" role="tablist" aria-label="Select Registration Role">
            <button
              type="button"
              role="tab"
              aria-selected={role === 'student'}
              className={`auth-role-tab ${role === 'student' ? 'active' : ''}`}
              onClick={() => handleRoleChange('student')}
              disabled={isLoading}
            >
              <span className="role-tab-icon" aria-hidden="true">🎓</span>
              <span>Student Registration</span>
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
              <span>Recruiter Registration</span>
            </button>
          </div>

          {/* Inline Message / Error / Success Area */}
          <div className={`auth-message-area ${(errorMessage || successMessage) ? 'auth-message-area--visible' : ''}`} aria-live="polite">
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
            {successMessage && (
              <div className="auth-success-banner" role="status">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
                <span>{successMessage}</span>
              </div>
            )}
          </div>

          {/* Registration Form */}
          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="auth-form-grid">
              {/* Account Credentials Section */}
              <div className="auth-form-section-title">
                Account Information
              </div>

              {/* Full Name */}
              <div className="auth-form-group auth-grid-full">
                <label htmlFor="fullName" className="auth-label">
                  Full Name <span className="auth-label-required">*</span>
                </label>
                <input
                  id="fullName"
                  name="fullName"
                  type="text"
                  className="auth-input"
                  placeholder={role === 'student' ? 'e.g. Alex Johnson' : 'e.g. Sarah Miller'}
                  value={formData.fullName}
                  onChange={handleChange}
                  required
                />
              </div>

              {/* Email */}
              <div className="auth-form-group">
                <label htmlFor="regEmail" className="auth-label">
                  Email Address <span className="auth-label-required">*</span>
                </label>
                <input
                  id="regEmail"
                  name="email"
                  type="email"
                  className="auth-input"
                  autoComplete="email"
                  placeholder={role === 'student' ? 'alex.johnson@university.edu' : 'sarah@company.com'}
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>

              {/* Phone Number */}
              <div className="auth-form-group">
                <label htmlFor="phoneNumber" className="auth-label">
                  Phone Number
                </label>
                <input
                  id="phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  className="auth-input"
                  placeholder="9876543210"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                />
              </div>

              {/* Password */}
              <div className="auth-form-group">
                <label htmlFor="regPassword" className="auth-label">
                  Password <span className="auth-label-required">*</span>
                </label>
                <div className="auth-password-wrap">
                  <input
                    id="regPassword"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    className="auth-input"
                    placeholder="Min. 6 characters"
                    value={formData.password}
                    onChange={handleChange}
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

              {/* Confirm Password */}
              <div className="auth-form-group">
                <label htmlFor="confirmPassword" className="auth-label">
                  Confirm Password <span className="auth-label-required">*</span>
                </label>
                <div className="auth-password-wrap">
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    className="auth-input"
                    placeholder="Repeat password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                  />
                  <button
                    type="button"
                    className="auth-password-toggle"
                    onClick={() => setShowConfirmPassword((p) => !p)}
                    aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                  >
                    {showConfirmPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
              </div>

              {/* Student Specific Fields */}
              {role === 'student' && (
                <>
                  <div className="auth-form-section-title auth-grid-full">
                    Academic Qualifications
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="course" className="auth-label">Degree / Course</label>
                    <input
                      id="course"
                      name="course"
                      type="text"
                      className="auth-input"
                      placeholder="e.g. B.Tech / BE"
                      value={formData.course}
                      onChange={handleChange}
                    />
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="branch" className="auth-label">Branch / Specialization</label>
                    <input
                      id="branch"
                      name="branch"
                      type="text"
                      className="auth-input"
                      placeholder="e.g. Computer Science"
                      value={formData.branch}
                      onChange={handleChange}
                    />
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="cgpa" className="auth-label">Current CGPA (out of 10)</label>
                    <input
                      id="cgpa"
                      name="cgpa"
                      type="number"
                      step="0.01"
                      min="0"
                      max="10"
                      className="auth-input"
                      placeholder="e.g. 8.5"
                      value={formData.cgpa}
                      onChange={handleChange}
                    />
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="dateOfBirth" className="auth-label">Date of Birth</label>
                    <input
                      id="dateOfBirth"
                      name="dateOfBirth"
                      type="date"
                      className="auth-input"
                      value={formData.dateOfBirth}
                      onChange={handleChange}
                    />
                  </div>
                </>
              )}

              {/* Recruiter Specific Fields */}
              {role === 'recruiter' && (
                <>
                  <div className="auth-form-section-title auth-grid-full">
                    Company Information
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="companyName" className="auth-label">
                      Company Name <span className="auth-label-required">*</span>
                    </label>
                    <input
                      id="companyName"
                      name="companyName"
                      type="text"
                      className="auth-input"
                      placeholder="e.g. TechNova Solutions"
                      value={formData.companyName}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="auth-form-group">
                    <label htmlFor="designation" className="auth-label">Your Designation</label>
                    <input
                      id="designation"
                      name="designation"
                      type="text"
                      className="auth-input"
                      placeholder="e.g. Senior Campus Recruiter"
                      value={formData.designation}
                      onChange={handleChange}
                    />
                  </div>
                </>
              )}
            </div>

            <button
              type="submit"
              className="btn btn-primary auth-btn-submit"
              disabled={isLoading}
            >
              {isLoading ? 'Creating Account...' : `Register as ${role === 'student' ? 'Student' : 'Recruiter'}`}
            </button>
          </form>

          {/* Footer Link */}
          <div className="auth-footer-prompt">
            Already have an account?{' '}
            <Link to={`/login?role=${role}`} className="auth-link">
              Sign In here
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
