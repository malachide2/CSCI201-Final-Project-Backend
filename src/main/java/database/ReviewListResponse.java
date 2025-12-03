package database;

import java.util.List;

public class ReviewListResponse {
    private int hikeId;
    private double averageRating;
    private int totalReviews;
    private List<ReviewResponse> reviews;

    public ReviewListResponse() {
    }

    public ReviewListResponse(int hikeId, double averageRating, int totalReviews, List<ReviewResponse> reviews) {
        this.hikeId = hikeId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
    }

    // Getters and setters
    public int getHikeId() {
        return hikeId;
    }

    public void setHikeId(int hikeId) {
        this.hikeId = hikeId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviews = reviews;
    }
}

