package com.hikehub.backend.service;

import com.hikehub.backend.dto.CreateOrUpdateReviewRequest;
import com.hikehub.backend.dto.CreateOrUpdateRatingRequest;
import com.hikehub.backend.dto.HikeReviewsResponseDto;
import com.hikehub.backend.dto.RatingDto;
import com.hikehub.backend.dto.ReviewResponseDto;
import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.ReviewUpvote;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.HikeRepository;
import com.hikehub.backend.repository.ReviewRepository;
import com.hikehub.backend.repository.ReviewUpvoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final HikeRepository hikeRepository;
    private final ReviewUpvoteRepository reviewUpvoteRepository;
    
    @Autowired
    public ReviewService(ReviewRepository reviewRepository, 
                        HikeRepository hikeRepository,
                        ReviewUpvoteRepository reviewUpvoteRepository) {
        this.reviewRepository = reviewRepository;
        this.hikeRepository = hikeRepository;
        this.reviewUpvoteRepository = reviewUpvoteRepository;
    }
    
    public ReviewResponseDto createOrUpdateReview(Long hikeId, CreateOrUpdateReviewRequest request, User currentUser) {
        // Load the hike
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Hike not found with id: " + hikeId));
        
        // Find existing review by same user for this hike
        Optional<Review> existingReviewOpt = reviewRepository.findByHikeAndUser(hike, currentUser);
        
        Review review;
        if (existingReviewOpt.isPresent()) {
            // Update existing review
            review = existingReviewOpt.get();
            review.setRating(request.getRating());
            review.setReviewBody(request.getReviewBody());
            // updatedAt is handled by @PreUpdate
        } else {
            // Create new review
            review = new Review();
            review.setHike(hike);
            review.setUser(currentUser);
            review.setRating(request.getRating());
            review.setReviewBody(request.getReviewBody());
            review.setUpvotesCount(0);
        }
        
        review = reviewRepository.save(review);
        
        // Convert to DTO
        return toReviewResponseDto(review, currentUser);
    }
    
    @Transactional(readOnly = true)
    public HikeReviewsResponseDto getReviewsForHike(Long hikeId, User currentUser) {
        // Load the hike
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Hike not found with id: " + hikeId));
        
        // Fetch all reviews sorted by upvotes_count desc, then created_at desc
        List<Review> reviews = reviewRepository.findByHikeOrderByUpvotesCountDescCreatedAtDesc(hike);
        
        // Compute average rating and count
        Double averageRating = reviewRepository.findAverageRatingByHike(hike);
        Long totalReviewsCount = reviewRepository.countByHike(hike);
        
        // Convert reviews to DTOs, checking if current user upvoted each
        List<ReviewResponseDto> reviewDtos = reviews.stream()
                .map(review -> toReviewResponseDto(review, currentUser))
                .collect(Collectors.toList());
        
        // If no reviews, averageRating will be null, set to 0.0
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        return new HikeReviewsResponseDto(
                hike.getHikeId(),
                averageRating,
                totalReviewsCount.intValue(),
                reviewDtos
        );
    }
    
    public ReviewResponseDto toggleUpvote(Long reviewId, User currentUser) {
        // Load the review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        // Check if upvote exists
        Optional<ReviewUpvote> existingUpvoteOpt = reviewUpvoteRepository.findByReviewAndUser(review, currentUser);
        
        if (existingUpvoteOpt.isPresent()) {
            // Remove upvote
            reviewUpvoteRepository.delete(existingUpvoteOpt.get());
            // Decrement count (but not below 0)
            review.setUpvotesCount(Math.max(0, review.getUpvotesCount() - 1));
        } else {
            // Create upvote
            ReviewUpvote upvote = new ReviewUpvote(review, currentUser);
            reviewUpvoteRepository.save(upvote);
            // Increment count
            review.setUpvotesCount(review.getUpvotesCount() + 1);
        }
        
        review = reviewRepository.save(review);
        
        // Return updated DTO
        return toReviewResponseDto(review, currentUser);
    }
    
    private ReviewResponseDto toReviewResponseDto(Review review, User currentUser) {
        // Check if current user has upvoted this review
        boolean currentUserUpvoted = false;
        if (currentUser != null) {
            currentUserUpvoted = reviewUpvoteRepository.findByReviewAndUser(review, currentUser).isPresent();
        }
        
        return new ReviewResponseDto(
                review.getReviewId(),
                review.getRating(),
                review.getReviewBody(),
                review.getUser().getUsername(),
                review.getUser().getUserId(),
                review.getUpvotesCount(),
                currentUserUpvoted,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
    
    // New methods for Rating API
    
    @Transactional(readOnly = true)
    public List<RatingDto> getRatingsForHike(Long hikeId) {
        // Load the hike
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Hike not found with id: " + hikeId));
        
        // Fetch all reviews sorted by upvotes_count desc, then created_at desc
        List<Review> reviews = reviewRepository.findByHikeOrderByUpvotesCountDescCreatedAtDesc(hike);
        
        // Convert reviews to RatingDtos
        return reviews.stream()
                .map(this::toRatingDto)
                .collect(Collectors.toList());
    }
    
    public RatingDto createOrUpdateRating(Long hikeId, CreateOrUpdateRatingRequest request, User currentUser) {
        // Load the hike
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Hike not found with id: " + hikeId));
        
        // Find existing review by same user for this hike
        Optional<Review> existingReviewOpt = reviewRepository.findByHikeAndUser(hike, currentUser);
        
        Review review;
        if (existingReviewOpt.isPresent()) {
            // Update existing review
            review = existingReviewOpt.get();
            review.setRating(request.getRating());
            review.setReviewBody(request.getComment());
            // updatedAt is handled by @PreUpdate
        } else {
            // Create new review
            review = new Review();
            review.setHike(hike);
            review.setUser(currentUser);
            review.setRating(request.getRating());
            review.setReviewBody(request.getComment());
            review.setUpvotesCount(0);
        }
        
        review = reviewRepository.save(review);
        
        // Convert to RatingDto
        return toRatingDto(review);
    }
    
    public RatingDto setUpvote(Long reviewId, Boolean upvoted, User currentUser) {
        // Load the review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        // Check if upvote exists
        Optional<ReviewUpvote> existingUpvoteOpt = reviewUpvoteRepository.findByReviewAndUser(review, currentUser);
        
        boolean currentlyUpvoted = existingUpvoteOpt.isPresent();
        
        if (upvoted && !currentlyUpvoted) {
            // Add upvote
            ReviewUpvote upvote = new ReviewUpvote(review, currentUser);
            reviewUpvoteRepository.save(upvote);
            review.setUpvotesCount(review.getUpvotesCount() + 1);
            review = reviewRepository.save(review);
        } else if (!upvoted && currentlyUpvoted) {
            // Remove upvote
            reviewUpvoteRepository.delete(existingUpvoteOpt.get());
            review.setUpvotesCount(Math.max(0, review.getUpvotesCount() - 1));
            review = reviewRepository.save(review);
        }
        // If upvoted == currentlyUpvoted, no change needed
        
        // Return updated DTO
        return toRatingDto(review);
    }
    
    private RatingDto toRatingDto(Review review) {
        // Get all upvoters for this review
        List<ReviewUpvote> upvotes = reviewUpvoteRepository.findByReview(review);
        List<String> upvotedBy = upvotes.stream()
                .map(upvote -> String.valueOf(upvote.getUser().getUserId()))
                .collect(Collectors.toList());
        
        // Convert createdAt to ISO string
        String createdAtStr = review.getCreatedAt() != null 
                ? review.getCreatedAt().toString() 
                : Instant.now().toString();
        
        return new RatingDto(
                String.valueOf(review.getReviewId()),
                String.valueOf(review.getHike().getHikeId()),
                String.valueOf(review.getUser().getUserId()),
                review.getRating(),
                review.getReviewBody(),
                review.getUpvotesCount(),
                upvotedBy,
                new ArrayList<>(), // images - empty for now
                createdAtStr
        );
    }
}

