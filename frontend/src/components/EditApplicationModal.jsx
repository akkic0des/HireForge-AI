
import { useState } from 'react'
import { X } from 'lucide-react'
import { STATUSES } from '../constants'
import { todayLocalDate } from '../utils/formatDate'

export function EditApplicationModal({ application, onSave, onClose }) {
  const [form, setForm] = useState({
    company: application.company || '',
    role: application.role || '',
    status: application.status || 'Saved',
    applicationDate:
      application.applicationDate ||
      application.createdAt?.slice(0, 10) ||
      todayLocalDate(),
    jobDescription: application.jobDescription || '',
    notes: application.notes || '',
  })

  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.company.trim() || !form.role.trim()) {
      setError('Company name and role are required.')
      return
    }

    setSaving(true)
    setError('')

    try {
      const saved = await onSave(application.id, form)

      if (saved) {
        onClose()
      } else {
        setError('Could not save changes. Please try again.')
      }
    } catch {
      setError('Something went wrong while saving.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <section
        className="edit-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="edit-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-heading">
          <div>
            <h2 id="edit-modal-title">Edit application</h2>
            <p>Update the details of this job application.</p>
          </div>

          <button
            className="icon-button"
            type="button"
            onClick={onClose}
            aria-label="Close edit dialog"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="edit-form-grid">
            <label>
              Company name *
              <input
                name="company"
                value={form.company}
                onChange={handleChange}
                placeholder="e.g. Google"
                required
              />
            </label>

            <label>
              Job role *
              <input
                name="role"
                value={form.role}
                onChange={handleChange}
                placeholder="e.g. Software Engineer"
                required
              />
            </label>

            <label>
              Application status
              <select
                name="status"
                value={form.status}
                onChange={handleChange}
              >
                {STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Application date
              <input
                type="date"
                name="applicationDate"
                value={form.applicationDate}
                onChange={handleChange}
              />
            </label>

            <label className="full-width">
              Job description
              <textarea
                name="jobDescription"
                value={form.jobDescription}
                onChange={handleChange}
                placeholder="Paste the job description here..."
                rows={4}
              />
            </label>

            <label className="full-width">
              Notes
              <textarea
                name="notes"
                value={form.notes}
                onChange={handleChange}
                placeholder="Add interview dates, recruiter details, reminders..."
                rows={3}
              />
            </label>
          </div>

          {error && <p className="form-error">{error}</p>}

          <div className="modal-actions">
            <button
              className="secondary-button"
              type="button"
              onClick={onClose}
              disabled={saving}
            >
              Cancel
            </button>

            <button
              className="primary-button modal-save"
              type="submit"
              disabled={saving}
            >
              {saving ? 'Saving...' : 'Save changes'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
