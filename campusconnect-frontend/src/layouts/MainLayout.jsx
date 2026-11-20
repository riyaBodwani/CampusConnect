import { useState, useEffect } from 'react'
import { NavLink, Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import Logo from '../components/Logo'
import ThemeToggle from '../components/ThemeToggle'
import './MainLayout.css'

export default function MainLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { isAuthenticated, user, role, logout } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const isHome = location.pathname === '/'
  const currentYear = new Date().getFullYear()

  // Close mobile drawer on route change
  useEffect(() => {
    setMobileMenuOpen(false)
  }, [location.pathname])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const userName = user?.fullName || user?.name || ''
  const userInitial = userName ? userName.charAt(0).toUpperCase() : (role === 'recruiter' ? 'R' : 'S')

  return (
    <div className="app-shell">
      {/* Top Floating / Sticky Header (Talentry Navigation) */}
      <header className="app-header">
        <div className="header-container">
          {/* Logo on Left */}
          <NavLink to="/" className="app-brand" aria-label="CampusConnect Home">
            <Logo size="sm" />
          </NavLink>

          {/* Desktop Center Navigation Pills */}
          <nav className="app-nav" aria-label="Main Navigation">
            <NavLink
              to="/"
              className={({ isActive }) => (isActive ? 'nav-pill active' : 'nav-pill')}
              end
            >
              Home
            </NavLink>
            <NavLink
              to="/student"
              className={({ isActive }) => (isActive ? 'nav-pill active' : 'nav-pill')}
            >
              For Students
            </NavLink>
            <NavLink
              to="/recruiter"
              className={({ isActive }) => (isActive ? 'nav-pill active' : 'nav-pill')}
            >
              For Recruiters
            </NavLink>
          </nav>

          {/* Right Action Area */}
          <div className="header-actions">
            {isHome && (
              <div className="theme-toggle-wrap">
                <ThemeToggle />
              </div>
            )}

            {isAuthenticated ? (
              <div className="user-profile-pill">
                <div className="user-avatar-circle" title={userName}>
                  {userInitial}
                </div>
                <div className="user-info-text">
                  <span className="user-name" title={userName}>
                    {userName || 'User'}
                  </span>
                  <span className="user-role-badge">
                    {role === 'recruiter' ? 'Recruiter' : 'Student'}
                  </span>
                </div>
                <button
                  type="button"
                  className="nav-btn-logout"
                  onClick={handleLogout}
                  title="Sign out of CampusConnect"
                  aria-label="Logout"
                >
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  <span>Logout</span>
                </button>
              </div>
            ) : (
              <div className="auth-btn-group">
                <NavLink
                  to="/login"
                  className={({ isActive }) => (isActive ? 'nav-pill active' : 'nav-pill')}
                >
                  Sign In
                </NavLink>
                <NavLink
                  to="/register"
                  className="nav-btn-register"
                >
                  Get Started
                </NavLink>
              </div>
            )}

            {/* Mobile Hamburger Button */}
            <button
              type="button"
              className="mobile-menu-toggle"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              aria-expanded={mobileMenuOpen}
              aria-label="Toggle navigation menu"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                {mobileMenuOpen ? (
                  <>
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </>
                ) : (
                  <>
                    <line x1="3" y1="12" x2="21" y2="12" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <line x1="3" y1="18" x2="21" y2="18" />
                  </>
                )}
              </svg>
            </button>
          </div>
        </div>

        {/* Mobile Slide-down Drawer */}
        {mobileMenuOpen && (
          <div className="mobile-drawer" role="dialog" aria-modal="true" aria-label="Mobile Navigation">
            <nav className="mobile-nav-links">
              <NavLink
                to="/"
                className={({ isActive }) => (isActive ? 'mobile-nav-item active' : 'mobile-nav-item')}
                end
              >
                <span>🏠 Home</span>
              </NavLink>
              <NavLink
                to="/student"
                className={({ isActive }) => (isActive ? 'mobile-nav-item active' : 'mobile-nav-item')}
              >
                <span>🎓 For Students</span>
              </NavLink>
              <NavLink
                to="/recruiter"
                className={({ isActive }) => (isActive ? 'mobile-nav-item active' : 'mobile-nav-item')}
              >
                <span>🏢 For Recruiters</span>
              </NavLink>
              {isHome && (
                <div className="mobile-theme-row">
                  <span>Theme:</span>
                  <ThemeToggle showLabel={true} />
                </div>
              )}
              {isAuthenticated ? (
                <div className="mobile-user-section">
                  <div className="mobile-user-details">
                    <span className="mobile-user-name">{userName}</span>
                    <span className="mobile-user-role">
                      {role === 'recruiter' ? 'Recruiter Account' : 'Student Account'}
                    </span>
                  </div>
                  <button
                    type="button"
                    className="mobile-btn-logout"
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <div className="mobile-auth-actions">
                  <NavLink to="/login" className="btn btn-secondary mobile-btn-block">
                    Sign In
                  </NavLink>
                  <NavLink to="/register" className="btn btn-primary mobile-btn-block">
                    Create Free Account
                  </NavLink>
                </div>
              )}
            </nav>
          </div>
        )}
      </header>

      {/* Main Content Area */}
      <main className="app-content">
        <Outlet />
      </main>

      {/* Premium Footer */}
      <footer className="app-footer">
        <div className="footer-container">
          <div className="footer-top-grid">
            {/* Brand Column */}
            <div className="footer-col footer-col-brand">
              <Logo size="md" showTagline={true} />
              <p className="footer-brand-desc">
                The centralized campus placement ecosystem connecting universities,
                talented students, and verified corporate recruiters.
              </p>
              <div className="footer-pill-badge">
                <span className="footer-pill-dot"></span>
                <span>CampusConnect 3.0 Platform</span>
              </div>
            </div>

            {/* Students Column */}
            <div className="footer-col">
              <h4 className="footer-col-title">For Students</h4>
              <ul className="footer-links">
                <li><Link to="/student">Student Portal</Link></li>
                <li><Link to="/register?role=student">Create Student Profile</Link></li>
                <li><Link to="/student">Browse Active Drives</Link></li>
                <li><Link to="/student">Application Progression</Link></li>
              </ul>
            </div>

            {/* Recruiters Column */}
            <div className="footer-col">
              <h4 className="footer-col-title">For Recruiters</h4>
              <ul className="footer-links">
                <li><Link to="/recruiter">Recruiter Workspace</Link></li>
                <li><Link to="/register?role=recruiter">Register Company</Link></li>
                <li><Link to="/recruiter">Post Placement Drive</Link></li>
                <li><Link to="/recruiter">Screen Candidates</Link></li>
              </ul>
            </div>

            {/* Platform Column */}
            <div className="footer-col">
              <h4 className="footer-col-title">Platform</h4>
              <ul className="footer-links">
                <li><Link to="/">Home Overview</Link></li>
                <li><Link to="/login">Sign In</Link></li>
                <li><Link to="/register">Create Account</Link></li>
                <li><span className="footer-link-text">Institutional Support</span></li>
              </ul>
            </div>
          </div>

          {/* Footer Bottom Bar */}
          <div className="footer-bottom-bar">
            <p className="footer-copyright">
              &copy; {currentYear} <strong>CampusConnect</strong>. All rights reserved.
            </p>
            <p className="footer-tagline-text">
              Bridging campus talent and verified corporate careers with automated transparency.
            </p>
          </div>
        </div>
      </footer>
    </div>
  )
}
