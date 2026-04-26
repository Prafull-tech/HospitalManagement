import { doctorsApi } from '../api/doctors'
import type { User } from '../contexts/AuthContext'
import type { DoctorResponse } from '../types/doctor'

function normalize(value: string | null | undefined): string {
  return (value || '').trim().toLowerCase()
}

function uniqueTerms(user: User | null): string[] {
  return Array.from(
    new Set(
      [user?.email, user?.fullName, user?.username]
        .map((value) => value?.trim())
        .filter((value): value is string => !!value)
    )
  )
}

function scoreDoctorMatch(doctor: DoctorResponse, user: User | null): number {
  const doctorEmail = normalize(doctor.email)
  const doctorFullName = normalize(doctor.fullName)
  const userEmail = normalize(user?.email)
  const userFullName = normalize(user?.fullName)
  const userUsername = normalize(user?.username)

  let score = 0
  if (userEmail && doctorEmail && doctorEmail === userEmail) score += 100
  if (userFullName && doctorFullName && doctorFullName === userFullName) score += 80
  if (userFullName && doctorFullName.includes(userFullName)) score += 25
  if (userUsername && normalize(doctor.code) === userUsername) score += 20
  if (userUsername && doctorFullName.includes(userUsername)) score += 10
  return score
}

export async function resolveCurrentDoctor(user: User | null): Promise<DoctorResponse | null> {
  const terms = uniqueTerms(user)
  if (!terms.length) return null

  const matches = new Map<number, DoctorResponse>()
  for (const term of terms) {
    const page = await doctorsApi.list({ search: term, status: 'ACTIVE', page: 0, size: 25 })
    for (const doctor of page.content) {
      matches.set(doctor.id, doctor)
    }
  }

  const doctors = [...matches.values()]
  if (!doctors.length) return null

  const ranked = doctors
    .map((doctor) => ({ doctor, score: scoreDoctorMatch(doctor, user) }))
    .sort((left, right) => right.score - left.score)

  if (ranked[0]?.score > 0) {
    return ranked[0].doctor
  }

  return doctors.length === 1 ? doctors[0] : null
}

export function getTodayIsoDate(): string {
  const now = new Date()
  const localTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
  return localTime.toISOString().slice(0, 10)
}

export function getWeekDates(anchorDate: string): string[] {
  const base = new Date(`${anchorDate}T00:00:00`)
  const day = base.getDay()
  const mondayOffset = day === 0 ? -6 : 1 - day
  const monday = new Date(base)
  monday.setDate(base.getDate() + mondayOffset)

  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday)
    date.setDate(monday.getDate() + index)
    const localTime = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    return localTime.toISOString().slice(0, 10)
  })
}

export function formatDisplayDate(value: string): string {
  return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

export function formatDisplayDateTime(date: string, time?: string | null): string {
  const normalizedTime = (time || '00:00').slice(0, 5)
  return new Date(`${date}T${normalizedTime}:00`).toLocaleString(undefined, {
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
  })
}