-- =============================================================================
-- V5: Contact messages and blog posts tables
-- =============================================================================

-- Contact messages
CREATE TABLE IF NOT EXISTS contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at DATETIME(6) NOT NULL,
    KEY idx_contact_status (status),
    KEY idx_contact_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Blog posts
CREATE TABLE IF NOT EXISTS blog_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL,
    excerpt VARCHAR(1000),
    content MEDIUMTEXT,
    cover_image VARCHAR(1000),
    tag VARCHAR(100),
    author VARCHAR(255) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    UNIQUE KEY idx_blog_slug (slug),
    KEY idx_blog_published (published, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add email and phone columns to hms_users if not present (for signup)
ALTER TABLE hms_users ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE hms_users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
