
import { useCallback, useEffect, useMemo, useState } from 'react'
import { apiRequest } from '../lib/api'
import { todayLocalDate } from '../utils/formatDate'

function mapApplication(application) {
  return application
}

function mapRequest(application) {
  return {
    company: application.company.trim(),
    role: application.role.trim(),
    status: application.status,
    applicationDate:
      application.applicationDate ||
      todayLocalDate(),
    jobDescription: application.jobDescription || null,
    notes: application.notes || null,
  }
}

const API_URL = '/applications'

export function useApplications(enabled = true) {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(enabled)
  const [error, setError] = useState('')

  useEffect(() => {
    // The unauthenticated App view does not render this hook's loading UI.
    // Initializing from `enabled` avoids a synchronous state update in the effect.
    if (!enabled) return undefined
    let cancelled = false

    async function loadApplications() {
      try {
        setLoading(true)
        const data = await apiRequest(API_URL)

        if (!cancelled) {
          setApplications(data.map(mapApplication))
          setError('')
        }
      } catch (err) {
        if (!cancelled) {
          setError(`Could not load applications: ${err.message}`)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    loadApplications()

    return () => {
      cancelled = true
    }
  }, [enabled])

  const addApplication = useCallback(async (payload) => {
    try {
      const created = await apiRequest(API_URL, {
        method: 'POST',
        body: JSON.stringify(mapRequest(payload)),
      })

      setApplications((current) => [
        mapApplication(created),
        ...current,
      ])

      setError('')
      return true
    } catch (err) {
      setError(`Could not add application: ${err.message}`)
      return false
    }
  }, [])

  // Edit all application details.
  const updateApplication = useCallback(async (id, payload) => {
    try {
      const updated = await apiRequest(`${API_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(mapRequest(payload)),
      })

      setApplications((current) =>
        current.map((application) =>
          application.id === id
            ? mapApplication(updated)
            : application
        )
      )

      setError('')
      return true
    } catch (err) {
      setError(`Could not edit application: ${err.message}`)
      return false
    }
  }, [])

  // Update status from the table dropdown.
  const updateStatus = useCallback(async (id, status) => {
    try {
      const existing = applications.find(
        (application) => application.id === id
      )

      if (!existing) throw new Error('Application not found')

      const updated = await apiRequest(`${API_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(mapRequest({ ...existing, status })),
      })

      setApplications((current) =>
        current.map((application) =>
          application.id === id
            ? mapApplication(updated)
            : application
        )
      )

      setError('')
      return true
    } catch (err) {
      setError(`Could not update application: ${err.message}`)
      return false
    }
  }, [applications])

  const deleteApplication = useCallback(async (id) => {
    try {
      await apiRequest(`${API_URL}/${id}`, {
        method: 'DELETE',
      })

      setApplications((current) =>
        current.filter((application) => application.id !== id)
      )

      setError('')
      return true
    } catch (err) {
      setError(`Could not delete application: ${err.message}`)
      return false
    }
  }, [])

  const stats = useMemo(() => {
    return applications.reduce(
      (acc, application) => {
        acc.total += 1
        acc[application.status] =
          (acc[application.status] || 0) + 1
        return acc
      },
      {
        total: 0,
        Saved: 0,
        Applied: 0,
        Interview: 0,
        Offered: 0,
        Rejected: 0,
      }
    )
  }, [applications])

  return {
    applications,
    stats,
    loading,
    error,
    addApplication,
    updateApplication,
    updateStatus,
    deleteApplication,
  }
}
