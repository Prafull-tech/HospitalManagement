import { useEffect, useState } from 'react'
import { publicCompanyProfileApi } from '../api/system'
import type { CompanyProfileResponse } from '../types/system'

export const DEFAULT_COMPANY_PROFILE: CompanyProfileResponse = {
  id: 1,
  companyName: 'HMS Hospital Management System',
  brandName: 'HMS',
  logoText: 'HMS',
  logoUrl: '',
  supportEmail: 'support@hms-hospital.com',
  supportPhone: '+91 22 1234 5678',
  addressText: 'HMS Office, Health Tech Park, Mumbai, Maharashtra, India',
}

export function useCompanyProfile() {
  const [profile, setProfile] = useState<CompanyProfileResponse>(DEFAULT_COMPANY_PROFILE)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    publicCompanyProfileApi
      .get()
      .then((data) => {
        if (active) {
          setProfile({ ...DEFAULT_COMPANY_PROFILE, ...data })
        }
      })
      .catch(() => {
        if (active) {
          setProfile(DEFAULT_COMPANY_PROFILE)
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [])

  return { profile, loading }
}