package com.hospital.hms.blog.controller;

import com.hospital.hms.blog.entity.BlogPost;
import com.hospital.hms.blog.repository.BlogPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/blog")
public class BlogController {

    private final BlogPostRepository repository;

    public BlogController(BlogPostRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Map<String, Object>>> listPosts() {
        List<BlogPost> posts = repository.findByPublishedTrueOrderByPublishedAtDesc();
        List<Map<String, Object>> result = posts.stream().map(this::toSummary).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<?> getPost(@PathVariable String slug) {
        return repository.findBySlugAndPublishedTrue(slug)
                .map(post -> ResponseEntity.ok(toDetail(post)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toSummary(BlogPost post) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", post.getId());
        m.put("title", post.getTitle());
        m.put("slug", post.getSlug());
        m.put("excerpt", post.getExcerpt());
        m.put("coverImage", post.getCoverImage());
        m.put("tag", post.getTag());
        m.put("author", post.getAuthor());
        m.put("publishedAt", post.getPublishedAt() != null ? post.getPublishedAt().toString() : "");
        return m;
    }

    private Map<String, Object> toDetail(BlogPost post) {
        Map<String, Object> m = toSummary(post);
        m.put("content", post.getContent());
        return m;
    }
}
