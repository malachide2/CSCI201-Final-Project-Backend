package com.hikehub.backend.controller;

import com.hikehub.backend.dto.CreateOrUpdateReviewRequest;
import com.hikehub.backend.dto.CreateOrUpdateRatingRequest;
import com.hikehub.backend.dto.HikeReviewsResponseDto;
import com.hikehub.backend.dto.RatingDto;
import com.hikehub.backend.dto.ReviewResponseDto;
import com.hikehub.backend.dto.UpvoteRequest;
import com.hikehub.backend.model.User;
import com.hikehub.backend.service.CurrentUserService;
import com.hikehub.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {
    
    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;
    
    @Autowired
    public ReviewController(ReviewService reviewService, CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.currentUserService = currentUserService;
    }
    
    @PostMapping("/hikes/{hikeId}/reviews")
    public ResponseEntity<ReviewResponseDto> createOrUpdateReview(
            @PathVariable Long hikeId,
            @Valid @RequestBody CreateOrUpdateReviewRequest request) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            ReviewResponseDto response = reviewService.createOrUpdateReview(hikeId, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hikes/{hikeId}/reviews")
    public ResponseEntity<HikeReviewsResponseDto> getReviewsForHike(@PathVariable Long hikeId) {
        try {
            // Try to get current user, but if not available, pass null (public endpoint)
            User currentUser = null;
            try {
                currentUser = currentUserService.getCurrentUser();
            } catch (RuntimeException e) {
                // No current user, that's fine for this public endpoint
            }
            
            HikeReviewsResponseDto response = reviewService.getReviewsForHike(hikeId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/reviews/{reviewId}/upvote")
    public ResponseEntity<ReviewResponseDto> toggleUpvote(@PathVariable Long reviewId) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            ReviewResponseDto response = reviewService.toggleUpvote(reviewId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // New Rating API endpoints for frontend compatibility
    
    @GetMapping("/hikes/{hikeId}/ratings")
    public ResponseEntity<List<RatingDto>> getRatingsForHike(@PathVariable Long hikeId) {
        try {
            List<RatingDto> ratings = reviewService.getRatingsForHike(hikeId);
            return ResponseEntity.ok(ratings);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/hikes/{hikeId}/ratings")
    public ResponseEntity<RatingDto> createOrUpdateRating(
            @PathVariable Long hikeId,
            @Valid @RequestBody CreateOrUpdateRatingRequest request) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            RatingDto response = reviewService.createOrUpdateRating(hikeId, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/ratings/{reviewId}/upvote")
    public ResponseEntity<RatingDto> setUpvote(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpvoteRequest request) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            RatingDto response = reviewService.setUpvote(reviewId, request.getUpvoted(), currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

