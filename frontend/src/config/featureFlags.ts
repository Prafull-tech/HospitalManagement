/**
 * Production UX: hide sidebar entries for modules that are still placeholder/stub UIs.
 * Set VITE_SHOW_STUB_SIDEBAR=true to show them (e.g. demos). Default: hidden in production builds only.
 */
export function showStubSidebarItems(): boolean {
  const v = import.meta.env.VITE_SHOW_STUB_SIDEBAR
  if (v === 'true') return true
  if (v === 'false') return false
  return !import.meta.env.PROD
}
