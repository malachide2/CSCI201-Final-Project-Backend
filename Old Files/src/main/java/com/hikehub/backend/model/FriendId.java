package com.hikehub.backend.model;

import java.io.Serializable;
import java.util.Objects;

public class FriendId implements Serializable {
    
    private Long followerId;
    private Long followedId;
    
    public FriendId() {
    }
    
    public FriendId(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }
    
    public Long getFollowerId() {
        return followerId;
    }
    
    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }
    
    public Long getFollowedId() {
        return followedId;
    }
    
    public void setFollowedId(Long followedId) {
        this.followedId = followedId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendId friendId = (FriendId) o;
        return Objects.equals(followerId, friendId.followerId) &&
               Objects.equals(followedId, friendId.followedId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(followerId, followedId);
    }
}