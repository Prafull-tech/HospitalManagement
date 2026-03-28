import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { TestOrder, LabResult, LabReport } from '../../types/lab'

export function ReportViewerPage() {
  const { orderId } = useParams<{ orderId: string }>()
  const [order, setOrder] = useState<TestOrder | null>(null)
  const [report, setReport] = useState<LabReport | null>(null)
  const [results, setResults] = useState<LabResult[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!orderId) return
    const id = parseInt(orderId, 10)
    if (isNaN(id)) return
    let cancelled = false
    const load = async () => {
      try {
        const [o, rpt, res] = await Promise.all([
          labApi.getOrder(id),
          labApi.getReportByOrder(id).catch(() => null),
          labApi.getResultsByOrder(id),
        ])
        if (!cancelled) {
          setOrder(o)
          setReport(rpt)
          setResults(res)
        }
      } catch (err: unknown) {
        if (!cancelled) setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [orderId])

  const handlePrint = () => {
    const style = document.createElement('style')
    style.textContent = '@media print { .no-print { display: none !important; } }'
    document.head.appendChild(style)
    window.print()
    document.head.removeChild(style)
  }

  const [downloading, setDownloading] = useState(false)
  const handleDownloadPdf = async () => {
    if (!orderId) return
    setDownloading(true)
    try {
      const blob = await labApi.downloadReportPdf(parseInt(orderId, 10))
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `lab-report-${orderId}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to download PDF.')
    } finally {
      setDownloading(false)
    }
  }

  if (loading && !order) return <div className="container mt-4"><div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 200 }} /></div></div>

  if (!order) return <div className="container mt-4"><div className="alert alert-warning">Report not found.</div><Link to="/lab/view-reports" className="btn btn-outline-primary btn-sm">Back to Reports</Link></div>

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4 no-print">
        <Link to="/lab/view-reports" className="btn btn-outline-primary btn-sm">Back to Reports</Link>
        <div>
          <button type="button" className="btn btn-outline-secondary btn-sm me-2" onClick={handlePrint}>Print</button>
          <button type="button" className="btn btn-primary btn-sm" onClick={handleDownloadPdf} disabled={downloading}>{downloading ? 'Downloading…' : 'Download PDF'}</button>
        </div>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="card shadow-sm">
        <div className="card-body">
          <div className="text-center border-bottom pb-3 mb-3">
            <h4>Hospital Laboratory Report</h4>
            <p className="text-muted small mb-0">NABH / ISO 15189 Compliant</p>
          </div>
          <div className="row mb-3">
            <div className="col-md-6"><strong>Patient:</strong> {order.patientName}</div>
            <div className="col-md-6"><strong>UHID:</strong> {order.patientUhid}</div>
            <div className="col-md-6"><strong>Order #:</strong> {order.orderNumber}</div>
            <div className="col-md-6"><strong>Test:</strong> {order.testName}</div>
            <div className="col-md-6"><strong>Ordered:</strong> {new Date(order.orderedAt).toLocaleString()}</div>
            <div className="col-md-6"><strong>Doctor:</strong> {order.doctorName}</div>
          </div>
          {results.length > 0 && (
            <table className="table table-sm table-hover">
              <thead><tr><th>Parameter</th><th>Result</th><th>Unit</th><th>Reference Range</th><th>Flag</th></tr></thead>
              <tbody>
                {results.map((r) => <tr key={r.id}><td>{r.parameterName}</td><td>{r.resultValue}</td><td>{r.unit}</td><td>{r.normalRange}</td><td>{r.flag ? <span className="badge bg-warning">{r.flag}</span> : '—'}</td></tr>)}
              </tbody>
            </table>
          )}
          {report?.reportContent && <div className="mt-3"><pre className="small">{report.reportContent}</pre></div>}
          <div className="mt-4 pt-3 border-top">
            <p className="mb-1"><strong>Authorized by (Pathologist):</strong> {report?.supervisorSignature ?? report?.verifiedBy ?? '—'}</p>
            {report?.verifiedAt && <p className="text-muted small mb-0">Verified: {new Date(report.verifiedAt).toLocaleString()}</p>}
            {report?.status === 'RELEASED' && report.releasedAt && <p className="text-muted small mb-0">Report released: {new Date(report.releasedAt).toLocaleString()} by {report.releasedBy}</p>}
          </div>
        </div>
      </div>
    </div>
  )
}
