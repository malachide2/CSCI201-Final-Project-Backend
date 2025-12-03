package com.hikehub.backend.dto;

import java.util.List;

public class FriendsListResponseDto {
    
    private Long userId;
    private Integer totalFriends;
    private Integer totalFollowers;
    private List<FriendDto> friends;
    
    public FriendsListResponseDto() {
    }
    
    public FriendsListResponseDto(Long userId, Integer totalFriends, Integer totalFollowers, 
                                  List<FriendDto> friends) {
        this.userId = userId;
        this.totalFriends = totalFriends;
        this.totalFollowers = totalFollowers;
        this.friends = friends;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getTotalFriends() {
        return totalFriends;
    }
    
    public void setTotalFriends(Integer totalFriends) {
        this.totalFriends = totalFriends;
    }
    
    public Integer getTotalFollowers() {
        return totalFollowers;
    }
    
    public void setTotalFollowers(Integer totalFollowers) {
        this.totalFollowers = totalFollowers;
    }
    
    public List<FriendDto> getFriends() {
        return friends;
    }
    
    public void setFriends(List<FriendDto> friends) {
        this.friends = friends;
    }
}