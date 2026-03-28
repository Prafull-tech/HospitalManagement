/**
 * Feature Flags Context
 * Fetches effective feature toggle states from the backend and provides
 * isEnabled() and uiModeFor() helpers throughout the app.
 */

import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { systemFeaturesApi } from '../api/system'

export type FeatureUiMode = 'hide' | 'disabled'

export interface EffectiveFeatureFlag {
  featureKey: string
  enabled: boolean
  uiMode: FeatureUiMode
  source?: string
}

interface FeatureFlagsContextValue {
  flags: EffectiveFeatureFlag[]
  featuresByKey: Record<string, EffectiveFeatureFlag>
  loading: boolean
  isEnabled: (key: string) => boolean
  uiModeFor: (key: string) => FeatureUiMode
}

const FeatureFlagsContext = createContext<FeatureFlagsContextValue | null>(null)

function normalize(key: string): string {
  return key.trim().replace(/-/g, '_').toUpperCase()
}

export function FeatureFlagsProvider({ children }: { children: ReactNode }) {
  const [flags, setFlags] = useState<EffectiveFeatureFlag[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    systemFeaturesApi
      .listEffective()
      .then((data) => {
        if (!cancelled) setFlags(data)
      })
      .catch(() => {
        // Non-critical: fail silently — all features remain enabled
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const featuresByKey = useMemo<Record<string, EffectiveFeatureFlag>>(() => {
    const map: Record<string, EffectiveFeatureFlag> = {}
    for (const f of flags) {
      map[normalize(f.featureKey)] = f
    }
    return map
  }, [flags])

  const value = useMemo<FeatureFlagsContextValue>(
    () => ({
      flags,
      featuresByKey,
      loading,
      isEnabled: (key: string) => featuresByKey[normalize(key)]?.enabled ?? true,
      uiModeFor: (key: string) => featuresByKey[normalize(key)]?.uiMode ?? 'hide',
    }),
    [flags, featuresByKey, loading]
  )

  return <FeatureFlagsContext.Provider value={value}>{children}</FeatureFlagsContext.Provider>
}

export function useFeatureFlags(): FeatureFlagsContextValue {
  const ctx = useContext(FeatureFlagsContext)
  if (!ctx) throw new Error('useFeatureFlags must be used inside FeatureFlagsProvider')
  return ctx
}

export function useFeatureFlagsOptional(): FeatureFlagsContextValue | null {
  return useContext(FeatureFlagsContext)
}