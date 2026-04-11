import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { apiClient } from '../../api/client'
import { useCompanyProfile } from '../../hooks/useCompanyProfile'
import styles from './Blog.module.css'

interface BlogPostDetail {
  id: number
  title: string
  slug: string
  content: string
  coverImage?: string
  tag?: string
  author: string
  publishedAt: string
}

export function BlogPostPage() {
  const { slug } = useParams<{ slug: string }>()
  const { profile } = useCompanyProfile()
  const [post, setPost] = useState<BlogPostDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    if (!slug) return
    apiClient
      .get(`/public/blog/posts/${encodeURIComponent(slug)}`)
      .then((res) => setPost(res.data))
      .catch((err) => {
        if (err?.response?.status === 404) setNotFound(true)
      })
      .finally(() => setLoading(false))
  }, [slug])

  if (loading) {
    return (
      <div className={styles.articleShell}>
        <p className={styles.articleLoading}>Loading…</p>
      </div>
    )
  }

  if (notFound || !post) {
    return (
      <div className={styles.articleShell}>
        <Link to="/blog" className={styles.articleBack}>← Back to Blog</Link>
        <h1 className={styles.articleTitle}>Post Not Found</h1>
        <p className={styles.articleEmptyText}>
          The blog post you're looking for doesn't exist or has been removed.
        </p>
      </div>
    )
  }

  return (
    <article className={styles.articleShell}>
      <Link to="/blog" className={styles.articleBack}>← Back to Blog</Link>

      <header className={styles.articleHeader}>
        <div className={styles.articleHeaderCopy}>
          <div className={styles.articleMeta}>
            {post.tag ? <span className={styles.cardTag}>{post.tag}</span> : null}
            <span>{new Date(post.publishedAt).toLocaleDateString()}</span>
            <span>By {post.author}</span>
          </div>
          <h1 className={styles.articleTitle}>{post.title}</h1>
          <p className={styles.articleIntro}>
            Published in the {profile.brandName} journal for hospital leaders, operators, and care teams refining how daily work moves.
          </p>
        </div>

        <aside className={styles.articleAside}>
          <span className={styles.articleAsideLabel}>Reading note</span>
          <p>Use this piece as an operational reference point, then map the same ideas back into your admissions, ward, billing, or discharge flow.</p>
        </aside>
      </header>

      {post.coverImage ? <img src={post.coverImage} alt={post.title} className={styles.articleCover} /> : null}

      <div className={styles.articleContent} dangerouslySetInnerHTML={{ __html: post.content }} />
    </article>
  )
}
