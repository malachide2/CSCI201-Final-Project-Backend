package com.hikehub.backend.dto;

import java.time.Instant;

public class ReviewResponseDto {
    private Long reviewId;
    private Double rating;
    private String reviewBody;
    private String authorUsername;
    private Long authorId;
    private Integer upvotesCount;
    private Boolean currentUserUpvoted;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Constructors
    public ReviewResponseDto() {
    }
    
    public ReviewResponseDto(Long reviewId, Double rating, String reviewBody, 
                            String authorUsername, Long authorId, Integer upvotesCount,
                            Boolean currentUserUpvoted, Instant createdAt, Instant updatedAt) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.reviewBody = reviewBody;
        this.authorUsername = authorUsername;
        this.authorId = authorId;
        this.upvotesCount = upvotesCount;
        this.currentUserUpvoted = currentUserUpvoted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
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
    
    public String getAuthorUsername() {
        return authorUsername;
    }
    
    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public Integer getUpvotesCount() {
        return upvotesCount;
    }
    
    public void setUpvotesCount(Integer upvotesCount) {
        this.upvotesCount = upvotesCount;
    }
    
    public Boolean getCurrentUserUpvoted() {
        return currentUserUpvoted;
    }
    
    public void setCurrentUserUpvoted(Boolean currentUserUpvoted) {
        this.currentUserUpvoted = currentUserUpvoted;
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

