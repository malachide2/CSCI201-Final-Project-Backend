package database;

public class ActivityItemResponse {
    private String type;
    private int id;
    private int hikeId;
    private String hikeName;
    private double rating;
    private String comment;
    private String createdAt;
    private String username;

    public ActivityItemResponse() {
    }

    public ActivityItemResponse(String type, int id, int hikeId, String hikeName, 
                                 double rating, String comment, String createdAt, String username) {
        this.type = type;
        this.id = id;
        this.hikeId = hikeId;
        this.hikeName = hikeName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getHikeName() {
        return hikeName;
    }

    public void setHikeName(String hikeName) {
        this.hikeName = hikeName;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}