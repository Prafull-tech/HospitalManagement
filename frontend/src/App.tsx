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
import { ReceptionPatientsListPage } from './pages/reception/ReceptionPatientsListPage'
import { PatientRegisterPage } from './pages/PatientRegisterPage'
import { PatientSearchPage } from './pages/PatientSearchPage'
import { PatientViewPage } from './pages/PatientViewPage'
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
import { BillingTransactionsPage } from './pages/billing/BillingTransactionsPage'
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
import { SampleCollectionPage } from './pages/lab/SampleCollectionPage'
import { SampleProcessingPage } from './pages/lab/SampleProcessingPage'
import { ResultEntryPage } from './pages/lab/ResultEntryPage'
import { LabResultEntryPage } from './pages/lab/LabResultEntryPage'
import { ResultVerificationPage } from './pages/lab/ResultVerificationPage'
import { LabResultVerificationPage } from './pages/lab/LabResultVerificationPage'
import { ReportViewerPage } from './pages/lab/ReportViewerPage'
import { LabTestMasterPage } from './pages/lab/LabTestMasterPage'
import { RadiologyDashboard } from './pages/RadiologyDashboard'
import { BloodBankDashboard } from './pages/BloodBankDashboard'
import { HousekeepingDashboard } from './pages/patient-services/HousekeepingDashboard'
import { LaundryDashboard } from './pages/patient-services/LaundryDashboard'
import { DietaryDashboard } from './pages/patient-services/DietaryDashboard'
import { MealServiceDashboard } from './pages/patient-services/MealServiceDashboard'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { UnauthorizedPage } from './pages/UnauthorizedPage'
import { PlaceholderPage } from './pages/PlaceholderPage'
import { AppointmentDashboard } from './pages/appointments/AppointmentDashboard'
import { AppointmentBookingPage } from './pages/appointments/AppointmentBookingPage'
import { AppointmentQueuePage } from './pages/appointments/AppointmentQueuePage'
import { AppointmentSearchPage } from './pages/appointments/AppointmentSearchPage'
import { TokenDashboard } from './pages/tokens/TokenDashboard'
import { TokenQueuePage } from './pages/tokens/TokenQueuePage'
import { TokenDisplayScreen } from './pages/tokens/TokenDisplayScreen'
import { DoctorTokenPanel } from './pages/tokens/DoctorTokenPanel'
import { WalkinDashboard } from './pages/walkin/WalkinDashboard'
import { WalkinRegistrationForm } from './pages/walkin/WalkinRegistrationForm'

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
        {/* Front Office – role: FRONT_DESK, RECEPTIONIST, BILLING, ADMIN */}
        <Route path="front-office" element={<ProtectedRoute allowedRoles={['FRONT_DESK', 'RECEPTIONIST', 'BILLING', 'ADMIN']}><Outlet /></ProtectedRoute>}>
          <Route path="register" element={<PatientRegisterPage />} />
          <Route path="appointments" element={<Outlet />}>
            <Route index element={<AppointmentDashboard />} />
            <Route path="book" element={<AppointmentBookingPage />} />
            <Route path="queue" element={<AppointmentQueuePage />} />
            <Route path="search" element={<AppointmentSearchPage />} />
          </Route>
          <Route path="walkin" element={<Outlet />}>
            <Route index element={<WalkinDashboard />} />
            <Route path="register" element={<WalkinRegistrationForm />} />
          </Route>
          <Route path="enquiry" element={<PlaceholderPage title="Enquiry Desk" description="Handle patient enquiries and information requests. Coming in a future release." />} />
          <Route path="tokens" element={<Outlet />}>
            <Route index element={<TokenDashboard />} />
            <Route path="queue" element={<TokenQueuePage />} />
            <Route path="dashboard" element={<TokenDashboard />} />
          </Route>
          <Route path="visitors" element={<PlaceholderPage title="Visitor Pass" description="Issue and manage visitor passes. Coming in a future release." />} />
          <Route path="billing" element={<Navigate to="/billing" replace />} />
          <Route path="patients" element={<PatientSearchPage />} />
        </Route>
        {/* Patient Flow – role: DOCTOR, NURSE, RECEPTIONIST, FRONT_DESK, IPD_MANAGER, BILLING, ADMIN */}
        <Route path="patient-flow" element={<ProtectedRoute allowedRoles={['DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER', 'BILLING', 'ADMIN']}><Outlet /></ProtectedRoute>}>
          <Route path="opd" element={<Navigate to="/opd" replace />} />
          <Route path="consultation" element={<Navigate to="/opd/queue" replace />} />
          <Route path="lab-orders" element={<Navigate to="/lab" replace />} />
          <Route path="radiology-orders" element={<Navigate to="/radiology" replace />} />
          <Route path="pharmacy-orders" element={<Navigate to="/pharmacy" replace />} />
          <Route path="admission" element={<Navigate to="/ipd/admission-management" replace />} />
          <Route path="bed-allocation" element={<Navigate to="/ipd/beds" replace />} />
          <Route path="treatment" element={<Navigate to="/nursing" replace />} />
          <Route path="billing" element={<Navigate to="/billing" replace />} />
          <Route path="discharge" element={<Navigate to="/ipd/admissions" replace />} />
        </Route>
        <Route path="reception" element={<Outlet />}>
          <Route index element={<ReceptionDashboard />} />
          <Route path="patients" element={<ReceptionPatientsListPage />} />
          <Route path="register" element={<PatientRegisterPage />} />
          <Route path="search" element={<PatientSearchPage />} />
          <Route path="patient/:id" element={<PatientViewPage />} />
          <Route path="patient/:id/edit" element={<PatientRegisterPage />} />
        </Route>
        <Route path="pharmacy" element={<RoleProtectedRoute><PharmacyDashboard /></RoleProtectedRoute>} />
        <Route path="lab" element={<RoleProtectedRoute><Outlet /></RoleProtectedRoute>}>
          <Route index element={<LabDashboard />} />
          <Route path="collection" element={<SampleCollectionPage />} />
          <Route path="processing" element={<SampleProcessingPage />} />
          <Route path="sample-processing" element={<SampleProcessingPage />} />
          <Route path="results" element={<ResultEntryPage />} />
          <Route path="result-entry" element={<LabResultEntryPage />} />
          <Route path="verification" element={<ResultVerificationPage />} />
          <Route path="result-verification" element={<LabResultVerificationPage />} />
          <Route path="report/:orderId" element={<ReportViewerPage />} />
          <Route path="reports" element={<LabReportsPage />} />
          <Route path="view-reports" element={<LabReportsPage />} />
          <Route path="test-master" element={<LabTestMasterPage />} />
          <Route path="tests" element={<LabTestMasterPage />} />
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
          <Route path="tokens" element={<DoctorTokenPanel />} />
          <Route path="visits" element={<OPDSearchVisitsPage />} />
          <Route path="visits/:id" element={<OPDVisitDetailPage />} />
        </Route>
        <Route path="display" element={<Outlet />}>
          <Route path="tokens" element={<TokenDisplayScreen />} />
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
          <Route path="discharges" element={<IPDAdmissionsListPage />} />
          <Route path="current" element={<IPDAdmissionsListPage />} />
          <Route path="admissions/:id" element={<ViewAdmission />} />
          <Route path="admissions/:id/edit" element={<EditAdmissionPage />} />
        </Route>
        <Route path="billing" element={<ProtectedRoute allowedRoles={['BILLING', 'ADMIN', 'RECEPTIONIST']}><Outlet /></ProtectedRoute>}>
          <Route index element={<BillingDashboard />} />
          <Route path="transactions" element={<BillingTransactionsPage />} />
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
        <Route path="housekeeping" element={<HousekeepingDashboard />} />
        <Route path="laundry" element={<LaundryDashboard />} />
        <Route path="dietary" element={<DietaryDashboard />} />
        <Route path="meals" element={<MealServiceDashboard />} />
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
