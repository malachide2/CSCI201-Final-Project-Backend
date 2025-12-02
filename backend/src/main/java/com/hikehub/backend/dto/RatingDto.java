package com.hikehub.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public class RatingDto {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("hikeId")
    private String hikeId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("rating")
    private Double rating;
    
    @JsonProperty("comment")
    private String comment;
    
    @JsonProperty("upvotes")
    private Integer upvotes;
    
    @JsonProperty("upvotedBy")
    private List<String> upvotedBy;
    
    @JsonProperty("images")
    private List<String> images;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    // Constructors
    public RatingDto() {
    }
    
    public RatingDto(String id, String hikeId, String userId, Double rating, String comment,
                    Integer upvotes, List<String> upvotedBy, List<String> images, String createdAt) {
        this.id = id;
        this.hikeId = hikeId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.upvotes = upvotes;
        this.upvotedBy = upvotedBy;
        this.images = images;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHikeId() {
        return hikeId;
    }
    
    public void setHikeId(String hikeId) {
        this.hikeId = hikeId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
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
    
    public Integer getUpvotes() {
        return upvotes;
    }
    
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }
    
    public List<String> getUpvotedBy() {
        return upvotedBy;
    }
    
    public void setUpvotedBy(List<String> upvotedBy) {
        this.upvotedBy = upvotedBy;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

