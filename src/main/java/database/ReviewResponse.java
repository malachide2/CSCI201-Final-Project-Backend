package database;

public class ReviewResponse {
    private int id;
    private int hikeId;
    private int userId;
    private String username;
    private double rating;
    private String comment;
    private int upvotes;
    private String createdAt;
    private boolean upvotedByCurrentUser;

    public ReviewResponse() {
    }

    public ReviewResponse(int id, int hikeId, int userId, String username, double rating, 
                         String comment, int upvotes, String createdAt, boolean upvotedByCurrentUser) {
        this.id = id;
        this.hikeId = hikeId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.upvotes = upvotes;
        this.createdAt = createdAt;
        this.upvotedByCurrentUser = upvotedByCurrentUser;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHikeId() {
        return hikeId;
    }

    public void setHikeId(int hikeId) {
        this.hikeId = hikeId;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isUpvotedByCurrentUser() {
        return upvotedByCurrentUser;
    }

    public void setUpvotedByCurrentUser(boolean upvotedByCurrentUser) {
        this.upvotedByCurrentUser = upvotedByCurrentUser;
    }
}

