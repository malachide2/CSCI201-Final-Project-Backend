package database;

import com.google.gson.annotations.SerializedName;

public class Hike {
    private int hike_id;
    private String name;
    private String location_text;
    private double distance;
    private double difficulty;
    private double average_rating;
    private int total_ratings;
    private String thumbnail_url;
    @SerializedName("created_by")
    private Integer created_by;

    // Constructor matching the ResultSet columns
    public Hike(int hike_id, String name, String location_text, double distance, 
                double difficulty, double average_rating, int total_ratings, String thumbnail_url, Integer created_by) {
        this.hike_id = hike_id;
        this.name = name;
        this.location_text = location_text;
        this.distance = distance;
        this.difficulty = difficulty;
        this.average_rating = average_rating;
        this.total_ratings = total_ratings;
        this.thumbnail_url = thumbnail_url;
        this.created_by = created_by;
    }
    
    public int getHike_id() {
        return hike_id;
    }

    public String getName() {
        return name;
    }

    public String getLocation_text() {
        return location_text;
    }

    public double getDistance() {
        return distance;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public double getAverage_rating() {
        return average_rating;
    }

    public int getTotal_ratings() {
        return total_ratings;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public Integer getCreated_by() {
        return created_by;
    }
}