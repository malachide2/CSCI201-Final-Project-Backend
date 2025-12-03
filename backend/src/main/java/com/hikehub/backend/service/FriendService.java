package com.hikehub.backend.service;

import com.hikehub.backend.dto.FriendActivityDto;
import com.hikehub.backend.dto.FriendActivityDto.ActivityItemDto;
import com.hikehub.backend.dto.FriendDto;
import com.hikehub.backend.dto.FriendsListResponseDto;
import com.hikehub.backend.model.Friend;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.FriendRepository;
import com.hikehub.backend.repository.ReviewRepository;
import com.hikehub.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendService {
    
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    @Autowired
    public FriendService(FriendRepository friendRepository, 
                        UserRepository userRepository,
                        ReviewRepository reviewRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }
    
    public FriendDto addFriend(String username, User currentUser) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        if (targetUser.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Cannot add yourself as a friend");
        }
        
        if (friendRepository.existsByFollowerIdAndFollowedId(currentUser.getUserId(), targetUser.getUserId())) {
            throw new RuntimeException("Already following this user");
        }
        
        Friend friend = new Friend(currentUser.getUserId(), targetUser.getUserId());
        friend = friendRepository.save(friend);
        
        return new FriendDto(
                targetUser.getUserId(),
                targetUser.getUsername(),
                targetUser.getEmail(),
                friend.getCreatedAt()
        );
    }
    
    public void removeFriend(Long friendUserId, User currentUser) {
        if (!friendRepository.existsByFollowerIdAndFollowedId(currentUser.getUserId(), friendUserId)) {
            throw new RuntimeException("Friendship not found");
        }
        
        friendRepository.deleteByFollowerIdAndFollowedId(currentUser.getUserId(), friendUserId);
    }
    
    @Transactional(readOnly = true)
    public FriendsListResponseDto getFriendsList(User currentUser) {
        List<Friend> friendships = friendRepository.findByFollowerId(currentUser.getUserId());
        
        List<FriendDto> friendDtos = friendships.stream()
                .map(friendship -> {
                    User friendUser = userRepository.findById(friendship.getFollowedId())
                            .orElse(null);
                    if (friendUser == null) return null;
                    return new FriendDto(
                            friendUser.getUserId(),
                            friendUser.getUsername(),
                            friendUser.getEmail(),
                            friendship.getCreatedAt()
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        
        Long followingCount = friendRepository.countFollowing(currentUser.getUserId());
        Long followersCount = friendRepository.countFollowers(currentUser.getUserId());
        
        return new FriendsListResponseDto(
                currentUser.getUserId(),
                followingCount.intValue(),
                followersCount.intValue(),
                friendDtos
        );
    }
    
    @Transactional(readOnly = true)
    public FriendActivityDto getFriendActivity(Long friendUserId, User currentUser) {
        if (!friendRepository.existsByFollowerIdAndFollowedId(currentUser.getUserId(), friendUserId)) {
            throw new RuntimeException("You are not following this user");
        }
        
        User friendUser = userRepository.findById(friendUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Review> reviews = reviewRepository.findByUserOrderByCreatedAtDesc(friendUser);
        
        List<ActivityItemDto> activities = reviews.stream()
                .limit(20)
                .map(review -> new ActivityItemDto(
                        "review",
                        review.getHike().getHikeId(),
                        review.getHike().getName(),
                        review.getRating(),
                        review.getReviewBody(),
                        null,
                        review.getCreatedAt()
                ))
                .collect(Collectors.toList());
        
        return new FriendActivityDto(
                friendUser.getUserId(),
                friendUser.getUsername(),
                activities
        );
    }
    
    @Transactional(readOnly = true)
    public boolean isFollowing(Long targetUserId, User currentUser) {
        return friendRepository.existsByFollowerIdAndFollowedId(currentUser.getUserId(), targetUserId);
    }
    
    @Transactional(readOnly = true)
    public List<FriendDto> getFollowers(User currentUser) {
        List<Friend> followers = friendRepository.findByFollowedId(currentUser.getUserId());
        
        return followers.stream()
                .map(friendship -> {
                    User followerUser = userRepository.findById(friendship.getFollowerId())
                            .orElse(null);
                    if (followerUser == null) return null;
                    return new FriendDto(
                            followerUser.getUserId(),
                            followerUser.getUsername(),
                            followerUser.getEmail(),
                            friendship.getCreatedAt()
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
}