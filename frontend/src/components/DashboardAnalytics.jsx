
import { BarChart3, BriefcaseBusiness, CalendarCheck, Trophy } from 'lucide-react'

const PIPELINE_STATUSES = [
  { name: 'Saved', color: '#a8bb87' },
  { name: 'Applied', color: '#82a9a0' },
  { name: 'Interview', color: '#d4b47d' },
  { name: 'Offered', color: '#93bd9b' },
  { name: 'Rejected', color: '#ca8982' },
]

export function DashboardAnalytics({ stats }) {
  const total = stats.total || 0

  const activeApplications =
    (stats.Saved || 0) + (stats.Applied || 0) + (stats.Interview || 0)

  const responseCount =
    (stats.Interview || 0) +
    (stats.Offered || 0) +
    (stats.Rejected || 0)

  const responseRate = total
    ? Math.round((responseCount / total) * 100)
    : 0

  const interviewRate = total
    ? Math.round(((stats.Interview || 0) / total) * 100)
    : 0

  const offerRate = total
    ? Math.round(((stats.Offered || 0) / total) * 100)
    : 0

  const metrics = [
    {
      label: 'Active applications',
      value: activeApplications,
      detail: 'Saved, applied and interview stages',
      icon: BriefcaseBusiness,
    },
    {
      label: 'Response rate',
      value: `${responseRate}%`,
      detail: `${responseCount} applications with an outcome`,
      icon: BarChart3,
    },
    {
      label: 'Interview rate',
      value: `${interviewRate}%`,
      detail: `${stats.Interview || 0} interviews`,
      icon: CalendarCheck,
    },
    {
      label: 'Offer rate',
      value: `${offerRate}%`,
      detail: `${stats.Offered || 0} offers received`,
      icon: Trophy,
    },
  ]

  return (
    <section className="analytics-section">
      <div className="analytics-heading">
        <div>
          <h2>Performance overview</h2>
          <p>Understand how your job search is progressing.</p>
        </div>
        <span className="analytics-period">All time</span>
      </div>

      <div className="analytics-metrics">
        {metrics.map((metric) => {
          const Icon = metric.icon

          return (
            <article className="analytics-card" key={metric.label}>
              <div className="analytics-card-top">
                <span>{metric.label}</span>
                <Icon size={18} />
              </div>
              <strong>{metric.value}</strong>
              <p>{metric.detail}</p>
            </article>
          )
        })}
      </div>

      <div className="panel pipeline-panel">
        <div className="pipeline-heading">
          <div>
            <h2>Application pipeline</h2>
            <p>Distribution of your applications by current status.</p>
          </div>
          <strong>{total} total</strong>
        </div>

        <div className="pipeline-bars">
          {PIPELINE_STATUSES.map((status) => {
            const count = stats[status.name] || 0
            const percentage = total
              ? Math.round((count / total) * 100)
              : 0

            return (
              <div className="pipeline-row" key={status.name}>
                <div className="pipeline-label">
                  <span>
                    <i style={{ background: status.color }} />
                    {status.name}
                  </span>
                  <strong>{count} <small>({percentage}%)</small></strong>
                </div>

                <div
                  className="pipeline-track"
                  role="progressbar"
                  aria-label={`${status.name} applications`}
                  aria-valuenow={percentage}
                  aria-valuemin={0}
                  aria-valuemax={100}
                >
                  <div
                    className="pipeline-fill"
                    style={{
                      width: `${percentage}%`,
                      background: status.color,
                    }}
                  />
                </div>
              </div>
            )
          })}
        </div>

        {total === 0 && (
          <p className="analytics-empty">
            Add your first application to see your analytics here.
          </p>
        )}
      </div>
    </section>
  )
}

