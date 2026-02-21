#!/usr/bin/env node
/**
 * Waits for the HMS backend to be ready before starting the frontend.
 * Polls http://localhost:8080/api/actuator/health until it returns 200 or timeout.
 */
const BACKEND_URL = 'http://localhost:8080/api/actuator/health'
const MAX_WAIT_MS = 90_000
const POLL_INTERVAL_MS = 2_000

const start = Date.now()
async function wait() {
  while (Date.now() - start < MAX_WAIT_MS) {
    try {
      const ctrl = new AbortController()
const t = setTimeout(() => ctrl.abort(), 3000)
const res = await fetch(BACKEND_URL, { signal: ctrl.signal })
clearTimeout(t)
      if (res.ok) {
        console.log('Backend is ready!')
        setTimeout(() => process.exit(0), 50)
        return
      }
    } catch {
      // ignore
    }
    const elapsed = Math.round((Date.now() - start) / 1000)
    console.log(`  Waiting for backend... (${elapsed}s)`)
    await new Promise((r) => setTimeout(r, POLL_INTERVAL_MS))
  }
  console.error(`ERROR: Backend did not become ready in ${MAX_WAIT_MS / 1000}s`)
  setTimeout(() => process.exit(1), 50)
}
wait()
