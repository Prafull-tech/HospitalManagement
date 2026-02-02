export interface DashboardStatsDto {
  fromDate: string
  toDate: string
  totalPatientsRegistered: number
  totalOPDVisits: number
  totalAdmitted: number
  totalDischarged: number
  totalCurrentlyAdmitted: number
  totalCollection: number
}
