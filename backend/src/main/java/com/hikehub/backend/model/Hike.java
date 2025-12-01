package com.hikehub.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "hikes")
public class Hike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hike_id")
    private Long hikeId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "location_text")
    private String locationText;
    
    private String difficulty;
    
    private Double distance;
    
    private Double elevation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    private Double latitude;
    
    private Double longitude;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Constructors
    public Hike() {
    }
    
    public Hike(String name, String locationText, String difficulty, Double distance, 
                Double elevation, User createdBy, Double latitude, Double longitude) {
        this.name = name;
        this.locationText = locationText;
        this.difficulty = difficulty;
        this.distance = distance;
        this.elevation = elevation;
        this.createdBy = createdBy;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public Long getHikeId() {
        return hikeId;
    }
    
    public void setHikeId(Long hikeId) {
        this.hikeId = hikeId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocationText() {
        return locationText;
    }
    
    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Double getElevation() {
        return elevation;
    }
    
    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

