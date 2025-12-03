package com.hikehub.backend.service;

import com.hikehub.backend.dto.CreateOrUpdateReviewRequest;
import com.hikehub.backend.dto.HikeReviewsResponseDto;
import com.hikehub.backend.dto.ReviewResponseDto;
import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.HikeRepository;
import com.hikehub.backend.repository.ReviewRepository;
import com.hikehub.backend.repository.ReviewUpvoteRepository;
import com.hikehub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private HikeRepository hikeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewUpvoteRepository reviewUpvoteRepository;
    
    private User user1;
    private User user2;
    private Hike hike1;
    
    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User("user1@test.com", "user1", "hash1");
        user2 = new User("user2@test.com", "user2", "hash2");
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        
        // Create test hike
        hike1 = new Hike("Test Hike", "Test Location", "Easy", 5.0, 100.0, user1, 40.0, -120.0);
        hike1 = hikeRepository.save(hike1);
    }
    
    @Test
    void testCreateNewReview() {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Great hike!");
        
        ReviewResponseDto response = reviewService.createOrUpdateReview(hike1.getHikeId(), request, user1);
        
        assertNotNull(response);
        assertEquals(4.5, response.getRating());
        assertEquals("Great hike!", response.getReviewBody());
        assertEquals(user1.getUsername(), response.getAuthorUsername());
        assertEquals(0, response.getUpvotesCount());
        assertFalse(response.getCurrentUserUpvoted());
        
        // Verify it was saved
        Review saved = reviewRepository.findById(response.getReviewId()).orElse(null);
        assertNotNull(saved);
        assertEquals(4.5, saved.getRating());
    }
    
    @Test
    void testUpdateExistingReview() {
        // Create initial review
        CreateOrUpdateReviewRequest initialRequest = new CreateOrUpdateReviewRequest(3.0, "Initial review");
        ReviewResponseDto initialResponse = reviewService.createOrUpdateReview(hike1.getHikeId(), initialRequest, user1);
        
        // Update the review
        CreateOrUpdateReviewRequest updateRequest = new CreateOrUpdateReviewRequest(5.0, "Updated review");
        ReviewResponseDto updatedResponse = reviewService.createOrUpdateReview(hike1.getHikeId(), updateRequest, user1);
        
        assertEquals(initialResponse.getReviewId(), updatedResponse.getReviewId());
        assertEquals(5.0, updatedResponse.getRating());
        assertEquals("Updated review", updatedResponse.getReviewBody());
        
        // Verify only one review exists
        long reviewCount = reviewRepository.countByHike(hike1);
        assertEquals(1, reviewCount);
    }
    
    @Test
    void testGetReviewsForHike() {
        // Create reviews from two different users
        CreateOrUpdateReviewRequest request1 = new CreateOrUpdateReviewRequest(4.0, "Review 1");
        CreateOrUpdateReviewRequest request2 = new CreateOrUpdateReviewRequest(5.0, "Review 2");
        
        reviewService.createOrUpdateReview(hike1.getHikeId(), request1, user1);
        reviewService.createOrUpdateReview(hike1.getHikeId(), request2, user2);
        
        HikeReviewsResponseDto response = reviewService.getReviewsForHike(hike1.getHikeId(), user1);
        
        assertNotNull(response);
        assertEquals(hike1.getHikeId(), response.getHikeId());
        assertEquals(2, response.getTotalReviews());
        assertEquals(4.5, response.getAverageRating(), 0.01); // (4.0 + 5.0) / 2 = 4.5
        assertEquals(2, response.getReviews().size());
    }
    
    @Test
    void testGetReviewsForHikeSortedByUpvotes() {
        // Create reviews
        CreateOrUpdateReviewRequest request1 = new CreateOrUpdateReviewRequest(4.0, "Review 1");
        CreateOrUpdateReviewRequest request2 = new CreateOrUpdateReviewRequest(5.0, "Review 2");
        
        ReviewResponseDto review1 = reviewService.createOrUpdateReview(hike1.getHikeId(), request1, user1);
        ReviewResponseDto review2 = reviewService.createOrUpdateReview(hike1.getHikeId(), request2, user2);
        
        // Upvote review2 (should have more upvotes)
        reviewService.toggleUpvote(review2.getReviewId(), user1);
        
        HikeReviewsResponseDto response = reviewService.getReviewsForHike(hike1.getHikeId(), user1);
        
        // Review2 should come first (more upvotes)
        assertEquals(review2.getReviewId(), response.getReviews().get(0).getReviewId());
        assertEquals(1, response.getReviews().get(0).getUpvotesCount());
    }
    
    @Test
    void testToggleUpvoteCreate() {
        // Create a review
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test review");
        ReviewResponseDto review = reviewService.createOrUpdateReview(hike1.getHikeId(), request, user1);
        
        // User2 upvotes the review
        ReviewResponseDto upvoted = reviewService.toggleUpvote(review.getReviewId(), user2);
        
        assertEquals(1, upvoted.getUpvotesCount());
        assertTrue(upvoted.getCurrentUserUpvoted());
        
        // Verify upvote was created
        Review savedReview = reviewRepository.findById(review.getReviewId()).orElse(null);
        assertNotNull(savedReview);
        assertEquals(1, savedReview.getUpvotesCount());
    }
    
    @Test
    void testToggleUpvoteRemove() {
        // Create a review
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test review");
        ReviewResponseDto review = reviewService.createOrUpdateReview(hike1.getHikeId(), request, user1);
        
        // User2 upvotes
        reviewService.toggleUpvote(review.getReviewId(), user2);
        
        // User2 removes upvote
        ReviewResponseDto unupvoted = reviewService.toggleUpvote(review.getReviewId(), user2);
        
        assertEquals(0, unupvoted.getUpvotesCount());
        assertFalse(unupvoted.getCurrentUserUpvoted());
        
        // Verify upvote was removed
        Review savedReview = reviewRepository.findById(review.getReviewId()).orElse(null);
        assertNotNull(savedReview);
        assertEquals(0, savedReview.getUpvotesCount());
    }
    
    @Test
    void testGetReviewsForHikeWithNoReviews() {
        HikeReviewsResponseDto response = reviewService.getReviewsForHike(hike1.getHikeId(), user1);
        
        assertNotNull(response);
        assertEquals(0, response.getTotalReviews());
        assertEquals(0.0, response.getAverageRating());
        assertTrue(response.getReviews().isEmpty());
    }
    
    @Test
    void testCreateOrUpdateReviewHikeNotFound() {
        CreateOrUpdateReviewRequest request = new CreateOrUpdateReviewRequest(4.5, "Test");
        
        assertThrows(RuntimeException.class, () -> {
            reviewService.createOrUpdateReview(999L, request, user1);
        });
    }
    
    @Test
    void testToggleUpvoteReviewNotFound() {
        assertThrows(RuntimeException.class, () -> {
            reviewService.toggleUpvote(999L, user1);
        });
    }
}

