package com.hikehub.backend.dto;

import java.util.List;

public class HikeReviewsResponseDto {
    private Long hikeId;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponseDto> reviews;
    
    // Constructors
    public HikeReviewsResponseDto() {
    }
    
    public HikeReviewsResponseDto(Long hikeId, Double averageRating, Integer totalReviews, 
                                  List<ReviewResponseDto> reviews) {
        this.hikeId = hikeId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
    }
    
    // Getters and Setters
    public Long getHikeId() {
        return hikeId;
    }
    
    public void setHikeId(Long hikeId) {
        this.hikeId = hikeId;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getTotalReviews() {
        return totalReviews;
    }
    
    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }
    
    public List<ReviewResponseDto> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<ReviewResponseDto> reviews) {
        this.reviews = reviews;
    }
}

