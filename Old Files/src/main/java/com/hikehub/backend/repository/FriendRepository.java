package com.hikehub.backend.repository;

import com.hikehub.backend.model.Friend;
import com.hikehub.backend.model.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {
    
    List<Friend> findByFollowerId(Long followerId);
    
    List<Friend> findByFollowedId(Long followedId);
    
    Optional<Friend> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.followerId = :userId")
    Long countFollowing(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.followedId = :userId")
    Long countFollowers(@Param("userId") Long userId);
}