import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { departmentsApi } from '../../api/doctors'
import { enquiryApi } from '../../api/enquiry'
import { PatientSearch } from '../../components/reception/PatientSearch'
import type { DepartmentResponse } from '../../types/doctor'
import type { PatientResponse } from '../../types/patient'
import type {
  EnquiryCategory,
  EnquiryDashboard,
  EnquiryPriority,
  EnquiryResponse,
  EnquiryStatus,
} from '../../types/enquiry.types'
import styles from './EnquiryDeskPage.module.css'

const CATEGORY_OPTIONS: { value: EnquiryCategory; label: string }[] = [
  { value: 'APPOINTMENT', label: 'Appointment' },
  { value: 'DOCTOR_INFO', label: 'Doctor Info / Availability' },
  { value: 'DEPARTMENT_INFO', label: 'Department / Ward Info' },
  { value: 'BILLING_GUIDANCE', label: 'Billing Guidance' },
  { value: 'ADMISSION_HELP', label: 'Admission / IPD Help' },
  { value: 'COMPLAINT', label: 'Complaint' },
  { value: 'GENERAL_ENQUIRY', label: 'General Enquiry' },
  { value: 'OTHER', label: 'Other' },
]

const PRIORITY_OPTIONS: { value: EnquiryPriority; label: string }[] = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' },
]

const STATUS_OPTIONS: { value: EnquiryStatus | ''; label: string }[] = [
  { value: '', label: 'All Statuses' },
  { value: 'OPEN', label: 'Open' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'ESCALATED', label: 'Escalated' },
]

function badgeClass(status: EnquiryStatus) {
  if (status === 'RESOLVED') return 'bg-success'
  if (status === 'IN_PROGRESS') return 'bg-primary'
  if (status === 'ESCALATED') return 'bg-danger'
  if (status === 'CLOSED') return 'bg-secondary'
  return 'bg-warning text-dark'
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  return new Date(value).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })
}

export function EnquiryDeskPage() {
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [dashboard, setDashboard] = useState<EnquiryDashboard | null>(null)
  const [tickets, setTickets] = useState<EnquiryResponse[]>([])
  const [selectedTicket, setSelectedTicket] = useState<EnquiryResponse | null>(null)
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null)
  const [loadingDashboard, setLoadingDashboard] = useState(false)
  const [loadingTickets, setLoadingTickets] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [feedback, setFeedback] = useState<{ type: 'success' | 'danger'; text: string } | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [form, setForm] = useState({
    category: 'GENERAL_ENQUIRY' as EnquiryCategory,
    priority: 'MEDIUM' as EnquiryPriority,
    departmentId: '',
    subject: '',
    description: '',
    enquirerName: '',
    phone: '',
    email: '',
  })
  const [filters, setFilters] = useState({
    status: '' as EnquiryStatus | '',
    category: '' as EnquiryCategory | '',
    priority: '' as EnquiryPriority | '',
    departmentId: '' as number | '',
    assignedToUser: '',
    createdFrom: '',
    createdTo: '',
    patientUhid: '',
    query: '',
  })
  const [assignForm, setAssignForm] = useState({ departmentId: '', assignedToUser: '', note: '' })
  const [statusForm, setStatusForm] = useState({ status: 'IN_PROGRESS' as EnquiryStatus, resolution: '', note: '' })
  const [noteText, setNoteText] = useState('')

  const loadDashboard = async () => {
    setLoadingDashboard(true)
    return enquiryApi.getDashboard()
      .then(setDashboard)
      .catch(() => setDashboard(null))
      .finally(() => setLoadingDashboard(false))
  }

  const loadTickets = async (targetPage = page) => {
    setLoadingTickets(true)
    return enquiryApi.search({ ...filters, page: targetPage, size: 12 })
      .then((result) => {
        setTickets(result.content)
        setTotalPages(result.totalPages)
        if (selectedTicket) {
          const updated = result.content.find((ticket) => ticket.id === selectedTicket.id)
          if (updated) setSelectedTicket(updated)
        }
      })
      .catch((err) => {
        setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to load enquiries.' })
        setTickets([])
        setTotalPages(0)
      })
      .finally(() => setLoadingTickets(false))
  }

  useEffect(() => {
    departmentsApi.list().then(setDepartments).catch(() => setDepartments([]))
    loadDashboard()
  }, [])

  useEffect(() => {
    loadTickets(page)
  }, [page])

  const handleSelectTicket = async (ticketId: number) => {
    return enquiryApi.getById(ticketId)
      .then((data) => {
        setSelectedTicket(data)
        setAssignForm({
          departmentId: data.departmentId ? String(data.departmentId) : '',
          assignedToUser: data.assignedToUser ?? '',
          note: '',
        })
        setStatusForm({
          status: data.status === 'OPEN' ? 'IN_PROGRESS' : data.status,
          resolution: data.resolution ?? '',
          note: '',
        })
      })
      .catch((err) => setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to load enquiry details.' }))
  }

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setFeedback(null)
    try {
      const created = await enquiryApi.create({
        patientId: selectedPatient?.id,
        departmentId: form.departmentId ? Number(form.departmentId) : undefined,
        category: form.category,
        priority: form.priority,
        subject: form.subject,
        description: form.description,
        enquirerName: form.enquirerName || selectedPatient?.fullName || undefined,
        phone: form.phone || selectedPatient?.phone || undefined,
        email: form.email || undefined,
      })
      setFeedback({ type: 'success', text: `Enquiry ${created.enquiryNo} created successfully.` })
      setForm({
        category: 'GENERAL_ENQUIRY',
        priority: 'MEDIUM',
        departmentId: '',
        subject: '',
        description: '',
        enquirerName: '',
        phone: '',
        email: '',
      })
      setSelectedPatient(null)
      setPage(0)
      await Promise.all([loadDashboard(), loadTickets(0), handleSelectTicket(created.id)])
    } catch (err: any) {
      setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to create enquiry.' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleAssign = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedTicket) return
    setSubmitting(true)
    try {
      const updated = await enquiryApi.assign(selectedTicket.id, {
        departmentId: assignForm.departmentId ? Number(assignForm.departmentId) : undefined,
        assignedToUser: assignForm.assignedToUser || undefined,
        note: assignForm.note || undefined,
      })
      setSelectedTicket(updated)
      setAssignForm((current) => ({ ...current, note: '' }))
      setFeedback({ type: 'success', text: `Enquiry ${updated.enquiryNo} updated.` })
      loadDashboard()
      loadTickets(page)
    } catch (err: any) {
      setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to assign enquiry.' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleStatusUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedTicket) return
    setSubmitting(true)
    try {
      const updated = await enquiryApi.updateStatus(selectedTicket.id, {
        status: statusForm.status,
        resolution: statusForm.resolution || undefined,
        note: statusForm.note || undefined,
      })
      setSelectedTicket(updated)
      setStatusForm((current) => ({ ...current, note: '' }))
      setFeedback({ type: 'success', text: `Enquiry ${updated.enquiryNo} status changed to ${updated.status}.` })
      loadDashboard()
      loadTickets(page)
    } catch (err: any) {
      setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to update enquiry status.' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleAddNote = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedTicket || !noteText.trim()) return
    setSubmitting(true)
    try {
      const updated = await enquiryApi.addNote(selectedTicket.id, { note: noteText.trim() })
      setSelectedTicket(updated)
      setNoteText('')
      setFeedback({ type: 'success', text: 'Note added.' })
      loadTickets(page)
    } catch (err: any) {
      setFeedback({ type: 'danger', text: err.response?.data?.message || 'Failed to add note.' })
    } finally {
      setSubmitting(false)
    }
  }

  const categorySummary = useMemo(() => {
    if (!dashboard?.byCategory) return []
    return Object.entries(dashboard.byCategory).sort((a, b) => b[1] - a[1]).slice(0, 4)
  }, [dashboard])

  return (
    <div className="hms-page-shell">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office">Front Office</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Enquiry Desk</li>
        </ol>
      </nav>

      <div className="hms-page-hero hms-page-hero-dark">
        <div>
          <div className="hms-page-kicker">Help Desk</div>
          <h1 className="hms-page-title">Enquiry Desk</h1>
          <p className="hms-page-subtitle">Create, route, resolve, and track patient and visitor enquiries from one desk workspace.</p>
        </div>
        <div className="hms-page-actions">
          <button type="button" className="btn btn-outline-light btn-sm" onClick={() => { loadDashboard(); loadTickets(page) }}>
            Refresh Desk
          </button>
          <Link to="/reception" className="btn btn-outline-secondary btn-sm">Reception Home</Link>
        </div>
      </div>

      <div className={styles.metricStrip}>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Open</span>
          <strong className={`hms-metric-value ${styles.metricValueAccent}`}>{loadingDashboard ? '…' : dashboard?.openCount ?? 0}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">In Progress</span>
          <strong className="hms-metric-value">{loadingDashboard ? '…' : dashboard?.inProgressCount ?? 0}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Resolved</span>
          <strong className="hms-metric-value">{loadingDashboard ? '…' : dashboard?.resolvedCount ?? 0}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Escalated</span>
          <strong className="hms-metric-value">{loadingDashboard ? '…' : dashboard?.escalatedCount ?? 0}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Top Category</span>
          <strong className="hms-metric-value">{categorySummary[0]?.[0]?.replace(/_/g, ' ') ?? '—'}</strong>
        </div>
      </div>

      {feedback && <div className={`alert alert-${feedback.type} mb-0`}>{feedback.text}</div>}

      <div className={styles.workspace}>
        <div className={styles.column}>
          <div className="hms-section-card">
            <div className="hms-section-card-header">
              <div>
                <h2 className="hms-section-title">New Enquiry</h2>
                <p className="hms-section-subtitle">Link a patient when available, route it to a department, and create the ticket immediately.</p>
              </div>
            </div>
            <div className="hms-section-card-body">
              <form className={styles.formGrid} onSubmit={handleCreate}>
                <div className={styles.fullSpan}>
                  <PatientSearch
                    value={selectedPatient?.uhid}
                    displayName={selectedPatient?.fullName}
                    onSelect={setSelectedPatient}
                    label="Linked Patient"
                  />
                </div>
                <div>
                  <label className="form-label">Category</label>
                  <select className="form-select" value={form.category} onChange={(e) => setForm((current) => ({ ...current, category: e.target.value as EnquiryCategory }))}>
                    {CATEGORY_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                  </select>
                </div>
                <div>
                  <label className="form-label">Priority</label>
                  <select className="form-select" value={form.priority} onChange={(e) => setForm((current) => ({ ...current, priority: e.target.value as EnquiryPriority }))}>
                    {PRIORITY_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                  </select>
                </div>
                <div className={styles.fullSpan}>
                  <label className="form-label">Routing Department</label>
                  <select className="form-select" value={form.departmentId} onChange={(e) => setForm((current) => ({ ...current, departmentId: e.target.value }))}>
                    <option value="">Unassigned</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                </div>
                <div className={styles.fullSpan}>
                  <label className="form-label">Subject</label>
                  <input className="form-control" value={form.subject} onChange={(e) => setForm((current) => ({ ...current, subject: e.target.value }))} required />
                </div>
                <div className={styles.fullSpan}>
                  <label className="form-label">Description</label>
                  <textarea className="form-control" rows={4} value={form.description} onChange={(e) => setForm((current) => ({ ...current, description: e.target.value }))} required />
                </div>
                <div>
                  <label className="form-label">Enquirer Name</label>
                  <input className="form-control" value={form.enquirerName} onChange={(e) => setForm((current) => ({ ...current, enquirerName: e.target.value }))} placeholder={selectedPatient?.fullName ?? 'Visitor or caller name'} />
                </div>
                <div>
                  <label className="form-label">Phone</label>
                  <input className="form-control" value={form.phone} onChange={(e) => setForm((current) => ({ ...current, phone: e.target.value }))} placeholder={selectedPatient?.phone ?? 'Contact number'} />
                </div>
                <div className={styles.fullSpan}>
                  <label className="form-label">Email</label>
                  <input className="form-control" type="email" value={form.email} onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))} placeholder="Optional email" />
                </div>
                <div className={styles.fullSpan}>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Saving…' : 'Create Enquiry'}</button>
                </div>
              </form>
            </div>
          </div>

          <div className="hms-section-card">
            <div className="hms-section-card-header">
              <div>
                <h2 className="hms-section-title">Desk Summary</h2>
                <p className="hms-section-subtitle">Recent ticket volume and category distribution.</p>
              </div>
            </div>
            <div className="hms-section-card-body d-flex flex-column gap-2">
              {categorySummary.length === 0 && <div className={styles.emptyState}>No category trends yet.</div>}
              {categorySummary.map(([category, count]) => (
                <div key={category} className="d-flex justify-content-between align-items-center rounded-4 border px-3 py-2">
                  <span>{category.replace(/_/g, ' ')}</span>
                  <strong>{count}</strong>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className={styles.column}>
          <div className="hms-section-card">
            <div className="hms-section-card-header">
              <div>
                <h2 className="hms-section-title">Search and Track Tickets</h2>
                <p className="hms-section-subtitle">Filter by status, route, assignee, patient UHID, and text query.</p>
              </div>
            </div>
            <div className="hms-section-card-body d-flex flex-column gap-3">
              <div className={styles.filterGrid}>
                <div className={styles.filterField}>
                  <label className="form-label">Status</label>
                  <select className="form-select" value={filters.status} onChange={(e) => setFilters((current) => ({ ...current, status: e.target.value as EnquiryStatus | '' }))}>
                    {STATUS_OPTIONS.map((option) => <option key={option.value || 'all'} value={option.value}>{option.label}</option>)}
                  </select>
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Category</label>
                  <select className="form-select" value={filters.category} onChange={(e) => setFilters((current) => ({ ...current, category: e.target.value as EnquiryCategory | '' }))}>
                    <option value="">All Categories</option>
                    {CATEGORY_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                  </select>
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Priority</label>
                  <select className="form-select" value={filters.priority} onChange={(e) => setFilters((current) => ({ ...current, priority: e.target.value as EnquiryPriority | '' }))}>
                    <option value="">All Priorities</option>
                    {PRIORITY_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                  </select>
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Department</label>
                  <select className="form-select" value={filters.departmentId} onChange={(e) => setFilters((current) => ({ ...current, departmentId: e.target.value ? Number(e.target.value) : '' }))}>
                    <option value="">All Departments</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Assigned User</label>
                  <input className="form-control" value={filters.assignedToUser} onChange={(e) => setFilters((current) => ({ ...current, assignedToUser: e.target.value }))} />
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Patient UHID</label>
                  <input className="form-control" value={filters.patientUhid} onChange={(e) => setFilters((current) => ({ ...current, patientUhid: e.target.value }))} />
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">Search Text</label>
                  <input className="form-control" value={filters.query} onChange={(e) => setFilters((current) => ({ ...current, query: e.target.value }))} placeholder="Subject, description, name, phone" />
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">From</label>
                  <input className="form-control" type="date" value={filters.createdFrom} onChange={(e) => setFilters((current) => ({ ...current, createdFrom: e.target.value }))} />
                </div>
                <div className={styles.filterField}>
                  <label className="form-label">To</label>
                  <input className="form-control" type="date" value={filters.createdTo} onChange={(e) => setFilters((current) => ({ ...current, createdTo: e.target.value }))} />
                </div>
                <div className="d-flex gap-2">
                  <button type="button" className="btn btn-primary" onClick={() => { setPage(0); loadTickets(0) }} disabled={loadingTickets}>Apply</button>
                  <button type="button" className="btn btn-outline-secondary" onClick={() => {
                    setFilters({ status: '', category: '', priority: '', departmentId: '', assignedToUser: '', createdFrom: '', createdTo: '', patientUhid: '', query: '' })
                    setPage(0)
                    window.setTimeout(() => loadTickets(0), 0)
                  }}>Clear</button>
                </div>
              </div>

              <div className={styles.ticketList}>
                {loadingTickets && <div className={styles.emptyState}>Loading enquiries…</div>}
                {!loadingTickets && tickets.length === 0 && <div className={styles.emptyState}>No enquiries found for the current filters.</div>}
                {!loadingTickets && tickets.map((ticket) => (
                  <button
                    type="button"
                    key={ticket.id}
                    className={`${styles.ticketRow} ${selectedTicket?.id === ticket.id ? styles.ticketRowActive : ''}`}
                    onClick={() => handleSelectTicket(ticket.id)}
                  >
                    <div className={styles.ticketRowHeader}>
                      <strong>{ticket.enquiryNo}</strong>
                      <span className={`badge ${badgeClass(ticket.status)}`}>{ticket.status.replace(/_/g, ' ')}</span>
                    </div>
                    <div>{ticket.subject}</div>
                    <div className={styles.ticketMeta}>
                      <span>{ticket.patientName || ticket.enquirerName || 'Unknown enquirer'}</span>
                      <span>{ticket.departmentName || 'Unassigned department'}</span>
                      <span>{formatDateTime(ticket.createdAt)}</span>
                    </div>
                    <div className={styles.ticketBadges}>
                      <span className="badge text-bg-light">{ticket.category.replace(/_/g, ' ')}</span>
                      <span className="badge text-bg-light">{ticket.priority}</span>
                      {ticket.assignedToUser && <span className="badge text-bg-info">{ticket.assignedToUser}</span>}
                    </div>
                  </button>
                ))}
              </div>

              <div className="d-flex justify-content-end gap-2">
                <button type="button" className="btn btn-outline-secondary btn-sm" disabled={page === 0} onClick={() => setPage((current) => Math.max(current - 1, 0))}>Previous</button>
                <button type="button" className="btn btn-outline-secondary btn-sm" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
              </div>
            </div>
          </div>

          <div className="hms-section-card">
            <div className="hms-section-card-header">
              <div>
                <h2 className="hms-section-title">Ticket Detail</h2>
                <p className="hms-section-subtitle">Review the timeline, route the ticket, update status, and add operational notes.</p>
              </div>
            </div>
            <div className="hms-section-card-body d-flex flex-column gap-3">
              {!selectedTicket && <div className={styles.emptyState}>Select a ticket from the list to view or update it.</div>}
              {selectedTicket && (
                <>
                  <div className={styles.detailCard}>
                    <div className={styles.detailHeader}>
                      <div>
                        <h3 className="h5 mb-1">{selectedTicket.subject}</h3>
                        <div className="text-muted small">{selectedTicket.enquiryNo}</div>
                      </div>
                      <span className={`badge ${badgeClass(selectedTicket.status)}`}>{selectedTicket.status.replace(/_/g, ' ')}</span>
                    </div>
                    <div className={styles.detailMetaGrid}>
                      <div>
                        <span className={styles.detailLabel}>Patient</span>
                        <div className={styles.detailValue}>{selectedTicket.patientName || 'No linked patient'}</div>
                        {selectedTicket.patientUhid && <div className="text-muted small">{selectedTicket.patientUhid}</div>}
                      </div>
                      <div>
                        <span className={styles.detailLabel}>Department</span>
                        <div className={styles.detailValue}>{selectedTicket.departmentName || 'Unassigned'}</div>
                      </div>
                      <div>
                        <span className={styles.detailLabel}>Enquirer</span>
                        <div className={styles.detailValue}>{selectedTicket.enquirerName || '—'}</div>
                        <div className="text-muted small">{selectedTicket.phone || 'No phone'} {selectedTicket.email ? `· ${selectedTicket.email}` : ''}</div>
                      </div>
                      <div>
                        <span className={styles.detailLabel}>Created</span>
                        <div className={styles.detailValue}>{formatDateTime(selectedTicket.createdAt)}</div>
                        <div className="text-muted small">Resolved: {formatDateTime(selectedTicket.resolvedAt)}</div>
                      </div>
                    </div>
                    <hr />
                    <p className="mb-2">{selectedTicket.description}</p>
                    {selectedTicket.resolution && (
                      <div className="alert alert-success mb-0">
                        <strong>Resolution:</strong> {selectedTicket.resolution}
                      </div>
                    )}
                  </div>

                  <div className={styles.detailForms}>
                    <form className="border rounded-4 p-3 flex-fill" onSubmit={handleAssign}>
                      <h3 className="h6">Assignment</h3>
                      <div className="row g-2">
                        <div className="col-md-6">
                          <label className="form-label">Department</label>
                          <select className="form-select" value={assignForm.departmentId} onChange={(e) => setAssignForm((current) => ({ ...current, departmentId: e.target.value }))}>
                            <option value="">Unassigned</option>
                            {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                          </select>
                        </div>
                        <div className="col-md-6">
                          <label className="form-label">Assigned User</label>
                          <input className="form-control" value={assignForm.assignedToUser} onChange={(e) => setAssignForm((current) => ({ ...current, assignedToUser: e.target.value }))} />
                        </div>
                        <div className="col-12">
                          <label className="form-label">Assignment Note</label>
                          <textarea className="form-control" rows={2} value={assignForm.note} onChange={(e) => setAssignForm((current) => ({ ...current, note: e.target.value }))} />
                        </div>
                        <div className="col-12">
                          <button type="submit" className="btn btn-outline-primary btn-sm" disabled={submitting}>Save Assignment</button>
                        </div>
                      </div>
                    </form>

                    <form className="border rounded-4 p-3 flex-fill" onSubmit={handleStatusUpdate}>
                      <h3 className="h6">Status Update</h3>
                      <div className="row g-2">
                        <div className="col-md-4">
                          <label className="form-label">Status</label>
                          <select className="form-select" value={statusForm.status} onChange={(e) => setStatusForm((current) => ({ ...current, status: e.target.value as EnquiryStatus }))}>
                            {STATUS_OPTIONS.filter((option) => option.value).map((option) => <option key={option.value} value={option.value!}>{option.label}</option>)}
                          </select>
                        </div>
                        <div className="col-md-8">
                          <label className="form-label">Resolution</label>
                          <input className="form-control" value={statusForm.resolution} onChange={(e) => setStatusForm((current) => ({ ...current, resolution: e.target.value }))} placeholder="Required for resolved tickets" />
                        </div>
                        <div className="col-12">
                          <label className="form-label">Status Note</label>
                          <textarea className="form-control" rows={2} value={statusForm.note} onChange={(e) => setStatusForm((current) => ({ ...current, note: e.target.value }))} />
                        </div>
                        <div className="col-12">
                          <button type="submit" className="btn btn-primary btn-sm" disabled={submitting}>Update Status</button>
                        </div>
                      </div>
                    </form>
                  </div>

                  <form className="border rounded-4 p-3" onSubmit={handleAddNote}>
                    <div className="d-flex justify-content-between align-items-center gap-3 mb-2">
                      <h3 className="h6 mb-0">Add Note</h3>
                      <button type="submit" className="btn btn-outline-secondary btn-sm" disabled={submitting || !noteText.trim()}>Add</button>
                    </div>
                    <textarea className="form-control" rows={3} value={noteText} onChange={(e) => setNoteText(e.target.value)} placeholder="Capture the latest operator update, handoff note, or follow-up detail." />
                  </form>

                  <div>
                    <h3 className="h6 mb-3">Timeline</h3>
                    <div className={styles.timeline}>
                      {selectedTicket.auditLogs.length === 0 && <div className={styles.emptyState}>No timeline activity yet.</div>}
                      {selectedTicket.auditLogs.map((log) => (
                        <div key={log.id} className={styles.timelineItem}>
                          <div className={styles.timelineRow}>
                            <strong>{log.eventType.replace(/_/g, ' ')}</strong>
                            <span className="text-muted small">{formatDateTime(log.eventAt)}</span>
                          </div>
                          <div className="text-muted small">{log.performedBy || 'System'}</div>
                          {log.note && <p className={styles.timelineNote}>{log.note}</p>}
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
