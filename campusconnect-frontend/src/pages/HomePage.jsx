import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './HomePage.css'

export default function HomePage() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')
  const [locationQuery, setLocationQuery] = useState('')
  const [cgpaFilter, setCgpaFilter] = useState('all')
  const [driveType, setDriveType] = useState('all')

  const popularTags = [
    'Java Backend',
    'React Frontend',
    'Full Stack',
    'Data Analyst',
    'Cloud & DevOps',
    'AI / ML Engineer',
    'Pune',
    'Bangalore',
  ]

  const handleChipClick = (tag) => {
    setSearchQuery(tag)
  }

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    // Navigate to student portal with search query params
    const params = new URLSearchParams()
    if (searchQuery.trim()) params.set('q', searchQuery.trim())
    if (locationQuery.trim()) params.set('loc', locationQuery.trim())
    if (cgpaFilter !== 'all') params.set('cgpa', cgpaFilter)
    if (driveType !== 'all') params.set('type', driveType)
    navigate(`/student?${params.toString()}`)
  }

  // Curated demo cards replicating Talentry visual style
  const featuredDrives = [
    {
      id: 1,
      company: 'TechNova Solutions',
      initial: 'T',
      avatarBg: '#6366F1',
      verified: true,
      role: 'Associate Software Engineer',
      type: 'Full-Time',
      location: 'Pune / Hybrid',
      cgpa: '7.0 CGPA',
      salary: '₹8,50,000 / yr',
      applicants: 42,
      deadline: 'Jul 28, 2026',
    },
    {
      id: 2,
      company: 'CloudScale Networks',
      initial: 'C',
      avatarBg: '#0EA5E9',
      verified: true,
      role: 'Full Stack Web Developer',
      type: 'Full-Time',
      location: 'Bangalore / On-site',
      cgpa: '6.5 CGPA',
      salary: '₹10,20,000 / yr',
      applicants: 68,
      deadline: 'Aug 05, 2026',
    },
    {
      id: 3,
      company: 'Aura Technologies',
      initial: 'A',
      avatarBg: '#8B5CF6',
      verified: true,
      role: 'Machine Learning Associate',
      type: 'Internship + PPO',
      location: 'Hyderabad / Remote',
      cgpa: '8.0 CGPA',
      salary: '₹12,00,000 / yr',
      applicants: 89,
      deadline: 'Aug 12, 2026',
    },
  ]

  return (
    <div className="home-page">
      {/* 1. Hero Section (Talentry Inspired) */}
      <section className="hero-section">
        <div className="hero-badge">
          <span className="hero-badge-dot"></span>
          <span>Campus Placement Management Platform 3.0</span>
        </div>

        <h1 className="hero-title">
          Bridging <span className="text-violet">Campus Talent</span> and{' '}
          <span className="text-mint">Verified Careers</span>
        </h1>

        <p className="hero-subtitle">
          CampusConnect unites universities, ambitious students, and leading corporate recruiters
          in a single, transparent placement platform. Discover verified placement drives,
          verify automated eligibility, and track application progression in real time.
        </p>

        <div className="hero-cta-group">
          <Link to="/student" className="btn btn-primary btn-lg">
            <span>Explore as Student</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
          <Link to="/recruiter" className="btn btn-secondary btn-lg">
            <span>Hire Campus Talent</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </div>

        {/* Hero Trust Highlights */}
        <div className="hero-highlights">
          <div className="highlight-item">
            <span className="highlight-icon-wrap mint">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="M9 12l2 2 4-4" />
              </svg>
            </span>
            <span>100% Verified Drives</span>
          </div>

          <div className="highlight-item">
            <span className="highlight-icon-wrap violet">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
              </svg>
            </span>
            <span>Live Application Status</span>
          </div>

          <div className="highlight-item">
            <span className="highlight-icon-wrap mint">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
            </span>
            <span>Automated CGPA Check</span>
          </div>

          <div className="highlight-item">
            <span className="highlight-icon-wrap violet">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </span>
            <span>Company Isolation</span>
          </div>
        </div>
      </section>

      {/* 2. Large Horizontal Search & Filter Toolbar (Talentry Style) */}
      <section className="search-section" aria-label="Placement Drive Search Toolbar">
        <div className="talentry-toolbar-card">
          <form className="talentry-toolbar-form" onSubmit={handleSearchSubmit}>
            {/* Field 1: Job Title / Keywords */}
            <div className="toolbar-field">
              <label htmlFor="home-search-keyword" className="toolbar-label">
                Job Title / Keywords
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <input
                  id="home-search-keyword"
                  type="text"
                  className="toolbar-input"
                  placeholder="e.g. Software Engineer, Backend"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            {/* Field 2: Location */}
            <div className="toolbar-field">
              <label htmlFor="home-search-location" className="toolbar-label">
                Location
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <input
                  id="home-search-location"
                  type="text"
                  className="toolbar-input"
                  placeholder="e.g. Pune, Bangalore, Remote"
                  value={locationQuery}
                  onChange={(e) => setLocationQuery(e.target.value)}
                />
              </div>
            </div>

            {/* Field 3: Min CGPA */}
            <div className="toolbar-field">
              <label htmlFor="home-search-cgpa" className="toolbar-label">
                Min CGPA
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
                  <path d="M6 12v5c3 3 9 3 12 0v-5" />
                </svg>
                <select
                  id="home-search-cgpa"
                  className="toolbar-select"
                  value={cgpaFilter}
                  onChange={(e) => setCgpaFilter(e.target.value)}
                >
                  <option value="all">All CGPA</option>
                  <option value="6.0">Min 6.0 CGPA</option>
                  <option value="7.0">Min 7.0 CGPA</option>
                  <option value="8.0">Min 8.0 CGPA</option>
                </select>
              </div>
            </div>

            {/* Field 4: Job Type */}
            <div className="toolbar-field">
              <label htmlFor="home-search-type" className="toolbar-label">
                Job Type
              </label>
              <div className="toolbar-input-wrap">
                <svg className="toolbar-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                </svg>
                <select
                  id="home-search-type"
                  className="toolbar-select"
                  value={driveType}
                  onChange={(e) => setDriveType(e.target.value)}
                >
                  <option value="all">All Job Types</option>
                  <option value="Full-Time">Full-Time</option>
                  <option value="Internship">Internship</option>
                  <option value="Contract">Contract</option>
                </select>
              </div>
            </div>

            {/* Search Pill Button */}
            <div className="toolbar-button-wrap">
              <button type="submit" className="btn btn-primary toolbar-search-btn">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <span>Search</span>
              </button>
            </div>
          </form>

          {/* Popular Tag Chips */}
          <div className="toolbar-chips-row">
            <span className="toolbar-chips-label">Popular Searches:</span>
            <div className="toolbar-chips-list">
              {popularTags.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  className="talentry-chip"
                  onClick={() => handleChipClick(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* 3. Featured Placement Drives (Talentry Soft Rounded Cards) */}
      <section className="featured-section">
        <div className="section-header-row">
          <div>
            <h2 className="section-title">Active Placement Opportunities</h2>
            <p className="section-subtitle">
              Explore recently opened drives with instant automated eligibility verification
            </p>
          </div>
          <Link to="/student" className="btn btn-outline btn-sm">
            <span>View All Drives</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </div>

        <div className="talentry-cards-grid">
          {featuredDrives.map((drive) => (
            <div key={drive.id} className="talentry-card">
              {/* Card Header: Avatar, Name, Verified Badge, Bookmark */}
              <div className="talentry-card-header">
                <div className="company-info-group">
                  <div className="company-avatar" style={{ backgroundColor: drive.avatarBg }}>
                    {drive.initial}
                  </div>
                  <div>
                    <div className="company-name-row">
                      <span className="company-name">{drive.company}</span>
                      {drive.verified && (
                        <span className="verified-badge" title="Verified Corporate Recruiter">
                          ✓
                        </span>
                      )}
                    </div>
                    <span className="post-date-text">Deadline: {drive.deadline}</span>
                  </div>
                </div>
                <button type="button" className="bookmark-icon-btn" aria-label="Bookmark drive">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                  </svg>
                </button>
              </div>

              {/* Job Title */}
              <h3 className="talentry-card-title">{drive.role}</h3>

              {/* Tags Row */}
              <div className="talentry-card-tags">
                <span className="talentry-chip">{drive.type}</span>
                <span className="talentry-chip">{drive.location}</span>
                <span className="talentry-chip talentry-chip--violet">{drive.cgpa}</span>
              </div>

              {/* Salary / CTC Display */}
              <div className="talentry-card-salary">
                <span className="salary-label">Package:</span>
                <span className="salary-amount">{drive.salary}</span>
              </div>

              {/* Card Footer: Metrics & Action Pill */}
              <div className="talentry-card-footer">
                <div className="applicant-metric">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="9" cy="7" r="4" />
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                  </svg>
                  <span>{drive.applicants} applicants</span>
                </div>
                <Link to="/student" className="btn btn-primary btn-sm">
                  Apply Now
                </Link>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 4. Three-Step Workflow Architecture */}
      <section className="workflow-section">
        <h2 className="section-title text-center">How CampusConnect Works</h2>
        <p className="section-subtitle text-center">
          Engineered for frictionless recruitment workflows between colleges and corporations
        </p>

        <div className="workflow-steps-grid">
          <div className="workflow-step-card">
            <div className="step-number-badge">01</div>
            <h3 className="step-title">Verified Profile & Drive Discovery</h3>
            <p className="step-desc">
              Students build comprehensive academic profiles. Corporate recruiters publish vetted placement drives with strict branch and CGPA requirements.
            </p>
          </div>

          <div className="workflow-step-card">
            <div className="step-number-badge">02</div>
            <h3 className="step-title">Instant Eligibility Engine</h3>
            <p className="step-desc">
              CampusConnect evaluates eligibility rules instantaneously. Ineligible or duplicate submissions are prevented before reaching recruiter review queues.
            </p>
          </div>

          <div className="workflow-step-card">
            <div className="step-number-badge">03</div>
            <h3 className="step-title">Real-Time Progression Tracking</h3>
            <p className="step-desc">
              Recruiters manage candidate review pipelines with one click. Students receive immediate status transitions from Applied to Shortlisted to Selected.
            </p>
          </div>
        </div>
      </section>

      {/* 5. Pathway Split Banner */}
      <section className="pathways-section">
        <div className="pathway-card student-pathway">
          <div className="pathway-content">
            <span className="talentry-chip talentry-chip--mint">For Students</span>
            <h3 className="pathway-title">Accelerate Your Career Launch</h3>
            <p className="pathway-desc">
              Access campus-exclusive hiring drives, verify your qualification match instantly, and track every phase of your application.
            </p>
            <Link to="/student" className="btn btn-primary">
              Browse Student Portal
            </Link>
          </div>
        </div>

        <div className="pathway-card recruiter-pathway">
          <div className="pathway-content">
            <span className="talentry-chip talentry-chip--violet">For Recruiters</span>
            <h3 className="pathway-title">Acquire Top Verified Campus Talent</h3>
            <p className="pathway-desc">
              Publish customized drives, filter candidates by department & CGPA, and streamline your entire on-campus hiring process.
            </p>
            <Link to="/recruiter" className="btn btn-secondary">
              Open Recruiter Workspace
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}
