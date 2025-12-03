package com.hikehub.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AddFriendRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    public AddFriendRequest() {
    }
    
    public AddFriendRequest(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}