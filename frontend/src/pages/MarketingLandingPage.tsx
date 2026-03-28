import { Link } from 'react-router-dom'
import styles from './MarketingLandingPage.module.css'

export function MarketingLandingPage() {
  return (
    <div className={styles.page}>
      <header className={styles.hero}>
        <div className={styles.heroText}>
          <span className={styles.badge}>Since 1993 · Trusted HMS</span>
          <h1 className={styles.title}>
            Transform your healthcare and transport business with HMS software
          </h1>
          <p className={styles.subtitle}>
            Comprehensive hospital and transport management software designed to streamline operations,
            enhance patient care, and improve revenue performance across your organisation.
          </p>
          <div className={styles.heroActions}>
            <a href="#contact" className={styles.primaryCta}>
              Request demo
            </a>
            <a href="#features" className={styles.secondaryCta}>
              Learn more
            </a>
          </div>
          <div className={styles.heroMeta}>
            <div className={styles.heroMetaItem}>
              <span className={styles.heroStatLabel}>Better client care</span>
              <span className={styles.heroStatValue}>Unified view of each patient and visit</span>
            </div>
            <div className={styles.heroMetaItem}>
              <span className={styles.heroStatLabel}>Streamlined practices</span>
              <span className={styles.heroStatValue}>Integrated workflows, fewer errors</span>
            </div>
            <div className={styles.heroMetaItem}>
              <span className={styles.heroStatLabel}>Trusted since 1993</span>
              <span className={styles.heroStatValue}>From small clinics to large hospitals</span>
            </div>
          </div>
        </div>

        <aside className={styles.heroRight}>
          <h2 className={styles.heroRightTitle}>Get started with HMS</h2>
          <ul className={styles.heroList}>
            <li className={styles.heroListItem}>
              <span className={styles.heroBullet}>1.</span>
              <span>Register your hospital and configure departments, doctors, and services.</span>
            </li>
            <li className={styles.heroListItem}>
              <span className={styles.heroBullet}>2.</span>
              <span>Onboard your team to manage reception, OPD, IPD, billing, lab, and pharmacy.</span>
            </li>
            <li className={styles.heroListItem}>
              <span className={styles.heroBullet}>3.</span>
              <span>Track patients end‑to‑end with unified clinical and financial records.</span>
            </li>
          </ul>
          <div className={styles.authLinks}>
            Already using HMS?{' '}
            <Link to="/login">Login</Link>
            {' '}or{' '}
            <Link to="/register">create a new hospital account</Link>.
          </div>
        </aside>
      </header>

      <main className={styles.sections}>
        <section id="challenge" className={styles.section}>
          <div>
            <h2 className={styles.sectionHeaderTitle}>Overwhelmed by operations, limited by time</h2>
            <p className={styles.sectionHeaderText}>
              Many hospitals run on disconnected systems and manual processes. Teams juggle multiple
              registers, spreadsheets, and legacy tools that slow them down and increase the risk of
              errors.
            </p>
          </div>
          <div className={styles.cardsGrid}>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Fragmented data</h3>
              <p className={styles.cardText}>
                Patient, billing, lab, and pharmacy data spread across systems makes it hard to see the full
                picture or respond quickly.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Manual workflows</h3>
              <p className={styles.cardText}>
                Paper registers, repeated data entry, and manual reporting consume valuable staff time
                every day.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Regulatory pressure</h3>
              <p className={styles.cardText}>
                Meeting audit and reporting requirements is difficult when information is incomplete or
                inconsistent.
              </p>
            </div>
          </div>
        </section>

        <section id="features" className={styles.section}>
          <div>
            <h2 className={styles.sectionHeaderTitle}>The HMS solution: streamlined operations, enhanced care</h2>
            <p className={styles.sectionHeaderText}>
              HMS brings your reception, OPD, IPD, billing, lab, radiology, and pharmacy together into a
              single, integrated platform so your team can focus on patients instead of paperwork.
            </p>
          </div>
          <div className={styles.cardsGrid}>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Unified patient record</h3>
              <p className={styles.cardText}>
                View registrations, visits, admissions, diagnostics, and bills in one place for each patient.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Scheduling & staff management</h3>
              <p className={styles.cardText}>
                Manage OPD queues, doctor availability, and IPD beds centrally to reduce waiting times and
                improve utilisation.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Financial management</h3>
              <p className={styles.cardText}>
                Streamline billing, collections, insurance and corporate accounts with clear, auditable
                records.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Mobile‑ready workflows</h3>
              <p className={styles.cardText}>
                Access key information from wards or clinics and keep teams aligned with real‑time updates.
              </p>
            </div>
          </div>
        </section>

        <section id="contact" className={styles.section}>
          <div>
            <h2 className={styles.sectionHeaderTitle}>Empower your team, elevate your care</h2>
            <p className={styles.sectionHeaderText}>
              Discover how HMS can help you save money, free up time for patients, and deliver better care
              across every department.
            </p>
          </div>
          <div className={styles.cardsGrid}>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Schedule a personalised demo</h3>
              <p className={styles.cardText}>
                Walk through your current workflow with our team and see how HMS can support your exact
                processes.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Talk to our implementation experts</h3>
              <p className={styles.cardText}>
                Plan a phased go‑live for reception, OPD, IPD and billing with minimal disruption to daily
                operations.
              </p>
            </div>
            <div className={styles.card}>
              <h3 className={styles.cardTitle}>Start with reception & billing</h3>
              <p className={styles.cardText}>
                You can begin with registrations and billing today and add modules like IPD, lab and pharmacy
                as you grow.
              </p>
            </div>
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        © {new Date().getFullYear()} HMS Hospital Management System. All rights reserved.
      </footer>
    </div>
  )
}

