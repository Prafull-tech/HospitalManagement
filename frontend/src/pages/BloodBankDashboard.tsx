export function BloodBankDashboard() {
  return (
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">Blood Bank</h2>
        <p className="text-muted small mb-0">Donor management, inventory, and blood issue.</p>
      </div>
      <div className="card shadow-sm">
        <div className="card-body">
          <p className="text-muted mb-0 small">Blood Bank module will be implemented here.</p>
          <p className="text-muted mb-0 small mt-1">Submenus: Donor Management, Blood Inventory, Issue Blood Units, Request Management, Blood Expiry Alerts.</p>
        </div>
      </div>
    </div>
  )
}
