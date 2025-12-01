package com.hikehub.backend.dto;

import com.hikehub.backend.validation.RatingIncrement;
import jakarta.validation.constraints.*;

public class CreateOrUpdateReviewRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1.0")
    @Max(value = 5, message = "Rating must be at most 5.0")
    @RatingIncrement(message = "Rating must be in 0.5 increments (e.g., 1.0, 1.5, 2.0, ..., 5.0)")
    private Double rating;
    
    @Size(max = 2000, message = "Review body must not exceed 2000 characters")
    private String reviewBody;
    
    // Constructors
    public CreateOrUpdateReviewRequest() {
    }
    
    public CreateOrUpdateReviewRequest(Double rating, String reviewBody) {
        this.rating = rating;
        this.reviewBody = reviewBody;
    }
    
    // Getters and Setters
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
}

