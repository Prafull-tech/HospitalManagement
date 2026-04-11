import { Link } from 'react-router-dom'
import { useCompanyProfile } from '../hooks/useCompanyProfile'
import styles from './MarketingLandingPage.module.css'

const pressurePoints = [
  {
    title: 'Front desk overload',
    text: 'Registrations, queues, and cash collection often rely on disconnected handoffs that slow the first patient touchpoint.',
  },
  {
    title: 'Inpatient blind spots',
    text: 'Bed movement, nursing updates, pharmacy requests, and discharge readiness can drift apart without a shared operational view.',
  },
  {
    title: 'Delayed financial closure',
    text: 'When billing, diagnostics, and doctor orders do not reconcile in real time, teams chase the file instead of serving patients.',
  },
]

const systemModules = [
  'Reception and patient onboarding',
  'OPD scheduling and doctor flow',
  'IPD admissions, beds, and transfers',
  'Billing, packages, and discharge clearance',
  'Lab, pharmacy, and nursing coordination',
  'Daily management with audit-friendly records',
]

const outcomes = [
  { value: '1 shared record', label: 'for patient, clinician, and finance workflows' },
  { value: '24/7 clarity', label: 'across beds, queues, admissions, and discharge' },
  { value: 'Fewer handoffs', label: 'between reception, ward, lab, pharmacy, and billing' },
]

export function MarketingLandingPage() {
  const { profile } = useCompanyProfile()

  return (
    <div className={styles.page}>
      <header className={styles.hero}>
        <div className={styles.heroCopy}>
          <span className={styles.badge}>Operational software for modern hospitals</span>
          <h1 className={styles.title}>A calmer patient journey starts with a tighter operating system.</h1>
          <p className={styles.subtitle}>
            {profile.brandName} brings admissions, care coordination, billing, diagnostics, and discharge
            into one connected workflow so every department works from the same live picture.
          </p>

          <div className={styles.heroActions}>
            <a href="#contact" className={styles.primaryCta}>Request a guided walkthrough</a>
            <a href="#features" className={styles.secondaryCta}>Explore the platform</a>
          </div>

          <div className={styles.heroMetrics}>
            {outcomes.map((outcome) => (
              <div key={outcome.value} className={styles.metricCard}>
                <strong>{outcome.value}</strong>
                <span>{outcome.label}</span>
              </div>
            ))}
          </div>
        </div>

        <aside className={styles.heroBoard}>
          <div className={styles.boardHeader}>
            <div>
              <p className={styles.boardEyebrow}>Live operations board</p>
              <h2 className={styles.boardTitle}>What hospital teams need in the same moment</h2>
            </div>
            <span className={styles.boardBadge}>Cross-department view</span>
          </div>

          <div className={styles.boardTimeline}>
            <div className={styles.timelineItem}>
              <span className={styles.timelineTime}>08:10</span>
              <div>
                <strong>Reception</strong>
                <p>Patient registration, token issue, and doctor routing begin without duplicate entry.</p>
              </div>
            </div>
            <div className={styles.timelineItem}>
              <span className={styles.timelineTime}>11:25</span>
              <div>
                <strong>IPD and nursing</strong>
                <p>Bed assignment, medication requests, and transfer visibility stay aligned on one thread.</p>
              </div>
            </div>
            <div className={styles.timelineItem}>
              <span className={styles.timelineTime}>16:40</span>
              <div>
                <strong>Billing and discharge</strong>
                <p>Clearance closes faster when lab, pharmacy, and billing status are already reconciled.</p>
              </div>
            </div>
          </div>

          <div className={styles.boardFooter}>
            Already operating with {profile.brandName}? <Link to="/login">Sign in</Link> or <Link to="/signup">create a workspace</Link>.
          </div>
        </aside>
      </header>

      <section className={styles.trustStrip} aria-label="Platform strengths">
        <span>Unified patient context</span>
        <span>Ward-ready coordination</span>
        <span>Finance-aware workflows</span>
        <span>Implementation that grows by department</span>
      </section>

      <main className={styles.sections}>
        <section id="challenge" className={styles.storySection}>
          <div className={styles.sectionIntro}>
            <span className={styles.sectionKicker}>Where hospitals lose time</span>
            <h2 className={styles.sectionTitle}>Operational drag shows up long before anyone calls it a systems problem.</h2>
            <p className={styles.sectionText}>
              When clinical, admin, and finance teams are forced to reconcile decisions after the fact,
              the patient experience becomes slower, staff feel reactive, and reporting becomes brittle.
            </p>
          </div>

          <div className={styles.pressureGrid}>
            {pressurePoints.map((item) => (
              <article key={item.title} className={styles.pressureCard}>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            ))}
          </div>
        </section>

        <section id="features" className={styles.featureSection}>
          <div className={styles.featurePanel}>
            <span className={styles.sectionKicker}>Built for the whole journey</span>
            <h2 className={styles.sectionTitle}>One operational layer from first registration to final discharge.</h2>
            <p className={styles.sectionText}>
              {profile.companyName} is designed so departments can move together rather than passing paper,
              spreadsheets, or verbal updates between shifts.
            </p>
          </div>

          <div className={styles.moduleGrid}>
            {systemModules.map((moduleName, index) => (
              <article key={moduleName} className={styles.moduleCard}>
                <span className={styles.moduleIndex}>0{index + 1}</span>
                <h3>{moduleName}</h3>
                <p>
                  Structured workflows, cleaner visibility, and less follow-up work for the people keeping
                  daily care operations moving.
                </p>
              </article>
            ))}
          </div>
        </section>

        <section className={styles.editorialSection}>
          <article className={styles.editorialCard}>
            <span className={styles.sectionKicker}>Why teams adopt in phases</span>
            <h3>Start where friction is highest, then expand without rebuilding the process.</h3>
            <p>
              Many hospitals begin with reception, billing, or inpatient management and then extend the same
              data backbone into nursing, diagnostics, and pharmacy once the first workflow stabilizes.
            </p>
          </article>

          <article className={styles.editorialAccent}>
            <p className={styles.editorialQuote}>
              “Good hospital software should reduce coordination effort, not just digitize the paperwork.”
            </p>
            <span className={styles.editorialNote}>That principle shapes the public experience and the operational workspace behind it.</span>
          </article>
        </section>

        <section id="contact" className={styles.ctaSection}>
          <div className={styles.ctaCopy}>
            <span className={styles.sectionKicker}>Next step</span>
            <h2 className={styles.sectionTitle}>See how your current admissions, ward, and billing flow would look inside the platform.</h2>
            <p className={styles.sectionText}>
              Bring one real hospital workflow to the discussion and we will map how it can run inside
              {` ${profile.brandName}`} with fewer handoffs and clearer accountability.
            </p>
          </div>

          <div className={styles.ctaActions}>
            <Link to="/contact" className={styles.primaryCta}>Talk to the team</Link>
            <Link to="/signup" className={styles.inlineLink}>Open a public workspace</Link>
          </div>
        </section>
      </main>
    </div>
  )
}

