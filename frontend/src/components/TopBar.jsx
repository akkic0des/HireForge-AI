import { Menu, Search } from 'lucide-react'

export function TopBar({ title, subtitle, search, onSearchChange, onMenuClick }) {
  return (
    <header className="topbar">
      <div className="topbar-copy">
        <button className="icon-button menu-button" type="button" onClick={onMenuClick} aria-label="Open navigation">
          <Menu size={18} />
        </button>
        <div>
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </div>
      </div>

      {onSearchChange ? (
        <label className="search-field">
          <Search size={16} />
          <input
            type="search"
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Search company or role"
          />
        </label>
      ) : null}
    </header>
  )
}
