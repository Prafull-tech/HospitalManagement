import { useState, useEffect, useRef } from 'react'
import { ipdApi } from '../api/ipd'
import { nursingApi } from '../api/nursing'
import { pharmacyApi } from '../api/pharmacy'
import type { MedicationAdministrationRequest, MedicationAdministrationResponse } from '../types/nursing'
import type { IPDAdmissionResponse } from '../types/ipd'
import type { NursingStaffResponse } from '../types/nursing'
import type { MedicineResponse } from '../types/pharmacy'
import styles from './NursingMARPage.module.css'

/** Map medicine form to typical administration route. */
function formToRoute(form?: string): string {
  if (!form) return ''
  switch (form.toUpperCase()) {
    case 'TABLET':
    case 'CAPSULE':
    case 'SYRUP':
      return 'Oral'
    case 'INJECTION':
      return 'Injection'
    case 'IV':
      return 'IV'
    case 'OINTMENT':
      return 'Topical'
    default:
      return ''
  }
}

export function NursingMARPage() {
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [staff, setStaff] = useState<NursingStaffResponse[]>([])
  const [admissionId, setAdmissionId] = useState<number | ''>('')
  const [marList, setMarList] = useState<MedicationAdministrationResponse[]>([])
  const [form, setForm] = useState<MedicationAdministrationRequest>({
    ipdAdmissionId: 0,
    medicationName: '',
    dosage: '',
    route: '',
    administeredById: undefined,
    doctorOrderRef: '',
    remarks: '',
  })
  const [loading, setLoading] = useState(false)
  const [loadingList, setLoadingList] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Medicine search (autocomplete) - load all once, filter client-side
  const [allMedicines, setAllMedicines] = useState<MedicineResponse[]>([])
  const [medicineResults, setMedicineResults] = useState<MedicineResponse[]>([])
  const [medicineSearching, setMedicineSearching] = useState(false)
  const [showMedicineDropdown, setShowMedicineDropdown] = useState(false)
  const medicineDropdownRef = useRef<HTMLDivElement>(null)
  const skipSearchRef = useRef(false)

  useEffect(() => {
    ipdApi
      .search({ page: 0, size: 200 })
      .then((r) =>
        setAdmissions(
          r.content.filter((a) =>
            ['ADMITTED', 'TRANSFERRED', 'DISCHARGE_INITIATED'].includes(a.admissionStatus)
          )
        )
      )
      .catch(() => setAdmissions([]))
    nursingApi.listStaff(true).then(setStaff).catch(() => setStaff([]))
  }, [])

  useEffect(() => {
    if (!admissionId) {
      setMarList([])
      return
    }
    setLoadingList(true)
    nursingApi
      .getMedicationsByAdmission(Number(admissionId))
      .then(setMarList)
      .catch(() => setMarList([]))
      .finally(() => setLoadingList(false))
  }, [admissionId])

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      ipdAdmissionId: admissionId ? Number(admissionId) : 0,
    }))
  }, [admissionId])

  // Load all medicines when admission is selected
  useEffect(() => {
    if (!admissionId) return
    setMedicineSearching(true)
    pharmacyApi
      .listMedicines()
      .then((data) => {
        const list = (data ?? []).filter((m) => m.active !== false)
        setAllMedicines(list)
      })
      .catch(() => setAllMedicines([]))
      .finally(() => setMedicineSearching(false))
  }, [admissionId])

  // Filter medicines client-side as user types
  useEffect(() => {
    if (skipSearchRef.current) {
      skipSearchRef.current = false
      return
    }
    const q = form.medicationName.trim().toLowerCase()
    if (q.length < 1) {
      setMedicineResults(allMedicines.slice(0, 100))
    } else {
      const filtered = allMedicines.filter(
        (m) =>
          (m.medicineName?.toLowerCase().includes(q) ?? false) ||
          (m.medicineCode?.toLowerCase().includes(q) ?? false)
      )
      setMedicineResults(filtered)
    }
  }, [admissionId, form.medicationName, allMedicines])

  // Close medicine dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (medicineDropdownRef.current && !medicineDropdownRef.current.contains(e.target as Node)) {
        setShowMedicineDropdown(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleSelectMedicine = (m: MedicineResponse) => {
    skipSearchRef.current = true
    const displayName = m.strength ? `${m.medicineName} (${m.strength})` : m.medicineName
    setForm((prev) => ({
      ...prev,
      medicationName: displayName,
      dosage: m.strength ?? prev.dosage ?? '',
      route: formToRoute(m.form) || (prev.route ?? ''),
    }))
    setShowMedicineDropdown(false)
    setMedicineResults([])
  }

  const handleMedicineFocus = () => setShowMedicineDropdown(true)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'administeredById' ? (value ? Number(value) : undefined) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.ipdAdmissionId || !form.medicationName?.trim()) {
      setError('Select admission and enter medication name.')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await nursingApi.recordMedication(form)
      setSuccess('Medication recorded.')
      setForm((prev) => ({
        ...prev,
        medicationName: '',
        dosage: '',
        route: '',
        doctorOrderRef: '',
        remarks: '',
      }))
      nursingApi.getMedicationsByAdmission(form.ipdAdmissionId).then(setMarList).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to record medication.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Medication Administration Record (MAR)</h2>
        {error && <div className={styles.error}>{error}</div>}
        {success && <div className={styles.success}>{success}</div>}
        <div className={styles.row}>
          <label>IPD admission</label>
          <select value={admissionId || ''} onChange={(e) => setAdmissionId(e.target.value ? Number(e.target.value) : '')} className={styles.select}>
            <option value="">Select admission</option>
            {admissions.map((a) => (
              <option key={a.id} value={a.id}>
                {a.admissionNumber} — {a.patientName}
              </option>
            ))}
          </select>
        </div>
        {admissionId && (
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.row} ref={medicineDropdownRef}>
              <label><span>Medication name <span className={styles.required}>*</span></span></label>
              <div className={styles.medicineSearchWrap}>
                <input
                  name="medicationName"
                  value={form.medicationName}
                  onChange={handleChange}
                  onFocus={handleMedicineFocus}
                  placeholder="Search medicine (e.g. Paracetamol)"
                  className={styles.input}
                  autoComplete="off"
                />
                {medicineSearching && <span className={styles.searchHint}>Searching…</span>}
                {showMedicineDropdown && (
                  <ul className={styles.medicineDropdown}>
                    {medicineSearching ? (
                      <li className={styles.medicineDropdownItem} style={{ color: 'var(--hms-text-muted)', cursor: 'default' }}>
                        Loading medicines…
                      </li>
                    ) : medicineResults.length > 0 ? (
                      medicineResults.map((m) => (
                        <li
                          key={m.id}
                          className={styles.medicineDropdownItem}
                          onClick={() => handleSelectMedicine(m)}
                          onKeyDown={(e) => e.key === 'Enter' && handleSelectMedicine(m)}
                          role="option"
                          tabIndex={0}
                        >
                          <span className={styles.medicineName}>{m.medicineName}</span>
                          {m.strength && <span className={styles.medicineMeta}> — {m.strength}</span>}
                          {m.form && <span className={styles.medicineMeta}> • {m.form}</span>}
                        </li>
                      ))
                    ) : (
                      <li className={styles.medicineDropdownItem} style={{ color: 'var(--hms-text-muted)', cursor: 'default' }}>
                        {form.medicationName.trim() ? 'No matching medicines.' : 'Type to search. Add medicines in Pharmacy if empty.'}
                      </li>
                    )}
                  </ul>
                )}
              </div>
            </div>
            <div className={styles.row}>
              <label>Dosage</label>
              <input name="dosage" value={form.dosage ?? ''} onChange={handleChange} placeholder="e.g. 500mg" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Route</label>
              <input name="route" value={form.route ?? ''} onChange={handleChange} placeholder="e.g. Oral, IV" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Administered by</label>
              <select name="administeredById" value={form.administeredById ?? ''} onChange={handleChange} className={styles.select}>
                <option value="">—</option>
                {staff.map((s) => (
                  <option key={s.id} value={s.id}>{s.fullName}</option>
                ))}
              </select>
            </div>
            <div className={styles.row}>
              <label>Doctor order ref</label>
              <input name="doctorOrderRef" value={form.doctorOrderRef ?? ''} onChange={handleChange} placeholder="Optional" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Remarks</label>
              <textarea name="remarks" value={form.remarks ?? ''} onChange={handleChange} placeholder="Optional" className={styles.textarea} rows={2} />
            </div>
            <button type="submit" className={styles.submitBtn} disabled={loading}>
              {loading ? 'Recording…' : 'Record administration'}
            </button>
          </form>
        )}
      </div>
      {admissionId && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>MAR history</h2>
          {loadingList && <div className={styles.loading}>Loading…</div>}
          {!loadingList && marList.length === 0 && <p className={styles.empty}>No medications recorded yet.</p>}
          {!loadingList && marList.length > 0 && (
            <div className={styles.tableWrap}>
              <table className={`table table-striped ${styles.table}`}>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Medication</th>
                    <th>Dosage</th>
                    <th>Route</th>
                    <th>By</th>
                  </tr>
                </thead>
                <tbody>
                  {marList.map((m) => (
                    <tr key={m.id}>
                      <td>{m.administeredAt.replace('T', ' ').slice(0, 16)}</td>
                      <td>{m.medicationName}</td>
                      <td>{m.dosage ?? '—'}</td>
                      <td>{m.route ?? '—'}</td>
                      <td>{m.administeredByName ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
