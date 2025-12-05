package database;

import java.util.List;

public class FriendActivityResponse {
    private int friendUserId;
    private String friendUsername;
    private int totalActivities;
    private List<ActivityItemResponse> activities;

    public FriendActivityResponse() {
    }

    public FriendActivityResponse(int friendUserId, String friendUsername, 
                                   int totalActivities, List<ActivityItemResponse> activities) {
        this.friendUserId = friendUserId;
        this.friendUsername = friendUsername;
        this.totalActivities = totalActivities;
        this.activities = activities;
    }

    public int getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(int friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public List<ActivityItemResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityItemResponse> activities) {
        this.activities = activities;
    }
}