import styles from './NursingNotesTableSkeleton.module.css'

const ROWS = 10

export function NursingNotesTableSkeleton() {
  return (
    <div className={styles.wrap} aria-busy="true" aria-label="Loading nursing notes">
      <div className={styles.toolbar}>
        <span className={styles.countLine} />
        <span className={styles.btnLine} />
      </div>
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
              <th><span className={styles.thLine} /></th>
            </tr>
          </thead>
          <tbody>
            {Array.from({ length: ROWS }).map((_, i) => (
              <tr key={i}>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
                <td><span className={styles.tdLine} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
