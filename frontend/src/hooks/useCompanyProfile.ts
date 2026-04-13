import { useEffect, useState } from 'react'
import { publicCompanyProfileApi } from '../api/system'
import type { CompanyProfileResponse } from '../types/system'
import { useAppBootstrap } from '../components/AppBootstrap'

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
  const bootstrap = useAppBootstrap()
  const [profile, setProfile] = useState<CompanyProfileResponse>(DEFAULT_COMPANY_PROFILE)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (bootstrap?.tenantLoading) {
      return
    }

    if (bootstrap?.tenant?.tenantResolved) {
      setProfile({
        ...DEFAULT_COMPANY_PROFILE,
        companyName: bootstrap.tenant.hospitalName || DEFAULT_COMPANY_PROFILE.companyName,
        brandName: bootstrap.tenant.hospitalName || DEFAULT_COMPANY_PROFILE.brandName,
        logoUrl: bootstrap.tenant.logoUrl || DEFAULT_COMPANY_PROFILE.logoUrl,
        supportEmail: bootstrap.tenant.contactEmail || DEFAULT_COMPANY_PROFILE.supportEmail,
        supportPhone: bootstrap.tenant.contactPhone || DEFAULT_COMPANY_PROFILE.supportPhone,
      })
      setLoading(false)
      return
    }

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
  }, [bootstrap?.tenant, bootstrap?.tenantLoading])

  return { profile, loading }
}