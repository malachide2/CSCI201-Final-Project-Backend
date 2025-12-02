package com.hikehub.backend.dto;

import com.hikehub.backend.validation.RatingIncrement;
import jakarta.validation.constraints.*;
import java.util.List;

public class CreateOrUpdateRatingRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1.0")
    @Max(value = 5, message = "Rating must be at most 5.0")
    @RatingIncrement(message = "Rating must be in 0.5 increments (e.g., 1.0, 1.5, 2.0, ..., 5.0)")
    private Double rating;
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
    
    private List<String> images;
    
    // Constructors
    public CreateOrUpdateRatingRequest() {
    }
    
    public CreateOrUpdateRatingRequest(Double rating, String comment, List<String> images) {
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }
    
    // Getters and Setters
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
}

