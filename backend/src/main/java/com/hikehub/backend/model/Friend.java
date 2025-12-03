package com.hikehub.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friends")
@IdClass(FriendId.class)
public class Friend {
    
    @Id
    @Column(name = "follower_id")
    private Long followerId;
    
    @Id
    @Column(name = "followed_id")
    private Long followedId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", insertable = false, updatable = false)
    private User followed;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    public Friend() {
    }
    
    public Friend(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }
    
    public Friend(User follower, User followed) {
        this.followerId = follower.getUserId();
        this.followedId = followed.getUserId();
        this.follower = follower;
        this.followed = followed;
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
    
    public User getFollower() {
        return follower;
    }
    
    public void setFollower(User follower) {
        this.follower = follower;
    }
    
    public User getFollowed() {
        return followed;
    }
    
    public void setFollowed(User followed) {
        this.followed = followed;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}