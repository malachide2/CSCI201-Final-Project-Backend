package com.hikehub.backend.dto;

import java.time.Instant;

public class FriendDto {
    
    private Long userId;
    private String username;
    private String email;
    private Instant friendsSince;
    
    public FriendDto() {
    }
    
    public FriendDto(Long userId, String username, String email, Instant friendsSince) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friendsSince = friendsSince;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Instant getFriendsSince() {
        return friendsSince;
    }
    
    public void setFriendsSince(Instant friendsSince) {
        this.friendsSince = friendsSince;
    }
}