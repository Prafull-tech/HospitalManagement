import { useState } from 'react'
import { apiClient } from '../api/client'
import { useCompanyProfile } from '../hooks/useCompanyProfile'
import styles from './ContactPage.module.css'

export function ContactPage() {
  const { profile } = useCompanyProfile()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [subject, setSubject] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!name.trim() || !email.trim() || !message.trim()) {
      setError('Name, email, and message are required.')
      return
    }

    try {
      setLoading(true)
      await apiClient.post('/public/contact', {
        name: name.trim(),
        email: email.trim(),
        phone: phone.trim() || undefined,
        subject: subject.trim() || undefined,
        message: message.trim(),
      })
      setSuccess(true)
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Failed to send message. Please try again.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <header className={styles.hero}>
        <div className={styles.heroCopy}>
          <span className={styles.kicker}>Talk with the team</span>
          <h1 className={styles.heading}>Bring one real hospital workflow. We’ll help you map the better version.</h1>
          <p className={styles.subheading}>
            Whether you are exploring admissions, inpatient visibility, or billing closure, {profile.brandName}
            can be introduced around the parts of the operation creating the most drag.
          </p>
        </div>

        <div className={styles.infoCards}>
          <div className={styles.infoCard}>
            <span className={styles.infoLabel}>Visit</span>
            <h3>Primary office</h3>
            <p>{profile.addressText || 'Address not configured yet.'}</p>
          </div>
          <div className={styles.infoCard}>
            <span className={styles.infoLabel}>Email</span>
            <h3>Support and sales</h3>
            <p>{profile.supportEmail || 'Email not configured yet.'}</p>
          </div>
          <div className={styles.infoCard}>
            <span className={styles.infoLabel}>Call</span>
            <h3>Direct line</h3>
            <p>{profile.supportPhone || 'Phone not configured yet.'}</p>
          </div>
        </div>
      </header>

      <div className={styles.contentGrid}>
        <section className={styles.formCard}>
          <div className={styles.formHeader}>
            <h2>Send a message</h2>
            <p>Tell us where your operational bottlenecks are showing up first.</p>
          </div>

          {success ? (
            <div className={styles.success}>
              Thank you. Your message has been sent and the team will get back to you soon.
            </div>
          ) : (
            <form onSubmit={handleSubmit} className={styles.form}>
              {error ? <div className={styles.error}>{error}</div> : null}

              <div className={styles.row}>
                <label className={styles.field}>
                  <span>Name *</span>
                  <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" />
                </label>
                <label className={styles.field}>
                  <span>Email *</span>
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
                </label>
              </div>

              <div className={styles.row}>
                <label className={styles.field}>
                  <span>Phone</span>
                  <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+91 98765 43210" />
                </label>
                <label className={styles.field}>
                  <span>Subject</span>
                  <input type="text" value={subject} onChange={(e) => setSubject(e.target.value)} placeholder="e.g. Demo request" />
                </label>
              </div>

              <label className={styles.field}>
                <span>Message *</span>
                <textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Tell us how we can help…" />
              </label>

              <button type="submit" className={styles.submitBtn} disabled={loading}>
                {loading ? 'Sending…' : 'Send Message'}
              </button>
            </form>
          )}
        </section>

        <aside className={styles.sidebar}>
          <div className={styles.sidebarCard}>
            <span className={styles.infoLabel}>Consultation window</span>
            <h3>Business hours</h3>
            <p>
              Monday – Friday: 9:00 AM – 6:00 PM<br />
              Saturday: 10:00 AM – 2:00 PM<br />
              Sunday: Closed
            </p>
          </div>

          <div className={styles.sidebarCardDark}>
            <span className={styles.infoLabelDark}>What to bring</span>
            <h3>Your current admission, billing, or discharge flow.</h3>
            <p>
              The fastest conversation starts with a real process. We’ll show how {profile.brandName} can reduce handoffs and improve visibility around it.
            </p>
          </div>

          <div className={styles.sidebarCard}>
            <span className={styles.infoLabel}>Location</span>
            <div className={styles.mapPlaceholder}>Coverage across hospital operations</div>
          </div>
        </aside>
      </div>
    </div>
  )
}
