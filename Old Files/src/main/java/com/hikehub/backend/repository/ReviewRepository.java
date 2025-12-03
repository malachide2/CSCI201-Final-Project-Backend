package com.hikehub.backend.repository;

import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByHikeOrderByUpvotesCountDescCreatedAtDesc(Hike hike);
    
    Optional<Review> findByHikeAndUser(Hike hike, User user);
    
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hike = :hike")
    Double findAverageRatingByHike(@Param("hike") Hike hike);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.hike = :hike")
    Long countByHike(@Param("hike") Hike hike);
}