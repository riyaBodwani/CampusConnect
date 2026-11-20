import { useState, useEffect, useMemo } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/useAuth'
import './StudentPage.css'

export default function StudentPage() {
  const { user, isAuthenticated } = useAuth()
  const [searchParams] = useSearchParams()

  // Navigation tab: 'drives' | 'applications'
  const [activeTab, setActiveTab] = useState('drives')

  // Search & Filter state
  const [searchKeyword, setSearchKeyword] = useState(searchParams.get('q') || '')
  const [filterLocation, setFilterLocation] = useState(searchParams.get('loc') || '')
  const [filterCgpa, setFilterCgpa] = useState(searchParams.get('cgpa') || 'all')
  const [filterType, setFilterType] = useState(searchParams.get('type') || 'all')
  const [onlyEligible, setOnlyEligible] = useState(false)

  // Applications state
  const [applications, setApplications] = useState([])
  const [isLoadingApps, setIsLoadingApps] = useState(true)
  const [appsErrorMessage, setAppsErrorMessage] = useState('')

  // Placement Drives state
  const [drives, setDrives] = useState([])
  const [isLoadingDrives, setIsLoadingDrives] = useState(true)
  const [drivesErrorMessage, setDrivesErrorMessage] = useState('')

  // Apply action state
  const [applyingDriveId, setApplyingDriveId] = useState(null)
  const [applySuccessMessage, setApplySuccessMessage] = useState('')
  const [applyErrorMessage, setApplyErrorMessage] = useState('')

  // Bookmarked drives set (client-side interactive feature)
  const [bookmarkedDriveIds, setBookmarkedDriveIds] = useState(new Set())

  const toggleBookmark = (driveId) => {
    setBookmarkedDriveIds((prev) => {
      const next = new Set(prev)
      if (next.has(driveId)) {
        next.delete(driveId)
      } else {
        next.add(driveId)
      }
      return next
    })
  }

  // Handle Application Submission
  const handleApply = async (drive) => {
    if (!drive || applyingDriveId !== null) return

    setApplyingDriveId(drive.id)
    setApplyErrorMessage('')
    setApplySuccessMessage('')

    try {
      await apiClient.post('/applications', {
        placementDriveId: drive.id,
      })
      const company = drive.companyName ? ` at ${drive.companyName}` : ''
      setApplySuccessMessage(
        `Application submitted successfully for "${drive.jobTitle || 'Placement Drive'}"${company}!`
      )
      loadApplications()
    } catch (err) {
      if (err.response) {
        if (err.response.status === 400) {
          setApplyErrorMessage(
            err.response.data?.message || 'You have already applied to this placement drive.'
          )
          loadApplications()
        } else if (err.response.status === 401) {
          setApplyErrorMessage('Your session has expired. Please sign in again as a student to apply.')
        } else if (err.response.status === 403) {
          setApplyErrorMessage('Access denied. Only student accounts can apply to placement drives.')
        } else if (err.response.status === 404) {
          setApplyErrorMessage(
            err.response.data?.message || 'This placement drive is no longer available.'
          )
          loadDrives()
        } else {
          setApplyErrorMessage(
            err.response.data?.message ||
              `Failed to submit application (Error ${err.response.status}). Please try again.`
          )
        }
      } else if (err.request) {
        setApplyErrorMessage('Unable to connect to the server. Please check your network connection.')
      } else {
        setApplyErrorMessage(err.message || 'An unexpected error occurred while applying.')
      }
    } finally {
      setApplyingDriveId(null)
    }
  }

  // Auto-dismiss apply feedback messages
  useEffect(() => {
    if (!applySuccessMessage) return
    const timer = setTimeout(() => setApplySuccessMessage(''), 6000)
    return () => clearTimeout(timer)
  }, [applySuccessMessage])

  useEffect(() => {
    if (!applyErrorMessage) return
    const timer = setTimeout(() => setApplyErrorMessage(''), 6000)
    return () => clearTimeout(timer)
  }, [applyErrorMessage])

  const loadApplications = () => {
    setIsLoadingApps(true)
    setAppsErrorMessage('')

    apiClient
      .get('/applications/my')
      .then((response) => {
        setApplications(Array.isArray(response.data) ? response.data : [])
      })
      .catch((err) => {
        if (err.response?.status === 401) {
          setAppsErrorMessage('You must be signed in as a student to view your applications.')
        } else if (err.response?.status === 403) {
          setAppsErrorMessage('Access denied. This section is only accessible to student accounts.')
        } else if (err.response?.data?.message) {
          setAppsErrorMessage(err.response.data.message)
        } else if (err.request) {
          setAppsErrorMessage('Unable to connect to the server. Please ensure the backend is running.')
        } else {
          setAppsErrorMessage(err.message || 'An error occurred while loading your applications.')
        }
      })
      .finally(() => {
        setIsLoadingApps(false)
      })
  }

  const loadDrives = () => {
    setIsLoadingDrives(true)
    setDrivesErrorMessage('')

    apiClient
      .get('/placement-drives')
      .then((response) => {
        setDrives(Array.isArray(response.data) ? response.data : [])
      })
      .catch((err) => {
        if (err.response?.status === 401) {
          setDrivesErrorMessage('You must be signed in to view placement drives.')
        } else if (err.response?.status === 403) {
          setDrivesErrorMessage('Access denied. You do not have permission to view placement drives.')
        } else if (err.response?.data?.message) {
          setDrivesErrorMessage(err.response.data.message)
        } else if (err.request) {
          setDrivesErrorMessage('Unable to connect to the server. Please ensure the backend is running.')
        } else {
          setDrivesErrorMessage(err.message || 'An error occurred while loading placement drives.')
        }
      })
      .finally(() => {
        setIsLoadingDrives(false)
      })
  }

  useEffect(() => {
    loadApplications()
    loadDrives()
  }, [])

  // Filtered drives based on search toolbar
  const filteredDrives = useMemo(() => {
    return drives.filter((drive) => {
      // Keyword match
      if (searchKeyword.trim()) {
        const query = searchKeyword.toLowerCase()
        const titleMatch = (drive.jobTitle || '').toLowerCase().includes(query)
        const companyMatch = (drive.companyName || '').toLowerCase().includes(query)
        const descMatch = (drive.jobDescription || '').toLowerCase().includes(query)
        if (!titleMatch && !companyMatch && !descMatch) return false
      }

      // Location match
      if (filterLocation.trim()) {
        const loc = filterLocation.toLowerCase()
        if (!(drive.location || '').toLowerCase().includes(loc)) return false
      }

      // CGPA threshold filter
      if (filterCgpa !== 'all') {
        const threshold = parseFloat(filterCgpa)
        if (drive.minimumCgpa != null && parseFloat(drive.minimumCgpa) > threshold) {
          return false
        }
      }

      // Only eligible filter (if user has CGPA)
      if (onlyEligible && user?.cgpa != null && drive.minimumCgpa != null) {
        if (parseFloat(user.cgpa) < parseFloat(drive.minimumCgpa)) {
          return false
        }
      }

      return true
    })
  }, [drives, searchKeyword, filterLocation, filterCgpa, onlyEligible, user])

  // Company avatar generator
  const getCompanyAvatar = (companyName) => {
    const name = companyName || 'Company'
    const initial = name.charAt(0).toUpperCase()
    const colors = ['#6366F1', '#8B5CF6', '#EC4899', '#3B82F6', '#10B981', '#F59E0B']
    const colorIndex = (name.charCodeAt(0) + (name.charCodeAt(1) || 0)) % colors.length
    return { initial, bg: colors[colorIndex] }
  }

  const studentCgpa = user?.cgpa != null ? Number(user.cgpa).toFixed(1) : '8.5'
  const studentBranch = user?.branch || 'Computer Engineering'

  return (
    <div className="talentry-dashboard-page">
      {/* 1. Talentry Header & Greeting Banner */}
      <header className="talentry-welcome-header">
        <div className="welcome-headline-group">
          <h1 className="welcome-title">
            Welcome back, {user?.fullName || 'Student'}
          </h1>
          <p className="welcome-meta">
            <span className="welcome-badge">
              {filteredDrives.length} verified placement drives open
            </span>
            <span className="welcome-divider">•</span>
            <span>Branch: <strong>{studentBranch}</strong></span>
            <span className="welcome-divider">•</span>
            <span>Academic CGPA: <strong>{studentCgpa}</strong></span>
          </p>
        </div>

        {/* Global Feedback Banners */}
        {applySuccessMessage && (
          <div className="feedback-banner feedback-banner--success" role="status">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span>{applySuccessMessage}</span>
            <button
              type="button"
              className="feedback-dismiss-btn"
              onClick={() => setApplySuccessMessage('')}
            >
              &times;
            </button>
          </div>
        )}

        {applyErrorMessage && (
          <div className="feedback-banner feedback-banner--error" role="alert">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{applyErrorMessage}</span>
            <button
              type="button"
              className="feedback-dismiss-btn"
              onClick={() => setApplyErrorMessage('')}
            >
              &times;
            </button>
          </div>
        )}
      </header>

      {/* 2. Talentry Horizontal Search & Filter Toolbar */}
      <section className="talentry-toolbar-wrapper">
        <div className="talentry-toolbar-card">
          <form
            className="talentry-toolbar-form"
            onSubmit={(e) => e.preventDefault()}
          >
            {/* Input 1: Job Title / Keywords */}
            <div className="toolbar-field">
              <label htmlFor="student-keyword-search" className="toolbar-label">
                Job Title / Keywords
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <input
                  id="student-keyword-search"
                  type="text"
                  className="toolbar-input"
                  placeholder="e.g. Developer, Analyst"
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                />
              </div>
            </div>

            {/* Input 2: Location */}
            <div className="toolbar-field">
              <label htmlFor="student-location-search" className="toolbar-label">
                Location
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <input
                  id="student-location-search"
                  type="text"
                  className="toolbar-input"
                  placeholder="e.g. Pune, Bangalore, Remote"
                  value={filterLocation}
                  onChange={(e) => setFilterLocation(e.target.value)}
                />
              </div>
            </div>

            {/* Input 3: Min CGPA */}
            <div className="toolbar-field">
              <label htmlFor="student-cgpa-filter" className="toolbar-label">
                Eligibility / CGPA
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
                  <path d="M6 12v5c3 3 9 3 12 0v-5" />
                </svg>
                <select
                  id="student-cgpa-filter"
                  className="toolbar-select"
                  value={filterCgpa}
                  onChange={(e) => setFilterCgpa(e.target.value)}
                >
                  <option value="all">All Drives</option>
                  <option value="6.0">Min 6.0 CGPA</option>
                  <option value="7.0">Min 7.0 CGPA</option>
                  <option value="8.0">Min 8.0 CGPA</option>
                </select>
              </div>
            </div>

            {/* Input 4: Drive Status */}
            <div className="toolbar-field">
              <label htmlFor="student-type-filter" className="toolbar-label">
                Drive Status
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                </svg>
                <select
                  id="student-type-filter"
                  className="toolbar-select"
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value)}
                >
                  <option value="all">All Statuses</option>
                  <option value="open">Active / Open Only</option>
                </select>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="toolbar-button-wrap">
              <button
                type="button"
                className="btn btn-primary toolbar-search-btn"
                onClick={() => {}}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <span>Search</span>
              </button>
            </div>
          </form>
        </div>
      </section>

      {/* 3. Main Dashboard Body: Left Dock/Sidebar + Right Card Grid */}
      <div className="talentry-dashboard-body">
        {/* Left Navigation Dock & Quick Panels */}
        <aside className="talentry-sidebar">
          {/* Navigation Tabs (Explore vs My Applications) */}
          <div className="sidebar-nav-tabs" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'drives'}
              className={`sidebar-tab-btn ${activeTab === 'drives' ? 'active' : ''}`}
              onClick={() => setActiveTab('drives')}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
              </svg>
              <span>Explore Drives</span>
              <span className="sidebar-tab-count">{drives.length}</span>
            </button>

            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'applications'}
              className={`sidebar-tab-btn ${activeTab === 'applications' ? 'active' : ''}`}
              onClick={() => setActiveTab('applications')}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
              <span>My Applications</span>
              <span className="sidebar-tab-count sidebar-tab-count--mint">
                {applications.length}
              </span>
            </button>
          </div>

          {/* Automated Academic Criteria & Qualification Filter */}
          <div className="ai-match-card">
            <div className="ai-card-badge">
              <span className="ai-pulse-dot"></span>
              <span>Eligibility Screening</span>
            </div>
            <h3 className="ai-card-title">Criteria & Qualification Filter</h3>
            <p className="ai-card-subtext">
              Real-time rule evaluation matches your branch & CGPA against verified recruiter requirements.
            </p>

            <div className="ai-stats-pill-grid">
              <div className="ai-stat-box">
                <span className="ai-stat-label">CGPA</span>
                <span className="ai-stat-value">{studentCgpa}</span>
              </div>
              <div className="ai-stat-box">
                <span className="ai-stat-label">Branch</span>
                <span className="ai-stat-value ai-stat-value--sm">{studentBranch.split(' ')[0]}</span>
              </div>
            </div>

            <label className="ai-toggle-label">
              <input
                type="checkbox"
                checked={onlyEligible}
                onChange={(e) => setOnlyEligible(e.target.checked)}
                className="ai-toggle-checkbox"
              />
              <span className="ai-toggle-text">Only show drives I qualify for</span>
            </label>
          </div>

          {/* Quick Academic Profile Summary */}
          <div className="academic-profile-card">
            <h4 className="profile-card-title">Student Credentials</h4>
            <div className="profile-details-list">
              <div className="profile-detail-row">
                <span className="detail-key">Name:</span>
                <span className="detail-val">{user?.fullName || 'Demo Student'}</span>
              </div>
              <div className="profile-detail-row">
                <span className="detail-key">Course:</span>
                <span className="detail-val">{user?.course || 'B.Tech'}</span>
              </div>
              <div className="profile-detail-row">
                <span className="detail-key">Email:</span>
                <span className="detail-val detail-val--truncate">{user?.email || 'student@campusconnect.com'}</span>
              </div>
              <div className="profile-detail-row">
                <span className="detail-key">Status:</span>
                <span className="detail-status-pill">Active Student</span>
              </div>
            </div>
          </div>
        </aside>

        {/* Right Main Content Panel */}
        <main className="talentry-main-content">
          {/* TAB 1: EXPLORE DRIVES */}
          {activeTab === 'drives' && (
            <div className="drives-view">
              <div className="view-header-row">
                <div>
                  <h2 className="view-title">Placement Drives</h2>
                  <p className="view-subtitle">
                    Showing {filteredDrives.length} of {drives.length} active opportunities
                  </p>
                </div>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={loadDrives}
                  disabled={isLoadingDrives}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="23 4 23 10 17 10" />
                    <polyline points="1 20 1 14 7 14" />
                    <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
                  </svg>
                  <span>Refresh</span>
                </button>
              </div>

              {/* Loading State */}
              {isLoadingDrives && (
                <div className="state-card" role="status">
                  <div className="talentry-spinner"></div>
                  <p className="state-message">Loading placement drives...</p>
                </div>
              )}

              {/* Error State */}
              {!isLoadingDrives && drivesErrorMessage && (
                <div className="state-card state-card--error" role="alert">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  <p className="state-message">{drivesErrorMessage}</p>
                  <button type="button" className="btn btn-secondary" onClick={loadDrives}>
                    Try Again
                  </button>
                </div>
              )}

              {/* Empty State */}
              {!isLoadingDrives && !drivesErrorMessage && filteredDrives.length === 0 && (
                <div className="state-card state-card--empty">
                  <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                  </svg>
                  <h3 className="empty-title">No matching placement drives</h3>
                  <p className="empty-desc">
                    {drives.length === 0
                      ? 'There are currently no placement drives listed by corporate recruiters.'
                      : 'Try broadening your search filters or clearing the CGPA threshold.'}
                  </p>
                  {(searchKeyword || filterLocation || filterCgpa !== 'all' || onlyEligible) && (
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => {
                        setSearchKeyword('')
                        setFilterLocation('')
                        setFilterCgpa('all')
                        setFilterType('all')
                        setOnlyEligible(false)
                      }}
                    >
                      Clear Filters
                    </button>
                  )}
                </div>
              )}

              {/* Populated Drives Grid (Talentry Soft Rounded Cards) */}
              {!isLoadingDrives && !drivesErrorMessage && filteredDrives.length > 0 && (
                <div className="talentry-cards-grid">
                  {filteredDrives.map((drive) => {
                    const avatar = getCompanyAvatar(drive.companyName)
                    const isApplied = applications.some(
                      (app) => app.placementDrive?.id === drive.id
                    )
                    const isClosed = (drive.status || '').toUpperCase() === 'CLOSED'
                    const isPastDeadline = Boolean(
                      drive.applicationDeadline &&
                      new Date(`${drive.applicationDeadline}T23:59:59`) < new Date()
                    )
                    const hasCgpaInfo =
                      user?.cgpa !== undefined &&
                      user?.cgpa !== null &&
                      drive.minimumCgpa !== undefined &&
                      drive.minimumCgpa !== null
                    const isIneligibleCgpa =
                      hasCgpaInfo && Number(user.cgpa) < Number(drive.minimumCgpa)
                    const isSubmittingThis = applyingDriveId === drive.id
                    const isBookmarked = bookmarkedDriveIds.has(drive.id)

                    return (
                      <div key={drive.id} className="talentry-card">
                        {/* Card Header: Avatar, Name, Verified Badge, Bookmark */}
                        <div className="talentry-card-header">
                          <div className="company-info-group">
                            <div className="company-avatar" style={{ backgroundColor: avatar.bg }}>
                              {avatar.initial}
                            </div>
                            <div>
                              <div className="company-name-row">
                                <span className="company-name">
                                  {drive.companyName || 'Corporate Recruiter'}
                                </span>
                                <span className="verified-badge" title="Verified Corporate Recruiter">
                                  ✓
                                </span>
                              </div>
                              <span className="post-date-text">
                                {drive.applicationDeadline
                                  ? `Deadline: ${drive.applicationDeadline}`
                                  : 'Open Opportunity'}
                              </span>
                            </div>
                          </div>

                          <button
                            type="button"
                            className={`bookmark-icon-btn ${isBookmarked ? 'active' : ''}`}
                            onClick={() => toggleBookmark(drive.id)}
                            aria-label="Bookmark drive"
                          >
                            <svg width="18" height="18" viewBox="0 0 24 24" fill={isBookmarked ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                            </svg>
                          </button>
                        </div>

                        {/* Job Title */}
                        <h3 className="talentry-card-title">
                          {drive.jobTitle || 'Placement Opportunity'}
                        </h3>

                        {/* Meta Tags Row */}
                        <div className="talentry-card-tags">
                          <span className="talentry-chip">
                            📍 {drive.location || 'Multiple Locations'}
                          </span>
                          <span className="talentry-chip talentry-chip--violet">
                            Min {drive.minimumCgpa != null ? drive.minimumCgpa : 'N/A'} CGPA
                          </span>
                          <span className={`talentry-chip ${isClosed ? 'talentry-chip--closed' : 'talentry-chip--mint'}`}>
                            {drive.status || 'OPEN'}
                          </span>
                        </div>

                        {/* Salary / Package */}
                        <div className="talentry-card-salary">
                          <span className="salary-label">CTC:</span>
                          <span className="salary-amount">
                            {drive.salary != null
                              ? `₹${Number(drive.salary).toLocaleString('en-IN')} / yr`
                              : 'Disclosed upon selection'}
                          </span>
                        </div>

                        {/* Job Description */}
                        <p className="talentry-card-desc">
                          {drive.jobDescription || 'Detailed job description and criteria provided by recruiter.'}
                        </p>

                        {/* Footer: Metrics & Action Pill */}
                        <div className="talentry-card-footer">
                          <div className="applicant-metric">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                              <circle cx="9" cy="7" r="4" />
                              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                            </svg>
                            <span>Verified Drive</span>
                          </div>

                          {/* Action Button: Apply / Applied / Ineligible */}
                          <div className="card-action-wrap">
                            {isApplied ? (
                              <button
                                type="button"
                                className="btn-apply btn-apply--applied"
                                disabled
                              >
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                  <polyline points="20 6 9 17 4 12" />
                                </svg>
                                <span>Applied</span>
                              </button>
                            ) : isClosed ? (
                              <button
                                type="button"
                                className="btn-apply btn-apply--disabled"
                                disabled
                              >
                                Drive Closed
                              </button>
                            ) : isPastDeadline ? (
                              <button
                                type="button"
                                className="btn-apply btn-apply--disabled"
                                disabled
                              >
                                Deadline Passed
                              </button>
                            ) : isIneligibleCgpa ? (
                              <button
                                type="button"
                                className="btn-apply btn-apply--ineligible"
                                disabled
                                title={`Your CGPA (${user.cgpa}) is below the required minimum (${drive.minimumCgpa})`}
                              >
                                Ineligible (&lt; {drive.minimumCgpa} CGPA)
                              </button>
                            ) : (
                              <button
                                type="button"
                                className="btn btn-primary btn-sm btn-apply"
                                onClick={() => handleApply(drive)}
                                disabled={applyingDriveId !== null}
                              >
                                {isSubmittingThis ? (
                                  <>
                                    <span className="btn-spinner" aria-hidden="true"></span>
                                    <span>Applying...</span>
                                  </>
                                ) : (
                                  <>
                                    <span>Apply Now</span>
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                                      <line x1="5" y1="12" x2="19" y2="12" />
                                      <polyline points="12 5 19 12 12 19" />
                                    </svg>
                                  </>
                                )}
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 2: MY APPLICATIONS TRACKER */}
          {activeTab === 'applications' && (
            <div className="applications-view">
              <div className="view-header-row">
                <div>
                  <h2 className="view-title">My Application Progression</h2>
                  <p className="view-subtitle">
                    Real-time status tracking for drives you have submitted to
                  </p>
                </div>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={loadApplications}
                  disabled={isLoadingApps}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="23 4 23 10 17 10" />
                    <polyline points="1 20 1 14 7 14" />
                    <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
                  </svg>
                  <span>Refresh</span>
                </button>
              </div>

              {/* Loading State */}
              {isLoadingApps && (
                <div className="state-card" role="status">
                  <div className="talentry-spinner"></div>
                  <p className="state-message">Loading your application status...</p>
                </div>
              )}

              {/* Error State */}
              {!isLoadingApps && appsErrorMessage && (
                <div className="state-card state-card--error" role="alert">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  <p className="state-message">{appsErrorMessage}</p>
                  <button type="button" className="btn btn-secondary" onClick={loadApplications}>
                    Try Again
                  </button>
                </div>
              )}

              {/* Empty State */}
              {!isLoadingApps && !appsErrorMessage && applications.length === 0 && (
                <div className="state-card state-card--empty">
                  <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                    <line x1="16" y1="13" x2="8" y2="13" />
                    <line x1="16" y1="17" x2="8" y2="17" />
                  </svg>
                  <h3 className="empty-title">No applications submitted yet</h3>
                  <p className="empty-desc">
                    Browse open placement drives and submit your application with a single click.
                  </p>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setActiveTab('drives')}
                  >
                    Browse Active Drives
                  </button>
                </div>
              )}

              {/* Populated Applications Table & Progression Cards */}
              {!isLoadingApps && !appsErrorMessage && applications.length > 0 && (
                <div className="talentry-table-card">
                  <div className="table-responsive">
                    <table className="talentry-table">
                      <thead>
                        <tr>
                          <th>Position & Company</th>
                          <th>Location</th>
                          <th>Date Applied</th>
                          <th>Application Status</th>
                          <th>Progression Milestone</th>
                        </tr>
                      </thead>
                      <tbody>
                        {applications.map((app) => {
                          const status = (app.status || 'APPLIED').toUpperCase()
                          let statusClass = 'applied'
                          let milestoneText = 'Application Under Review'

                          if (status === 'SHORTLISTED') {
                            statusClass = 'shortlisted'
                            milestoneText = 'Selected for Interview Round'
                          } else if (status === 'SELECTED') {
                            statusClass = 'selected'
                            milestoneText = 'Offer Letter Extended 🎉'
                          } else if (status === 'REJECTED') {
                            statusClass = 'rejected'
                            milestoneText = 'Process Concluded'
                          }

                          return (
                            <tr key={app.id}>
                              <td className="cell-role">
                                <div className="role-title-text">
                                  {app.placementDrive?.jobTitle || 'Placement Drive'}
                                </div>
                                <div className="role-company-text">
                                  {app.placementDrive?.companyName || 'Corporate Recruiter'}
                                  <span className="verified-badge" title="Verified">✓</span>
                                </div>
                              </td>
                              <td className="cell-muted">
                                {app.placementDrive?.location || 'Pune / Remote'}
                              </td>
                              <td className="cell-date">
                                {app.appliedDate || 'Recent'}
                              </td>
                              <td className="cell-status">
                                <span className={`talentry-status-badge talentry-status-badge--${statusClass}`}>
                                  {status}
                                </span>
                              </td>
                              <td className="cell-milestone">
                                <span className="milestone-text">{milestoneText}</span>
                              </td>
                            </tr>
                          )
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
