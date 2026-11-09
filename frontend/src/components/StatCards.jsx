import { Briefcase, CheckCircle2, MessageSquare, Send, Trophy, XCircle } from 'lucide-react'

const CARDS = [
  { key: 'total', label: 'Total', icon: Briefcase },
  { key: 'Saved', label: 'Saved', icon: CheckCircle2 },
  { key: 'Applied', label: 'Applied', icon: Send },
  { key: 'Interview', label: 'Interview', icon: MessageSquare },
  { key: 'Offered', label: 'Offered', icon: Trophy },
  { key: 'Rejected', label: 'Rejected', icon: XCircle },
]

export function StatCards({ stats }) {
  return (
    <section className="stat-grid" aria-label="Application statistics">
      {CARDS.map((card) => {
        const Icon = card.icon
        return (
          <article key={card.key} className={`stat-card status-${card.key.toLowerCase()}`}>
            <div className="stat-icon">
              <Icon size={18} />
            </div>
            <div>
              <p>{card.label}</p>
              <strong>{stats[card.key] ?? 0}</strong>
            </div>
          </article>
        )
      })}
    </section>
  )
}
