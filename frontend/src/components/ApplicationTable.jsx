
import { useState } from 'react'
import { Trash2, Pencil } from 'lucide-react'
import { STATUSES } from '../constants'
import { formatDate } from '../utils/formatDate'
import { EditApplicationModal } from './EditApplicationModal'

export function ApplicationTable({
  applications,
  statusFilter,
  onStatusFilterChange,
  onStatusChange,
  onDelete,
  onEdit,
  loading = false,
  hasAnyApplications = false,
}) {
  const [editingApplication, setEditingApplication] = useState(null)

  return (
    <>
      <section className="panel table-panel">
        <div className="panel-heading table-heading">
          <div>
            <h2>Application tracker</h2>
            <p>Update status, edit details, or remove closed roles.</p>
          </div>

          <label className="filter-field">
            Status
            <select
              value={statusFilter}
              onChange={(event) => onStatusFilterChange(event.target.value)}
            >
              <option value="All">All statuses</option>
              {STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Company</th>
                <th>Role</th>
                <th>Status</th>
                <th>Application date</th>
                <th className="actions-col">Actions</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="empty-row" role="status">Loading your applications…</td></tr>
              ) : applications.length === 0 ? (
                <tr>
                  <td colSpan={5} className="empty-row">
                    {hasAnyApplications ? 'No applications match the current filters.' : 'No applications yet. Add your first role from the dashboard.'}
                  </td>
                </tr>
              ) : (
                applications.map((application) => (
                  <tr key={application.id}>
                    <td>
                      <strong>{application.company}</strong>
                    </td>

                    <td>{application.role}</td>

                    <td>
                      <select
                        className={`status-select status-${application.status.toLowerCase()}`}
                        value={application.status}
                        onChange={(event) =>
                          onStatusChange(application.id, event.target.value)
                        }
                        aria-label={`Status for ${application.company}`}
                      >
                        {STATUSES.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </td>

                    <td>{formatDate(application.applicationDate)}</td>

                    <td className="actions-col">
                      <div className="table-actions">
                        <button
                          className="icon-button"
                          type="button"
                          onClick={() => setEditingApplication(application)}
                          aria-label={`Edit ${application.company} application`}
                          title="Edit application"
                        >
                          <Pencil size={16} />
                        </button>

                        <button
                          className="icon-button danger"
                          type="button"
                          onClick={() => {
                            if (window.confirm(`Delete the ${application.role} application at ${application.company}?`)) {
                              onDelete(application.id)
                            }
                          }}
                          aria-label={`Delete ${application.company} application`}
                          title="Delete application"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      {editingApplication && (
        <EditApplicationModal
          application={editingApplication}
          onSave={onEdit}
          onClose={() => setEditingApplication(null)}
        />
      )}
    </>
  )
}
