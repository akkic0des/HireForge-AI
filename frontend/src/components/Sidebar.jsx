import { Briefcase, LayoutDashboard, Sparkles, FileText, MessageCircle, LogOut, Target } from 'lucide-react'

const NAV_ITEMS = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'applications', label: 'Applications', icon: Briefcase },
  { id: 'resume', label: 'Resume review', icon: FileText },
  { id: 'match', label: 'Job match', icon: Target },
  { id: 'interview', label: 'Interview practice', icon: MessageCircle },
]

export function Sidebar({ currentView, onNavigate, open, onClose, user, onSignOut }) {
  return (
    <>
      {open ? <button className="sidebar-backdrop" type="button" aria-label="Close menu" onClick={onClose} /> : null}

      <aside className={`sidebar ${open ? 'is-open' : ''}`}>
        <div className="brand">
          <span className="brand-mark">HF</span>
          <div>
            <p className="brand-name">HireForge AI</p>
            <p className="brand-tag">Application tracker</p>
          </div>
        </div>

        <nav className="sidebar-nav" aria-label="Primary">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon
            const isActive = currentView === item.id

            return (
              <button
                key={item.id}
                type="button"
                className={`nav-item ${isActive ? 'is-active' : ''}`}
                onClick={() => onNavigate(item.id)}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            )
          })}
        </nav>

        <div className="sidebar-note">
          <Sparkles size={16} />
          <div>
            <p>Interview prep</p>
            <span>Personalized tools for your next move.</span>
          </div>
        </div>
        <div className="sidebar-user"><span className="avatar">{user?.name?.slice(0,1)?.toUpperCase() || 'H'}</span><div className="sidebar-user-copy"><strong>{user?.name}</strong><small>{user?.email}</small></div><button type="button" className="icon-button" onClick={onSignOut} title="Sign out" aria-label="Sign out"><LogOut size={17}/></button></div>
      </aside>
    </>
  )
}
