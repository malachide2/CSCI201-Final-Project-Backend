package com.hikehub.backend.dto;

import java.time.Instant;
import java.util.List;

public class FriendActivityDto {
    
    private Long userId;
    private String username;
    private List<ActivityItemDto> recentActivity;
    
    public FriendActivityDto() {
    }
    
    public FriendActivityDto(Long userId, String username, List<ActivityItemDto> recentActivity) {
        this.userId = userId;
        this.username = username;
        this.recentActivity = recentActivity;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<ActivityItemDto> getRecentActivity() {
        return recentActivity;
    }
    
    public void setRecentActivity(List<ActivityItemDto> recentActivity) {
        this.recentActivity = recentActivity;
    }
    
    public static class ActivityItemDto {
        
        private String type;
        private Long hikeId;
        private String hikeName;
        private Double rating;
        private String comment;
        private String imageUrl;
        private Instant createdAt;
        
        public ActivityItemDto() {
        }
        
        public ActivityItemDto(String type, Long hikeId, String hikeName, Double rating, 
                              String comment, String imageUrl, Instant createdAt) {
            this.type = type;
            this.hikeId = hikeId;
            this.hikeName = hikeName;
            this.rating = rating;
            this.comment = comment;
            this.imageUrl = imageUrl;
            this.createdAt = createdAt;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Long getHikeId() {
            return hikeId;
        }
        
        public void setHikeId(Long hikeId) {
            this.hikeId = hikeId;
        }
        
        public String getHikeName() {
            return hikeName;
        }
        
        public void setHikeName(String hikeName) {
            this.hikeName = hikeName;
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
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public Instant getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}