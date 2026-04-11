#!/usr/bin/env node
import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const [, , command, ...rawArgs] = process.argv

if (!command || !['dev', 'preview', 'build'].includes(command)) {
  console.error('Usage: node scripts/run-vite.js <dev|preview|build> [vite args...]')
  process.exit(1)
}

const waitForBackend = rawArgs.includes('--wait-for-backend')
const viteArgs = rawArgs.filter((arg) => arg !== '--wait-for-backend')
const scriptDir = path.dirname(fileURLToPath(import.meta.url))
const frontendRoot = path.resolve(scriptDir, '..')
const viteEntry = path.resolve(frontendRoot, 'node_modules', 'vite', 'bin', 'vite.js')
const waitScript = path.resolve(scriptDir, 'wait-for-backend.js')

function normalizePositionalArgs(args) {
  const normalized = []
  let positionalHostUsed = false
  let positionalPortUsed = false

  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index]
    if (!arg.startsWith('-')) {
      if (!positionalHostUsed && /^[a-zA-Z0-9_.:-]+$/.test(arg)) {
        normalized.push('--host', arg)
        positionalHostUsed = true
        continue
      }
      if (!positionalPortUsed && /^\d+$/.test(arg)) {
        normalized.push('--port', arg)
        positionalPortUsed = true
        continue
      }
    }
    normalized.push(arg)
  }

  return normalized
}

function mergeNpmConfigArgs(args) {
  const merged = normalizePositionalArgs(args)
  const knownOptions = [
    ['host', '--host'],
    ['port', '--port'],
    ['strict_port', '--strictPort'],
    ['open', '--open'],
    ['mode', '--mode'],
    ['base', '--base'],
  ]

  for (const [envKey, flag] of knownOptions) {
    const value = process.env[`npm_config_${envKey}`]
    if (value === undefined || value === '' || merged.includes(flag)) {
      continue
    }
    if (value === 'true') {
      if (flag === '--host' || flag === '--port' || flag === '--mode' || flag === '--base') {
        continue
      }
      merged.push(flag)
      continue
    }
    if (value === 'false') {
      continue
    }
    merged.push(flag, value)
  }

  return merged
}

function run(commandName, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(commandName, args, {
      cwd: frontendRoot,
      stdio: 'inherit',
      shell: false,
      ...options,
    })

    child.on('error', reject)
    child.on('exit', (code, signal) => {
      if (signal) {
        reject(new Error(`${commandName} exited with signal ${signal}`))
        return
      }
      resolve(code ?? 0)
    })
  })
}

async function main() {
  try {
    if (waitForBackend) {
      const waitCode = await run(process.execPath, [waitScript])
      if (waitCode !== 0) {
        process.exit(waitCode)
      }
    }

    const code = await run(process.execPath, [viteEntry, command, ...mergeNpmConfigArgs(viteArgs)])
    process.exit(code)
  } catch (error) {
    console.error(error instanceof Error ? error.message : String(error))
    process.exit(1)
  }
}

main()