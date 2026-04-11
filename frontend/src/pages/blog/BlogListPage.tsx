import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../../api/client'
import { useCompanyProfile } from '../../hooks/useCompanyProfile'
import styles from './Blog.module.css'

interface BlogPost {
  id: number
  title: string
  slug: string
  excerpt: string
  coverImage?: string
  tag?: string
  author: string
  publishedAt: string
}

export function BlogListPage() {
  const { profile } = useCompanyProfile()
  const [posts, setPosts] = useState<BlogPost[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    apiClient
      .get('/public/blog/posts')
      .then((res) => setPosts(res.data))
      .catch(() => setPosts([]))
      .finally(() => setLoading(false))
  }, [])

  const [featuredPost, ...otherPosts] = posts

  return (
    <div className={styles.wrapper}>
      <header className={styles.blogHero}>
        <div className={styles.blogHeroCopy}>
          <span className={styles.blogKicker}>Clinical operations journal</span>
          <h1 className={styles.heading}>Ideas, field notes, and practical patterns for hospital teams.</h1>
          <p className={styles.subheading}>
            The public journal from {profile.brandName} covers workflow design, inpatient coordination,
            patient administration, and the operational decisions that shape a calmer care environment.
          </p>
        </div>

        <div className={styles.blogHeroPanel}>
          <span className={styles.blogHeroLabel}>Editorial focus</span>
          <p>Admissions, ward visibility, finance closure, and the moments where hospital software should reduce coordination effort.</p>
        </div>
      </header>

      {loading ? (
        <div className={styles.empty}>Loading…</div>
      ) : posts.length === 0 ? (
        <div className={styles.empty}>
          <div className={styles.emptyIcon}>Journal</div>
          <p>No blog posts yet. Check back soon!</p>
        </div>
      ) : (
        <>
          <Link to={`/blog/${featuredPost.slug}`} className={styles.featuredCard}>
            <div className={styles.featuredMedia}>
              {featuredPost.coverImage ? (
                <img src={featuredPost.coverImage} alt={featuredPost.title} className={styles.cardImage} />
              ) : (
                <div className={styles.cardImagePlaceholder}>{profile.brandName} Journal</div>
              )}
            </div>
            <div className={styles.featuredBody}>
              <div className={styles.cardMeta}>
                {featuredPost.tag ? <span className={styles.cardTag}>{featuredPost.tag}</span> : null}
                <span>{new Date(featuredPost.publishedAt).toLocaleDateString()}</span>
                <span>{featuredPost.author}</span>
              </div>
              <h2 className={styles.featuredTitle}>{featuredPost.title}</h2>
              <p className={styles.featuredExcerpt}>{featuredPost.excerpt}</p>
              <span className={styles.readMore}>Read the feature</span>
            </div>
          </Link>

          <div className={styles.sectionHeaderRow}>
            <h2 className={styles.sectionHeading}>Latest articles</h2>
            <span className={styles.sectionMeta}>{posts.length} published pieces</span>
          </div>

          <div className={styles.grid}>
            {otherPosts.map((post) => (
              <Link key={post.id} to={`/blog/${post.slug}`} className={styles.card}>
                {post.coverImage ? (
                  <img src={post.coverImage} alt={post.title} className={styles.cardImage} />
                ) : (
                  <div className={styles.cardImagePlaceholder}>{profile.brandName} Blog</div>
                )}
                <div className={styles.cardBody}>
                  <div className={styles.cardMeta}>
                    {post.tag ? <span className={styles.cardTag}>{post.tag}</span> : null}
                    <span>{new Date(post.publishedAt).toLocaleDateString()}</span>
                  </div>
                  <h2 className={styles.cardTitle}>{post.title}</h2>
                  <p className={styles.cardExcerpt}>{post.excerpt}</p>
                  <div className={styles.cardFooterMeta}>
                    <span>{post.author}</span>
                    <span className={styles.readMore}>Read more</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
