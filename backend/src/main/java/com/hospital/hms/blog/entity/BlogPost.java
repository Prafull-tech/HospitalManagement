package com.hospital.hms.blog.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts", indexes = {
        @Index(name = "idx_blog_slug", columnList = "slug", unique = true),
        @Index(name = "idx_blog_published", columnList = "published, published_at")
})
public class BlogPost extends BaseIdEntity {

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @NotBlank
    @Size(max = 500)
    @Column(name = "slug", nullable = false, unique = true, length = 500)
    private String slug;

    @Size(max = 1000)
    @Column(name = "excerpt", length = 1000)
    private String excerpt;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @Size(max = 1000)
    @Column(name = "cover_image", length = 1000)
    private String coverImage;

    @Size(max = 100)
    @Column(name = "tag", length = 100)
    private String tag;

    @NotBlank
    @Size(max = 255)
    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "published", nullable = false)
    private Boolean published = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
