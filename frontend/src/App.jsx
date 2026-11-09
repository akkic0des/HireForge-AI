
import { useEffect, useMemo, useState } from 'react'
import { AuthScreen } from './components/AuthScreen'
import { ResumeReview, JobMatcher, InterviewPractice } from './components/CareerTools'
import { authStore } from './lib/api'
import { ApplicationForm } from './components/ApplicationForm'
import { ApplicationTable } from './components/ApplicationTable'
import { Sidebar } from './components/Sidebar'
import { StatCards } from './components/StatCards'
import { TopBar } from './components/TopBar'
import { useApplications } from './hooks/useApplications'
import { DashboardAnalytics } from './components/DashboardAnalytics'

function matchesSearch(application, search) {
  const query = search.trim().toLowerCase()

  if (!query) return true

  return (
    application.company.toLowerCase().includes(query) ||
    application.role.toLowerCase().includes(query)
  )
}

function App() {
  const [user, setUser] = useState(() => authStore.user())
  const [currentView, setCurrentView] = useState('dashboard')

  useEffect(() => {
    function handleUnauthorized() {
      authStore.clear()
      setUser(null)
      setCurrentView('dashboard')
    }

    window.addEventListener('hireforge:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('hireforge:unauthorized', handleUnauthorized)
  }, [])
  const {
    applications,
    stats,
    loading,
    error,
    addApplication,
    updateApplication,
    updateStatus,
    deleteApplication,
  } = useApplications(Boolean(user && authStore.token()))

  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('All')

  const filteredApplications = useMemo(() => {
    return applications.filter((application) => {
      const matchesStatus =
        statusFilter === 'All' || application.status === statusFilter

      return matchesStatus && matchesSearch(application, search)
    })
  }, [applications, search, statusFilter])

  if (!user || !authStore.token()) return <AuthScreen onAuthenticated={setUser} />

  function handleNavigate(view) {
    setCurrentView(view)
    setSidebarOpen(false)
  }

  const titles = { dashboard: 'Dashboard', applications: 'Applications', resume: 'Resume review', match: 'Job match', interview: 'Interview practice' }
  const title = titles[currentView] || 'Dashboard'

  const subtitle =
    currentView === 'dashboard'
      ? 'Track your pipeline and keep every application moving.'
      : currentView === 'applications' ? 'Search, filter, and manage every role you are pursuing.' : 'Build a focused plan for your next career move.'

  return (
    <div className="app-shell">
      <Sidebar
        currentView={currentView}
        onNavigate={handleNavigate}
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        user={user}
        onSignOut={() => { authStore.clear(); setUser(null) }}
      />

      <main className="app-main">
        <TopBar
          title={title}
          subtitle={subtitle}
          search={search}
          onSearchChange={['dashboard', 'applications'].includes(currentView) ? setSearch : undefined}
          onMenuClick={() => setSidebarOpen(true)}
        />

        {loading && currentView === 'dashboard' && <DashboardSkeleton />}
        {loading && currentView === 'applications' && <p className="inline-loading" role="status">Loading your applications�</p>}

        {error && (
          <p className="app-error-banner" role="alert">
            {error}
          </p>
        )}

        {currentView === 'dashboard' && !loading && <StatCards stats={stats} />}
        {currentView === 'dashboard' && !loading && <DashboardAnalytics stats={stats} />}

        {currentView === 'resume' && <ResumeReview />}
        {currentView === 'match' && <JobMatcher />}
        {currentView === 'interview' && <InterviewPractice />}

        <div
          className={
            currentView === 'dashboard' ? 'content-grid' : 'content-stack'
          }
        >
          {currentView === 'dashboard' && (
            <ApplicationForm onAdd={addApplication} />
          )}

          {(currentView === 'dashboard' || currentView === 'applications') && <ApplicationTable
            applications={filteredApplications}
            statusFilter={statusFilter}
            onStatusFilterChange={setStatusFilter}
            onStatusChange={updateStatus}
            onDelete={deleteApplication}
            onEdit={updateApplication}
            loading={loading}
            hasAnyApplications={applications.length > 0}
          />}
        </div>
      </main>
    </div>
  )
}

function DashboardSkeleton() {
  return <div className="dashboard-skeleton" role="status" aria-label="Loading dashboard">
    <span className="skeleton-line skeleton-line-short" />
    <div className="skeleton-stat-row">{Array.from({ length: 6 }, (_, index) => <span className="skeleton-stat" key={index} />)}</div>
    <div className="skeleton-panel" />
  </div>
}

export default App
