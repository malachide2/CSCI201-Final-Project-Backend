package database;

public class FriendResponse {
    private int userId;
    private String username;
    private String email;
    private String friendsSince;

    public FriendResponse() {
    }

    public FriendResponse(int userId, String username, String email, String friendsSince) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friendsSince = friendsSince;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public String getFriendsSince() {
        return friendsSince;
    }

    public void setFriendsSince(String friendsSince) {
        this.friendsSince = friendsSince;
    }
}