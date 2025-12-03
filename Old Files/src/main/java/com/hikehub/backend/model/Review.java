package com.hikehub.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hike_id", nullable = false)
    private Hike hike;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Double rating;
    
    @Column(name = "review_body", columnDefinition = "TEXT")
    private String reviewBody;
    
    @Column(name = "upvotes_count", nullable = false)
    private Integer upvotesCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (upvotesCount == null) {
            upvotesCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Constructors
    public Review() {
    }
    
    public Review(Hike hike, User user, Double rating, String reviewBody) {
        this.hike = hike;
        this.user = user;
        this.rating = rating;
        this.reviewBody = reviewBody;
        this.upvotesCount = 0;
    }
    
    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    
    public Hike getHike() {
        return hike;
    }
    
    public void setHike(Hike hike) {
        this.hike = hike;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getReviewBody() {
        return reviewBody;
    }
    
    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }
    
    public Integer getUpvotesCount() {
        return upvotesCount;
    }
    
    public void setUpvotesCount(Integer upvotesCount) {
        this.upvotesCount = upvotesCount;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

