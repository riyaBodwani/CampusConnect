import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/useAuth'
import './RecruiterPage.css'

const INITIAL_FORM_DATA = {
  jobTitle: '',
  jobDescription: '',
  location: '',
  minimumCgpa: '',
  eligibilityCriteria: '',
  salary: '',
  applicationDeadline: '',
  status: 'OPEN',
}

export default function RecruiterPage() {
  const { user } = useAuth()
  const [drives, setDrives] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  // Form states
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [editingDriveId, setEditingDriveId] = useState(null)
  const [formData, setFormData] = useState(INITIAL_FORM_DATA)
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Delete confirmation modal states
  const [driveToDelete, setDriveToDelete] = useState(null)
  const [deleteError, setDeleteError] = useState('')
  const [isDeleting, setIsDeleting] = useState(false)

  // Applications state
  const [applications, setApplications] = useState([])
  const [isLoadingApps, setIsLoadingApps] = useState(true)
  const [appsErrorMessage, setAppsErrorMessage] = useState('')
  const [updatingAppId, setUpdatingAppId] = useState(null)
  const [applicantSuccessMessage, setApplicantSuccessMessage] = useState('')
  const [applicantErrorMessage, setApplicantErrorMessage] = useState('')

  // Filters for applicants table
  const [applicantSearch, setApplicantSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [driveFilter, setDriveFilter] = useState('ALL')

  // Auto-dismiss applicant update success message
  useEffect(() => {
    if (!applicantSuccessMessage) return
    const timer = setTimeout(() => {
      setApplicantSuccessMessage('')
    }, 5000)
    return () => clearTimeout(timer)
  }, [applicantSuccessMessage])

  // Auto-dismiss applicant update error message
  useEffect(() => {
    if (!applicantErrorMessage) return
    const timer = setTimeout(() => {
      setApplicantErrorMessage('')
    }, 6000)
    return () => clearTimeout(timer)
  }, [applicantErrorMessage])

  // Auto-dismiss general success message
  useEffect(() => {
    if (!successMessage) return
    const timer = setTimeout(() => {
      setSuccessMessage('')
    }, 5000)
    return () => clearTimeout(timer)
  }, [successMessage])

  const loadDrives = () => {
    setIsLoading(true)
    setErrorMessage('')

    apiClient
      .get('/recruiter/placement-drives')
      .then((response) => {
        setDrives(Array.isArray(response.data) ? response.data : [])
      })
      .catch((err) => {
        if (err.response?.status === 401) {
          setErrorMessage('You must be signed in as a recruiter to view this dashboard.')
        } else if (err.response?.status === 403) {
          setErrorMessage('Access denied. This dashboard is restricted to recruiter accounts.')
        } else if (err.response?.data?.message) {
          setErrorMessage(err.response.data.message)
        } else if (err.request) {
          setErrorMessage('Unable to connect to the server. Please ensure the backend is running.')
        } else {
          setErrorMessage(err.message || 'An error occurred while loading placement drives.')
        }
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  const loadApplications = () => {
    setIsLoadingApps(true)
    setAppsErrorMessage('')

    apiClient
      .get('/recruiter/applications')
      .then((response) => {
        setApplications(Array.isArray(response.data) ? response.data : [])
      })
      .catch((err) => {
        if (err.response?.status === 401) {
          setAppsErrorMessage('You must be signed in as a recruiter to view applicants.')
        } else if (err.response?.status === 403) {
          setAppsErrorMessage('Access denied. This dashboard is restricted to recruiter accounts.')
        } else if (err.response?.data?.message) {
          setAppsErrorMessage(err.response.data.message)
        } else if (err.request) {
          setAppsErrorMessage('Unable to connect to the server. Please ensure the backend is running.')
        } else {
          setAppsErrorMessage(err.message || 'An error occurred while loading applicants.')
        }
      })
      .finally(() => {
        setIsLoadingApps(false)
      })
  }

  useEffect(() => {
    loadDrives()
    loadApplications()
  }, [])

  // Status Change Handler
  const handleStatusChange = async (applicationId, newStatus, currentStatus) => {
    if (!applicationId || !newStatus || newStatus === currentStatus) return
    if (updatingAppId !== null) return

    setUpdatingAppId(applicationId)
    setApplicantSuccessMessage('')
    setApplicantErrorMessage('')

    try {
      const response = await apiClient.patch(
        `/recruiter/applications/${applicationId}/status`,
        { status: newStatus }
      )

      const updatedData = response.data
      const updatedStatus = updatedData?.status || newStatus

      setApplications((prevApps) =>
        prevApps.map((app) => {
          if (app.id === applicationId) {
            return {
              ...app,
              ...(updatedData && typeof updatedData === 'object' ? updatedData : {}),
              status: updatedStatus,
            }
          }
          return app
        })
      )

      const targetApp = applications.find((a) => a.id === applicationId)
      const candidateName = targetApp?.student?.fullName || 'Candidate'
      setApplicantSuccessMessage(
        `Application status for ${candidateName} updated to "${updatedStatus}" successfully!`
      )
    } catch (err) {
      if (err.response?.data?.message) {
        setApplicantErrorMessage(err.response.data.message)
      } else if (err.request) {
        setApplicantErrorMessage('Unable to connect to server to update status.')
      } else {
        setApplicantErrorMessage(err.message || 'Failed to update application status.')
      }
    } finally {
      setUpdatingAppId(null)
    }
  }

  // Form input handler
  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (formError) setFormError('')
  }

  // Open form in create mode
  const handleOpenCreateForm = () => {
    setEditingDriveId(null)
    setFormData(INITIAL_FORM_DATA)
    setFormError('')
    setIsFormOpen(true)
  }

  // Open form in edit mode
  const handleStartEdit = (drive) => {
    setEditingDriveId(drive.id)
    setFormData({
      jobTitle: drive.jobTitle || '',
      jobDescription: drive.jobDescription || '',
      location: drive.location || '',
      minimumCgpa: drive.minimumCgpa != null ? String(drive.minimumCgpa) : '',
      eligibilityCriteria: drive.eligibilityCriteria || '',
      salary: drive.salary != null ? String(drive.salary) : '',
      applicationDeadline: drive.applicationDeadline || '',
      status: (drive.status || 'OPEN').toUpperCase(),
    })
    setFormError('')
    setIsFormOpen(true)
  }

  const handleCancelForm = () => {
    setIsFormOpen(false)
    setEditingDriveId(null)
    setFormData(INITIAL_FORM_DATA)
    setFormError('')
  }

  // Delete modal handlers
  const handleOpenDeleteModal = (drive) => {
    setDriveToDelete(drive)
    setDeleteError('')
  }

  const handleCancelDelete = () => {
    if (isDeleting) return
    setDriveToDelete(null)
  }

  const handleConfirmDelete = async () => {
    if (!driveToDelete || isDeleting) return
    const driveId = driveToDelete.id
    const title = driveToDelete.jobTitle || 'Placement drive'

    setIsDeleting(true)
    setDeleteError('')
    setSuccessMessage('')

    try {
      await apiClient.delete(`/recruiter/placement-drives/${driveId}`)
      setDriveToDelete(null)
      setSuccessMessage(`Placement drive "${title}" has been deleted successfully!`)
      loadDrives()
      loadApplications()
    } catch (err) {
      setDriveToDelete(null)
      if (err.response?.status === 409) {
        setDeleteError(
          `Cannot delete placement drive "${title}" because student applications are already associated with it.`
        )
      } else {
        setDeleteError(
          err.response?.data?.message || `Failed to delete placement drive "${title}".`
        )
      }
    } finally {
      setIsDeleting(false)
    }
  }

  // Form Validation & Submission
  const validateForm = () => {
    if (!formData.jobTitle.trim()) {
      setFormError('Job Title is required.')
      return false
    }
    if (!formData.applicationDeadline) {
      setFormError('Application Deadline is required.')
      return false
    }
    if (formData.minimumCgpa !== '' && formData.minimumCgpa != null) {
      const cgpa = Number(formData.minimumCgpa)
      if (isNaN(cgpa) || cgpa < 0 || cgpa > 10) {
        setFormError('Minimum CGPA must be a valid number between 0.0 and 10.0.')
        return false
      }
    }
    return true
  }

  const handleSubmitDrive = async (e) => {
    e.preventDefault()
    if (!validateForm()) return

    setIsSubmitting(true)
    setFormError('')

    const payload = {
      jobTitle: formData.jobTitle.trim(),
      jobDescription: formData.jobDescription.trim() || null,
      location: formData.location.trim() || null,
      minimumCgpa: formData.minimumCgpa !== '' && formData.minimumCgpa != null ? Number(formData.minimumCgpa) : null,
      eligibilityCriteria: formData.eligibilityCriteria.trim() || null,
      salary: formData.salary !== '' && formData.salary != null ? Number(formData.salary) : null,
      applicationDeadline: formData.applicationDeadline,
      status: (formData.status || 'OPEN').toUpperCase(),
    }

    try {
      if (editingDriveId) {
        await apiClient.put(`/recruiter/placement-drives/${editingDriveId}`, payload)
        setSuccessMessage(`Placement drive "${payload.jobTitle}" updated successfully!`)
      } else {
        await apiClient.post('/recruiter/placement-drives', payload)
        setSuccessMessage(`Placement drive "${payload.jobTitle}" published successfully!`)
      }
      setIsFormOpen(false)
      setEditingDriveId(null)
      setFormData(INITIAL_FORM_DATA)
      loadDrives()
    } catch (err) {
      if (err.response?.data?.message) {
        setFormError(err.response.data.message)
      } else {
        setFormError(err.message || 'Failed to save placement drive. Please try again.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  // Filtered applicants
  const filteredApplicants = useMemo(() => {
    return applications.filter((app) => {
      // Search
      if (applicantSearch.trim()) {
        const query = applicantSearch.toLowerCase()
        const nameMatch = (app.student?.fullName || '').toLowerCase().includes(query)
        const emailMatch = (app.student?.email || '').toLowerCase().includes(query)
        const driveMatch = (app.placementDrive?.jobTitle || '').toLowerCase().includes(query)
        if (!nameMatch && !emailMatch && !driveMatch) return false
      }

      // Status
      if (statusFilter !== 'ALL') {
        if ((app.status || '').toUpperCase() !== statusFilter) return false
      }

      // Drive
      if (driveFilter !== 'ALL') {
        if (String(app.placementDrive?.id) !== String(driveFilter)) return false
      }

      return true
    })
  }, [applications, applicantSearch, statusFilter, driveFilter])

  // KPI Calculations
  const totalDrives = drives.length
  const activeDrives = drives.filter((d) => (d.status || '').toUpperCase() === 'OPEN').length
  const totalApplicants = applications.length
  const shortlistedCount = applications.filter((a) => (a.status || '').toUpperCase() === 'SHORTLISTED').length
  const selectedCount = applications.filter((a) => (a.status || '').toUpperCase() === 'SELECTED').length

  const companyName = user?.companyName || 'TechNova Solutions'

  return (
    <div className="recruiter-page-container">
      {/* 1. Header & Welcome Area */}
      <header className="recruiter-header">
        <div className="recruiter-title-group">
          <div className="company-badge-wrap">
            <span className="recruiter-role-pill">Corporate Recruiter</span>
            <span className="verified-badge" title="Verified Enterprise Partner">✓</span>
          </div>
          <h1 className="recruiter-main-title">
            Recruiter Workspace — {companyName}
          </h1>
          <p className="recruiter-subtitle">
            Manage your corporate placement drives, candidate qualification screening, and recruitment progression.
          </p>
        </div>

        {/* Action Button: Create Drive */}
        <div className="recruiter-header-actions">
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleOpenCreateForm}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            <span>Post Placement Drive</span>
          </button>
        </div>
      </header>

      {/* Global Alerts */}
      {successMessage && (
        <div className="feedback-banner feedback-banner--success" role="status">
          <span>{successMessage}</span>
          <button type="button" className="feedback-dismiss-btn" onClick={() => setSuccessMessage('')}>
            &times;
          </button>
        </div>
      )}

      {deleteError && (
        <div className="feedback-banner feedback-banner--error" role="alert">
          <span>{deleteError}</span>
          <button type="button" className="feedback-dismiss-btn" onClick={() => setDeleteError('')}>
            &times;
          </button>
        </div>
      )}

      {applicantSuccessMessage && (
        <div className="feedback-banner feedback-banner--success" role="status">
          <span>{applicantSuccessMessage}</span>
          <button type="button" className="feedback-dismiss-btn" onClick={() => setApplicantSuccessMessage('')}>
            &times;
          </button>
        </div>
      )}

      {applicantErrorMessage && (
        <div className="feedback-banner feedback-banner--error" role="alert">
          <span>{applicantErrorMessage}</span>
          <button type="button" className="feedback-dismiss-btn" onClick={() => setApplicantErrorMessage('')}>
            &times;
          </button>
        </div>
      )}

      {/* 2. KPI Metric Cards (Talentry Soft Rounded Architecture) */}
      <section className="kpi-cards-grid">
        <div className="kpi-card">
          <div className="kpi-card-header">
            <span className="kpi-label">Placement Drives</span>
            <div className="kpi-icon-wrap violet">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
              </svg>
            </div>
          </div>
          <div className="kpi-value">{totalDrives}</div>
          <div className="kpi-subtext">
            <strong>{activeDrives}</strong> currently accepting candidates
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-card-header">
            <span className="kpi-label">Total Applications</span>
            <div className="kpi-icon-wrap mint">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
          </div>
          <div className="kpi-value">{totalApplicants}</div>
          <div className="kpi-subtext">Candidates across all company drives</div>
        </div>

        <div className="kpi-card">
          <div className="kpi-card-header">
            <span className="kpi-label">Shortlisted</span>
            <div className="kpi-icon-wrap amber">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 14 14" />
              </svg>
            </div>
          </div>
          <div className="kpi-value">{shortlistedCount}</div>
          <div className="kpi-subtext">Ready for interview stage</div>
        </div>

        <div className="kpi-card">
          <div className="kpi-card-header">
            <span className="kpi-label">Selected</span>
            <div className="kpi-icon-wrap mint">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
          </div>
          <div className="kpi-value">{selectedCount}</div>
          <div className="kpi-subtext">Job offers successfully extended</div>
        </div>
      </section>

      {/* 3. Placement Drives Section */}
      <section className="drives-management-section">
        <div className="section-title-row">
          <div>
            <h2 className="section-title">My Placement Drives</h2>
            <p className="section-subtitle">
              Manage drive specifications, eligibility cutoffs, and application deadlines
            </p>
          </div>
        </div>

        {/* Drives Grid */}
        {isLoading ? (
          <div className="state-card">
            <div className="talentry-spinner"></div>
            <p className="state-message">Loading placement drives...</p>
          </div>
        ) : drives.length === 0 ? (
          <div className="state-card state-card--empty">
            <h3 className="empty-title">No placement drives yet</h3>
            <p className="empty-desc">
              Post your first campus hiring drive to start receiving qualified student applications.
            </p>
            <button type="button" className="btn btn-primary" onClick={handleOpenCreateForm}>
              Post Placement Drive
            </button>
          </div>
        ) : (
          <div className="recruiter-drives-grid">
            {drives.map((drive) => {
              const driveAppCount = applications.filter(
                (app) => app.placementDrive?.id === drive.id
              ).length
              const isClosed = (drive.status || '').toUpperCase() === 'CLOSED'

              return (
                <div key={drive.id} className="recruiter-drive-card">
                  <div className="drive-card-top">
                    <span className={`status-pill ${isClosed ? 'status-pill--closed' : 'status-pill--open'}`}>
                      {drive.status || 'OPEN'}
                    </span>
                    <span className="drive-deadline-badge">
                      Deadline: {drive.applicationDeadline || 'Open'}
                    </span>
                  </div>

                  <h3 className="recruiter-drive-title">{drive.jobTitle}</h3>

                  <div className="drive-specs-grid">
                    <div className="spec-item">
                      <span className="spec-label">Location:</span>
                      <span className="spec-val">{drive.location || 'Pune / Hybrid'}</span>
                    </div>
                    <div className="spec-item">
                      <span className="spec-label">Min CGPA:</span>
                      <span className="spec-val">{drive.minimumCgpa != null ? drive.minimumCgpa : 'N/A'}</span>
                    </div>
                    <div className="spec-item">
                      <span className="spec-label">Salary / CTC:</span>
                      <span className="spec-val spec-val--salary">
                        {drive.salary != null ? `₹${Number(drive.salary).toLocaleString('en-IN')}` : 'Not disclosed'}
                      </span>
                    </div>
                    <div className="spec-item">
                      <span className="spec-label">Applicants:</span>
                      <span className="spec-val spec-val--count">{driveAppCount} candidates</span>
                    </div>
                  </div>

                  <div className="recruiter-drive-actions">
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleStartEdit(drive)}
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                      <span>Edit Drive</span>
                    </button>
                    <button
                      type="button"
                      className="btn-delete-drive"
                      onClick={() => handleOpenDeleteModal(drive)}
                      title={driveAppCount > 0 ? 'Cannot delete drive with active applications' : 'Delete placement drive'}
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polyline points="3 6 5 6 21 6" />
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      </svg>
                      <span>Delete</span>
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </section>

      {/* 4. Candidate Review Pipeline (Interactive Status Updates) */}
      <section className="pipeline-section">
        <div className="section-title-row">
          <div>
            <h2 className="section-title">Candidate Review Pipeline</h2>
            <p className="section-subtitle">
              Review student applications and update evaluation status in real time
            </p>
          </div>
        </div>

        {/* Pipeline Toolbar */}
        <div className="pipeline-toolbar-card">
          <div className="pipeline-toolbar-inputs">
            {/* Search */}
            <div className="pipeline-search-wrap">
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input
                type="text"
                className="pipeline-input"
                placeholder="Search candidate by name or email..."
                value={applicantSearch}
                onChange={(e) => setApplicantSearch(e.target.value)}
              />
            </div>

            {/* Filter by Status */}
            <div className="pipeline-select-wrap">
              <select
                className="pipeline-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">All Statuses</option>
                <option value="APPLIED">APPLIED</option>
                <option value="SHORTLISTED">SHORTLISTED</option>
                <option value="SELECTED">SELECTED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </div>

            {/* Filter by Drive */}
            <div className="pipeline-select-wrap">
              <select
                className="pipeline-select"
                value={driveFilter}
                onChange={(e) => setDriveFilter(e.target.value)}
              >
                <option value="ALL">All Placement Drives</option>
                {drives.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.jobTitle}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Applications Table */}
        {isLoadingApps ? (
          <div className="state-card">
            <div className="talentry-spinner"></div>
            <p className="state-message">Loading candidate applications...</p>
          </div>
        ) : filteredApplicants.length === 0 ? (
          <div className="state-card state-card--empty">
            <h3 className="empty-title">No candidate applications found</h3>
            <p className="empty-desc">
              {applications.length === 0
                ? 'No students have applied to your active drives yet.'
                : 'No candidates matched the current search and status filters.'}
            </p>
          </div>
        ) : (
          <div className="talentry-table-card">
            <div className="table-responsive">
              <table className="talentry-table">
                <thead>
                  <tr>
                    <th>Candidate Details</th>
                    <th>Academic Profile</th>
                    <th>Applied Position</th>
                    <th>Applied Date</th>
                    <th>Status Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredApplicants.map((app) => {
                    const student = app.student || {}
                    const drive = app.placementDrive || {}
                    const status = (app.status || 'APPLIED').toUpperCase()
                    const isUpdating = updatingAppId === app.id
                    const initial = student.fullName ? student.fullName.charAt(0).toUpperCase() : 'S'

                    return (
                      <tr key={app.id}>
                        <td className="cell-candidate">
                          <div className="candidate-group">
                            <div className="candidate-avatar">{initial}</div>
                            <div>
                              <div className="candidate-name">{student.fullName || 'Student'}</div>
                              <div className="candidate-email">{student.email || '—'}</div>
                            </div>
                          </div>
                        </td>

                        <td className="cell-academic">
                          <div className="academic-line">
                            <span className="cgpa-badge">
                              {student.cgpa != null ? `${student.cgpa} CGPA` : 'N/A'}
                            </span>
                            <span className="branch-text">{student.branch || student.course || 'Engineering'}</span>
                          </div>
                        </td>

                        <td className="cell-drive">
                          <div className="drive-applied-title">{drive.jobTitle || 'Drive'}</div>
                          <div className="drive-applied-meta">{drive.location || 'Pune'}</div>
                        </td>

                        <td className="cell-date">
                          {app.appliedDate || 'Recent'}
                        </td>

                        <td className="cell-status-action">
                          <div className="status-dropdown-wrap">
                            <select
                              className={`status-select status-select--${status.toLowerCase()}`}
                              value={status}
                              disabled={isUpdating}
                              onChange={(e) =>
                                handleStatusChange(app.id, e.target.value, status)
                              }
                            >
                              <option value="APPLIED">APPLIED</option>
                              <option value="SHORTLISTED">SHORTLISTED</option>
                              <option value="SELECTED">SELECTED</option>
                              <option value="REJECTED">REJECTED</option>
                            </select>
                            {isUpdating && (
                              <span className="status-updating-spinner" aria-hidden="true"></span>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </section>

      {/* 5. Create / Edit Placement Drive Modal */}
      {isFormOpen && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog--form">
            <div className="modal-header">
              <h3 className="modal-title">
                {editingDriveId ? 'Edit Placement Drive' : 'Post New Placement Drive'}
              </h3>
              <button
                type="button"
                className="modal-close-btn"
                onClick={handleCancelForm}
                aria-label="Close modal"
              >
                &times;
              </button>
            </div>

            {formError && (
              <div className="modal-error-banner" role="alert">
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmitDrive} className="modal-form">
              <div className="modal-form-grid">
                {/* Job Title */}
                <div className="modal-form-group modal-grid-full">
                  <label htmlFor="jobTitle" className="modal-label">
                    Job Title <span className="required-star">*</span>
                  </label>
                  <input
                    id="jobTitle"
                    name="jobTitle"
                    type="text"
                    className="modal-input"
                    placeholder="e.g. Associate Software Engineer"
                    value={formData.jobTitle}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                {/* Location */}
                <div className="modal-form-group">
                  <label htmlFor="location" className="modal-label">Location</label>
                  <input
                    id="location"
                    name="location"
                    type="text"
                    className="modal-input"
                    placeholder="e.g. Pune / Hybrid"
                    value={formData.location}
                    onChange={handleInputChange}
                  />
                </div>

                {/* Application Deadline */}
                <div className="modal-form-group">
                  <label htmlFor="applicationDeadline" className="modal-label">
                    Application Deadline <span className="required-star">*</span>
                  </label>
                  <input
                    id="applicationDeadline"
                    name="applicationDeadline"
                    type="date"
                    className="modal-input"
                    value={formData.applicationDeadline}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                {/* Min CGPA */}
                <div className="modal-form-group">
                  <label htmlFor="minimumCgpa" className="modal-label">Minimum CGPA (0 - 10)</label>
                  <input
                    id="minimumCgpa"
                    name="minimumCgpa"
                    type="number"
                    step="0.1"
                    min="0"
                    max="10"
                    className="modal-input"
                    placeholder="e.g. 7.0"
                    value={formData.minimumCgpa}
                    onChange={handleInputChange}
                  />
                </div>

                {/* Salary */}
                <div className="modal-form-group">
                  <label htmlFor="salary" className="modal-label">Annual Salary / CTC (₹)</label>
                  <input
                    id="salary"
                    name="salary"
                    type="number"
                    min="0"
                    className="modal-input"
                    placeholder="e.g. 850000"
                    value={formData.salary}
                    onChange={handleInputChange}
                  />
                </div>

                {/* Status */}
                <div className="modal-form-group">
                  <label htmlFor="status" className="modal-label">Drive Status</label>
                  <select
                    id="status"
                    name="status"
                    className="modal-select"
                    value={formData.status}
                    onChange={handleInputChange}
                  >
                    <option value="OPEN">OPEN (Accepting Applications)</option>
                    <option value="CLOSED">CLOSED</option>
                  </select>
                </div>

                {/* Eligibility Criteria */}
                <div className="modal-form-group modal-grid-full">
                  <label htmlFor="eligibilityCriteria" className="modal-label">Eligibility Criteria</label>
                  <input
                    id="eligibilityCriteria"
                    name="eligibilityCriteria"
                    type="text"
                    className="modal-input"
                    placeholder="e.g. B.Tech Computer Science / IT with no backlogs"
                    value={formData.eligibilityCriteria}
                    onChange={handleInputChange}
                  />
                </div>

                {/* Description */}
                <div className="modal-form-group modal-grid-full">
                  <label htmlFor="jobDescription" className="modal-label">Job Description</label>
                  <textarea
                    id="jobDescription"
                    name="jobDescription"
                    rows="3"
                    className="modal-textarea"
                    placeholder="Outline job responsibilities, skills, and compensation details..."
                    value={formData.jobDescription}
                    onChange={handleInputChange}
                  ></textarea>
                </div>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCancelForm}
                  disabled={isSubmitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Saving...' : editingDriveId ? 'Update Drive' : 'Publish Drive'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 6. Delete Confirmation Modal */}
      {driveToDelete && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog--sm">
            <div className="modal-header">
              <h3 className="modal-title">Delete Placement Drive</h3>
              <button
                type="button"
                className="modal-close-btn"
                onClick={handleCancelDelete}
                aria-label="Close"
              >
                &times;
              </button>
            </div>
            <div className="modal-body-content">
              <p>
                Are you sure you want to delete the placement drive{' '}
                <strong>&quot;{driveToDelete.jobTitle}&quot;</strong>?
              </p>
              <p className="modal-warning-text">
                Note: Drives with existing student applications cannot be deleted.
              </p>
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleCancelDelete}
                disabled={isDeleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-delete-confirm"
                onClick={handleConfirmDelete}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete Drive'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
