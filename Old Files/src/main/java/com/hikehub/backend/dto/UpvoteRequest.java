package com.hikehub.backend.dto;

import jakarta.validation.constraints.NotNull;

public class UpvoteRequest {
    
    @NotNull(message = "upvoted field is required")
    private Boolean upvoted;
    
    // Constructors
    public UpvoteRequest() {
    }
    
    public UpvoteRequest(Boolean upvoted) {
        this.upvoted = upvoted;
    }
    
    // Getters and Setters
    public Boolean getUpvoted() {
        return upvoted;
    }
    
    public void setUpvoted(Boolean upvoted) {
        this.upvoted = upvoted;
    }
}

