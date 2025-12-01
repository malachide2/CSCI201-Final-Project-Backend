package com.hikehub.backend.repository;

import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.ReviewUpvote;
import com.hikehub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewUpvoteRepository extends JpaRepository<ReviewUpvote, Long> {
    Optional<ReviewUpvote> findByReviewAndUser(Review review, User user);
    long countByReview(Review review);
}

