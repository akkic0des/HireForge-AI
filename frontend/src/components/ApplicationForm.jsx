import { useState } from 'react'
import { Plus } from 'lucide-react'
import { STATUSES } from '../constants'
import { todayLocalDate } from '../utils/formatDate'

const INITIAL_FORM = {
  company: '',
  role: '',
  status: 'Saved',
  applicationDate: todayLocalDate(),
  jobDescription: '',
  notes: '',
}

export function ApplicationForm({ onAdd }) {
  const [form, setForm] = useState(INITIAL_FORM)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

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
    const saved = await onAdd(form)

    if (saved) {
      setForm({ ...INITIAL_FORM, applicationDate: todayLocalDate() })
    }
    setSaving(false)
  }


  return (
    <form className="panel form-panel" onSubmit={handleSubmit}>
      <div className="panel-heading">
        <h2>Add application</h2>
        <p>Track a new company and role in your pipeline.</p>
      </div>

      <div className="form-grid">
        <label>
          Company name
          <input
            name="company"
            maxLength={255}
            value={form.company}
            onChange={handleChange}
            placeholder="Acme Inc."
            autoComplete="organization"
          />
        </label>

        <label>
          Application date
          <input type="date" name="applicationDate" value={form.applicationDate} onChange={handleChange} />
        </label>

        <label>
          Job description
          <textarea name="jobDescription" value={form.jobDescription} onChange={handleChange} placeholder="Add requirements to tailor interview practice…" rows={3} />
        </label>

        <label>
          Notes
          <textarea name="notes" value={form.notes} onChange={handleChange} placeholder="Recruiter details, reminders, or next steps…" rows={3} />
        </label>

        <label>
          Role
          <input
            name="role"
            maxLength={255}
            value={form.role}
            onChange={handleChange}
            placeholder="Software Engineer"
          />
        </label>

        <label>
          Status
          <select name="status" value={form.status} onChange={handleChange}>
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <button className="primary-button" type="submit" disabled={saving}>
        <Plus size={16} />
        {saving ? 'Saving…' : 'Add application'}
      </button>
    </form>
  )
}
