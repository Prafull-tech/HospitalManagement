package com.hospital.hms.blog.repository;

import com.hospital.hms.blog.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByPublishedTrueOrderByPublishedAtDesc();
    Optional<BlogPost> findBySlugAndPublishedTrue(String slug);
}
