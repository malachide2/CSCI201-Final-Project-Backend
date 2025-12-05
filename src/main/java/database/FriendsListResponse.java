package database;

import java.util.List;

public class FriendsListResponse {
    private int userId;
    private int totalFriends;
    private int totalFollowers;
    private List<FriendResponse> friends;

    public FriendsListResponse() {
    }

    public FriendsListResponse(int userId, int totalFriends, int totalFollowers, List<FriendResponse> friends) {
        this.userId = userId;
        this.totalFriends = totalFriends;
        this.totalFollowers = totalFollowers;
        this.friends = friends;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalFriends() {
        return totalFriends;
    }

    public void setTotalFriends(int totalFriends) {
        this.totalFriends = totalFriends;
    }

    public int getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(int totalFollowers) {
        this.totalFollowers = totalFollowers;
    }

    public List<FriendResponse> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendResponse> friends) {
        this.friends = friends;
    }
}