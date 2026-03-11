import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { TestMaster, TestMasterRequest, TestCategory, SampleType } from '../../types/lab'

const CATEGORIES: TestCategory[] = ['HEMATOLOGY', 'BIOCHEMISTRY', 'SEROLOGY_IMMUNOLOGY', 'MICROBIOLOGY', 'HISTOPATHOLOGY_CYTOLOGY', 'EMERGENCY_ICU_PANEL']
const SAMPLE_TYPES: SampleType[] = ['BLOOD', 'URINE', 'STOOL', 'SPUTUM', 'CSF', 'PLEURAL_FLUID', 'PERITONEAL_FLUID', 'SWAB', 'TISSUE', 'OTHER']

export function LabTestMasterPage() {
  const [tests, setTests] = useState<TestMaster[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<Partial<TestMasterRequest>>({
    testCode: '', testName: '', category: 'BIOCHEMISTRY', sampleType: 'BLOOD', normalTATMinutes: 60, price: 0, unit: '', normalRange: '',
  })

  const load = useCallback(async () => {
    try {
      setTests(await labApi.listTestMasters())
    } catch {
      setTests([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.testCode?.trim() || !form.testName?.trim()) { alert('Code and name required'); return }
    try {
      await labApi.createTestMaster({
        testCode: form.testCode.trim(),
        testName: form.testName.trim(),
        category: form.category ?? 'BIOCHEMISTRY',
        sampleType: form.sampleType ?? 'BLOOD',
        normalTATMinutes: form.normalTATMinutes ?? 60,
        price: form.price ?? 0,
        normalRange: form.normalRange?.trim() || undefined,
        unit: form.unit?.trim() || undefined,
      })
      setShowForm(false)
      setForm({ testCode: '', testName: '', category: 'BIOCHEMISTRY', sampleType: 'BLOOD', normalTATMinutes: 60, price: 0, unit: '', normalRange: '' })
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to create.')
    }
  }

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="h5 mb-1 fw-bold">Test Master</h2><p className="text-muted small mb-0">Manage laboratory tests.</p></div>
        <div>
          <Link to="/lab" className="btn btn-outline-primary btn-sm me-2">Back to Lab</Link>
          <button type="button" className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>{showForm ? 'Cancel' : 'Add Test'}</button>
        </div>
      </div>

      {showForm && (
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <h5 className="card-title">Add new test</h5>
            <form onSubmit={handleSubmit} className="row g-2">
              <div className="col-md-2"><input className="form-control form-control-sm" placeholder="Test code *" value={form.testCode ?? ''} onChange={(e) => setForm((f) => ({ ...f, testCode: e.target.value }))} required /></div>
              <div className="col-md-3"><input className="form-control form-control-sm" placeholder="Test name *" value={form.testName ?? ''} onChange={(e) => setForm((f) => ({ ...f, testName: e.target.value }))} required /></div>
              <div className="col-md-2"><select className="form-select form-select-sm" value={form.category ?? 'BIOCHEMISTRY'} onChange={(e) => setForm((f) => ({ ...f, category: e.target.value as TestCategory }))}>{CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}</select></div>
              <div className="col-md-2"><select className="form-select form-select-sm" value={form.sampleType ?? 'BLOOD'} onChange={(e) => setForm((f) => ({ ...f, sampleType: e.target.value as SampleType }))}>{SAMPLE_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}</select></div>
              <div className="col-md-1"><input type="number" className="form-control form-control-sm" placeholder="TAT min" value={form.normalTATMinutes ?? 60} onChange={(e) => setForm((f) => ({ ...f, normalTATMinutes: parseInt(e.target.value, 10) || 60 }))} /></div>
              <div className="col-md-1"><input type="number" step="0.01" className="form-control form-control-sm" placeholder="Price" value={form.price ?? 0} onChange={(e) => setForm((f) => ({ ...f, price: parseFloat(e.target.value) || 0 }))} /></div>
              <div className="col-md-1"><input className="form-control form-control-sm" placeholder="Unit" value={form.unit ?? ''} onChange={(e) => setForm((f) => ({ ...f, unit: e.target.value }))} /></div>
              <div className="col-md-2"><input className="form-control form-control-sm" placeholder="Reference range" value={form.normalRange ?? ''} onChange={(e) => setForm((f) => ({ ...f, normalRange: e.target.value }))} /></div>
              <div className="col-md-2"><button type="submit" className="btn btn-success btn-sm">Create</button></div>
            </form>
          </div>
        </div>
      )}

      {loading ? <div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 100 }} /></div> : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Code</th><th>Name</th><th>Category</th><th>Specimen</th><th>Unit</th><th>Reference Range</th><th>TAT (min)</th><th>Price</th><th>Active</th></tr></thead>
            <tbody>
              {tests.map((t) => (
                <tr key={t.id}>
                  <td>{t.testCode}</td><td>{t.testName}</td><td>{t.category}</td><td>{t.sampleType}</td><td>{t.unit ?? '—'}</td><td>{t.normalRange ?? '—'}</td><td>{t.normalTATMinutes}</td><td>{t.price}</td><td>{t.active ? <span className="badge bg-success">Yes</span> : <span className="badge bg-secondary">No</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
