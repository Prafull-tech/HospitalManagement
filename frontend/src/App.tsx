import { useEffect } from 'react'
import { Routes, Route, Navigate, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import { useAppBootstrap } from './components/AppBootstrap'
import { setAuthRedirect } from './api/authRedirect'
import { PermissionsProvider } from './contexts/PermissionsContext'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { RoleProtectedRoute } from './components/RoleProtectedRoute'
import { DashboardRedirect } from './components/DashboardRedirect'
import { ReceptionDashboard } from './pages/ReceptionDashboard'
import { PatientRegisterPage } from './pages/PatientRegisterPage'
import { PatientSearchPage } from './pages/PatientSearchPage'
import { DoctorListPage } from './pages/DoctorListPage'
import { DoctorFormPage } from './pages/DoctorFormPage'
import { DoctorAvailabilityPage } from './pages/DoctorAvailabilityPage'
import { OPDDashboard } from './pages/OPDDashboard'
import { OPDRegisterVisitPage } from './pages/OPDRegisterVisitPage'
import { OPDQueuePage } from './pages/OPDQueuePage'
import { OPDSearchVisitsPage } from './pages/OPDSearchVisitsPage'
import { OPDVisitDetailPage } from './pages/OPDVisitDetailPage'
import { IPDDashboard } from './pages/IPDDashboard'
import { IPDAdmitPage } from './pages/IPDAdmitPage'
import { IPDAdmissionManagementPage } from './pages/IPDAdmissionManagementPage'
import { BedsAvailability } from './pages/ipd/BedsAvailability'
import HospitalBedAvailability from './pages/ipd/HospitalBedAvailability'
import { IPDAdmissionsListPage } from './pages/IPDAdmissionsListPage'
import { ViewAdmission } from './pages/ipd/ViewAdmission'
import { EditAdmissionPage } from './pages/ipd/EditAdmissionPage'
import { DischargePage } from './pages/ipd/DischargePage'
import { BillingAccountPage } from './pages/billing/BillingAccountPage'
import { BillingDashboard } from './pages/billing/BillingDashboard'
import { IpdBillingPage } from './pages/billing/IpdBillingPage'
import { CorporateAccountsPage } from './pages/billing/CorporateAccountsPage'
import { EMIPlansPage } from './pages/billing/EMIPlansPage'
import { InsurancePage } from './pages/billing/InsurancePage'
import { OnlinePaymentPage } from './pages/billing/OnlinePaymentPage'
import { OpdGroupBillingPage } from './pages/billing/OpdGroupBillingPage'
import { PaymentsPage } from './pages/billing/PaymentsPage'
import { RefundsPage } from './pages/billing/RefundsPage'
import { NursingDashboard } from './pages/NursingDashboard'
import { NursingStaffPage } from './pages/NursingStaffPage'
import { NursingAssignPage } from './pages/NursingAssignPage'
import { NursingVitalsPage } from './pages/NursingVitalsPage'
import { NursingMARPage } from './pages/NursingMARPage'
import { NursingNotesPage } from './pages/NursingNotesPage'
import { SearchNursingNotes } from './pages/nursing/SearchNursingNotes'
import { NursingNotesPrintPage } from './pages/NursingNotesPrintPage'
import { WardPage } from './pages/wards/WardPage'
import { SystemConfigRolesPage } from './pages/system-config/SystemConfigRolesPage'
import { SystemConfigModulesPage } from './pages/system-config/SystemConfigModulesPage'
import { SystemConfigPermissionsPage } from './pages/system-config/SystemConfigPermissionsPage'
import { SystemConfigFeaturesPage } from './pages/system-config/SystemConfigFeaturesPage'
import { PharmacyDashboard } from './pages/PharmacyDashboard'
import { LabDashboard } from './pages/LabDashboard'
import { LabReportsPage } from './pages/LabReportsPage'
import { RadiologyDashboard } from './pages/RadiologyDashboard'
import { BloodBankDashboard } from './pages/BloodBankDashboard'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { UnauthorizedPage } from './pages/UnauthorizedPage'

const AUTH_BYPASS = false // Set true to isolate: render "App Loaded" and skip auth

export default function App() {
  console.log('App render')
  const { user } = useAuth()
  const roleCodes = user?.roles?.length ? user.roles : []

  const navigate = useNavigate()
  const bootstrap = useAppBootstrap()

  useEffect(() => {
    setAuthRedirect(() => navigate('/login', { replace: true }))
    return () => setAuthRedirect(null)
  }, [navigate])

  useEffect(() => {
    bootstrap?.setReady()
  }, [bootstrap])

  if (AUTH_BYPASS) {
    return <div>App Loaded</div>
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <PermissionsProvider roleCodes={roleCodes}>
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          </PermissionsProvider>
        }
      >
        <Route index element={<DashboardRedirect />} />
        <Route path="reception" element={<Outlet />}>
          <Route index element={<ReceptionDashboard />} />
          <Route path="register" element={<PatientRegisterPage />} />
          <Route path="search" element={<PatientSearchPage />} />
        </Route>
        <Route path="pharmacy" element={<RoleProtectedRoute><PharmacyDashboard /></RoleProtectedRoute>} />
        <Route path="lab" element={<RoleProtectedRoute><Outlet /></RoleProtectedRoute>}>
          <Route index element={<LabDashboard />} />
          <Route path="reports" element={<LabReportsPage />} />
        </Route>
        <Route path="radiology" element={<RoleProtectedRoute><Outlet /></RoleProtectedRoute>}>
          <Route index element={<RadiologyDashboard />} />
          <Route path="reports" element={<RadiologyDashboard />} />
          <Route path="requests" element={<RadiologyDashboard />} />
          <Route path="view" element={<RadiologyDashboard />} />
        </Route>
        <Route path="bloodbank" element={<RoleProtectedRoute><Outlet /></RoleProtectedRoute>}>
          <Route index element={<BloodBankDashboard />} />
          <Route path="donors" element={<BloodBankDashboard />} />
          <Route path="inventory" element={<BloodBankDashboard />} />
          <Route path="issue" element={<BloodBankDashboard />} />
          <Route path="requests" element={<BloodBankDashboard />} />
          <Route path="alerts" element={<BloodBankDashboard />} />
        </Route>
        <Route path="doctors" element={<Outlet />}>
          <Route index element={<DoctorListPage />} />
          <Route path="new" element={<DoctorFormPage />} />
          <Route path=":id/edit" element={<DoctorFormPage />} />
          <Route path=":id/availability" element={<DoctorAvailabilityPage />} />
        </Route>
          <Route path="opd" element={<Outlet />}>
          <Route index element={<OPDDashboard />} />
          <Route path="register" element={<OPDRegisterVisitPage />} />
          <Route path="queue" element={<OPDQueuePage />} />
          <Route path="visits" element={<OPDSearchVisitsPage />} />
          <Route path="visits/:id" element={<OPDVisitDetailPage />} />
        </Route>
        {/* Explicit discharge route so /ipd/discharge/:id is matched reliably */}
        <Route path="ipd/discharge/:id" element={<DischargePage />} />
        <Route path="ipd" element={<Outlet />}>
          <Route index element={<IPDDashboard />} />
          <Route path="admission-management" element={<IPDAdmissionManagementPage />} />
          <Route path="admit" element={<IPDAdmitPage />} />
          <Route path="beds" element={<BedsAvailability />} />
          <Route path="hospital-beds" element={<HospitalBedAvailability />} />
          <Route path="admissions" element={<IPDAdmissionsListPage />} />
          <Route path="admissions/:id" element={<ViewAdmission />} />
          <Route path="admissions/:id/edit" element={<EditAdmissionPage />} />
        </Route>
        <Route path="billing" element={<ProtectedRoute allowedRoles={['BILLING', 'ADMIN']}><Outlet /></ProtectedRoute>}>
          <Route index element={<BillingDashboard />} />
          <Route path="ipd" element={<IpdBillingPage />} />
          <Route path="account/:id" element={<BillingAccountPage />} />
          <Route path="corporate" element={<CorporateAccountsPage />} />
          <Route path="emi" element={<EMIPlansPage />} />
          <Route path="payment/online" element={<OnlinePaymentPage />} />
          <Route path="opd/group" element={<OpdGroupBillingPage />} />
          <Route path="tpa" element={<InsurancePage />} />
          <Route path="payments" element={<PaymentsPage />} />
          <Route path="refunds" element={<RefundsPage />} />
        </Route>
        <Route path="insurance" element={<Navigate to="/billing/tpa" replace />} />
        <Route path="payments" element={<Navigate to="/billing/payments" replace />} />
        <Route path="refunds" element={<Navigate to="/billing/refunds" replace />} />
        <Route path="nursing" element={<Outlet />}>
          <Route index element={<NursingDashboard />} />
          <Route path="staff" element={<NursingStaffPage />} />
          <Route path="assign" element={<NursingAssignPage />} />
          <Route path="vitals" element={<NursingVitalsPage />} />
          <Route path="medications" element={<NursingMARPage />} />
          <Route path="notes" element={<NursingNotesPage />} />
          <Route path="notes/search" element={<SearchNursingNotes />} />
          <Route path="notes/print" element={<NursingNotesPrintPage />} />
        </Route>
        <Route path="wards" element={<Outlet />}>
          <Route index element={<Navigate to="/wards/general" replace />} />
          <Route path=":wardSlug" element={<WardPage />} />
        </Route>
        <Route path="admin/config" element={<Outlet />}>
          <Route index element={<Navigate to="/admin/config/roles" replace />} />
          <Route path="roles" element={<SystemConfigRolesPage />} />
          <Route path="modules" element={<SystemConfigModulesPage />} />
          <Route path="permissions" element={<SystemConfigPermissionsPage />} />
          <Route path="features" element={<SystemConfigFeaturesPage />} />
        </Route>
      </Route>
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
