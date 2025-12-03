package com.hikehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hikehub.backend.dto.CreateOrUpdateReviewRequest;
import com.hikehub.backend.dto.HikeReviewsResponseDto;
import com.hikehub.backend.dto.ReviewResponseDto;
import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.HikeRepository;
import com.hikehub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HikeRepository hikeRepository;
    
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private User testUser;
    private Hike testHike;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Clear all data to ensure clean state
        hikeRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user (CurrentUserService will use this if ID 1 doesn't exist)
        testUser = new User("test@test.com", "testuser", "hash");
        testUser = userRepository.save(testUser);
        
        // Create test hike
        testHike = new Hike("Test Hike", "Test Location", "Easy", 5.0, 100.0, testUser, 40.0, -120.0);
        testHike = hikeRepository.save(testHike);
    }
    
    @Test
    void testCreateReview() throws Exception {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Great hike!");
        
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.reviewBody").value("Great hike!"))
                .andExpect(jsonPath("$.upvotesCount").value(0));
    }
    
    @Test
    void testCreateReviewInvalidRatingTooLow() throws Exception {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(0.5, "Invalid rating");
        
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateReviewInvalidRatingTooHigh() throws Exception {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(5.5, "Invalid rating");
        
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateReviewInvalidRatingNotHalfIncrement() throws Exception {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.3, "Invalid increment");
        
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateReviewValidHalfIncrements() throws Exception {
        // Test various valid increments
        double[] validRatings = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
        
        for (double rating : validRatings) {
            CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(rating, "Rating: " + rating);
            
            mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(rating));
        }
    }
    
    @Test
    void testUpdateReview() throws Exception {
        // Create initial review
        CreateOrUpdateReviewRequest initialRequest = new CreateOrUpdateReviewRequest(3.0, "Initial");
        String initialResponse = mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        ReviewResponseDto initialDto = objectMapper.readValue(initialResponse, ReviewResponseDto.class);
        
        // Update the review
        CreateOrUpdateReviewRequest updateRequest = new CreateOrUpdateReviewRequest(5.0, "Updated");
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(initialDto.getReviewId()))
                .andExpect(jsonPath("$.rating").value(5.0))
                .andExpect(jsonPath("$.reviewBody").value("Updated"));
    }
    
    @Test
    void testGetReviewsForHike() throws Exception {
        // Create a review first
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test review");
        mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // Get reviews
        mockMvc.perform(get("/api/hikes/{hikeId}/reviews", testHike.getHikeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hikeId").value(testHike.getHikeId()))
                .andExpect(jsonPath("$.totalReviews").value(1))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews[0].rating").value(4.5));
    }
    
    @Test
    void testGetReviewsForHikeNotFound() throws Exception {
        mockMvc.perform(get("/api/hikes/999/reviews"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testToggleUpvote() throws Exception {
        // Create a review
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test review");
        String reviewResponse = mockMvc.perform(post("/api/hikes/{hikeId}/reviews", testHike.getHikeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        ReviewResponseDto reviewDto = objectMapper.readValue(reviewResponse, ReviewResponseDto.class);
        
        // Toggle upvote (create)
        mockMvc.perform(post("/api/reviews/{reviewId}/upvote", reviewDto.getReviewId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotesCount").value(1))
                .andExpect(jsonPath("$.currentUserUpvoted").value(true));
        
        // Toggle upvote again (remove)
        mockMvc.perform(post("/api/reviews/{reviewId}/upvote", reviewDto.getReviewId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotesCount").value(0))
                .andExpect(jsonPath("$.currentUserUpvoted").value(false));
    }
    
    @Test
    void testToggleUpvoteReviewNotFound() throws Exception {
        mockMvc.perform(post("/api/reviews/999/upvote"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateReviewHikeNotFound() throws Exception {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test");
        
        mockMvc.perform(post("/api/hikes/999/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}

