import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import { PermissionsProvider } from './contexts/PermissionsContext'
import { Layout } from './components/Layout'
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

export default function App() {
  const { user } = useAuth()
  const roleCodes = user?.roles?.length ? user.roles : ['ADMIN']

  return (
    <Routes>
      <Route path="/" element={<PermissionsProvider roleCodes={roleCodes}><Layout /></PermissionsProvider>}>
        <Route index element={<Navigate to="/reception" replace />} />
        <Route path="reception" element={<Outlet />}>
          <Route index element={<ReceptionDashboard />} />
          <Route path="register" element={<PatientRegisterPage />} />
          <Route path="search" element={<PatientSearchPage />} />
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
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
