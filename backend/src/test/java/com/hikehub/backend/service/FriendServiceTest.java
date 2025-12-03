package com.hikehub.backend.service;

import com.hikehub.backend.dto.FriendActivityDto;
import com.hikehub.backend.dto.FriendDto;
import com.hikehub.backend.dto.FriendsListResponseDto;
import com.hikehub.backend.model.Friend;
import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.FriendRepository;
import com.hikehub.backend.repository.HikeRepository;
import com.hikehub.backend.repository.ReviewRepository;
import com.hikehub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FriendServiceTest {
    
    @Autowired
    private FriendService friendService;
    
    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HikeRepository hikeRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    private User user1;
    private User user2;
    private User user3;
    
    @BeforeEach
    void setUp() {
        user1 = new User("user1@test.com", "user1", "hash1");
        user2 = new User("user2@test.com", "user2", "hash2");
        user3 = new User("user3@test.com", "user3", "hash3");
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);
    }
    
    @Test
    void testAddFriendValid() {
        FriendDto result = friendService.addFriend("user2", user1);
        
        assertNotNull(result);
        assertEquals(user2.getUserId(), result.getUserId());
        assertEquals("user2", result.getUsername());
        assertNotNull(result.getFriendsSince());
        
        assertTrue(friendRepository.existsByFollowerIdAndFollowedId(user1.getUserId(), user2.getUserId()));
    }
    
    @Test
    void testAddFriendNonexistentUser() {
        assertThrows(RuntimeException.class, () -> {
            friendService.addFriend("nonexistent", user1);
        });
    }
    
    @Test
    void testAddFriendSelf() {
        assertThrows(RuntimeException.class, () -> {
            friendService.addFriend("user1", user1);
        });
    }
    
    @Test
    void testAddFriendDuplicate() {
        friendService.addFriend("user2", user1);
        
        assertThrows(RuntimeException.class, () -> {
            friendService.addFriend("user2", user1);
        });
    }
    
    @Test
    void testRemoveFriend() {
        friendService.addFriend("user2", user1);
        assertTrue(friendRepository.existsByFollowerIdAndFollowedId(user1.getUserId(), user2.getUserId()));
        
        friendService.removeFriend(user2.getUserId(), user1);
        
        assertFalse(friendRepository.existsByFollowerIdAndFollowedId(user1.getUserId(), user2.getUserId()));
    }
    
    @Test
    void testRemoveFriendNotExists() {
        assertThrows(RuntimeException.class, () -> {
            friendService.removeFriend(user2.getUserId(), user1);
        });
    }
    
    @Test
    void testGetFriendsList() {
        friendService.addFriend("user2", user1);
        friendService.addFriend("user3", user1);
        
        FriendsListResponseDto result = friendService.getFriendsList(user1);
        
        assertNotNull(result);
        assertEquals(user1.getUserId(), result.getUserId());
        assertEquals(2, result.getTotalFriends());
        assertEquals(2, result.getFriends().size());
    }
    
    @Test
    void testGetFriendsListEmpty() {
        FriendsListResponseDto result = friendService.getFriendsList(user1);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalFriends());
        assertTrue(result.getFriends().isEmpty());
    }
    
    @Test
    void testGetFollowers() {
        friendService.addFriend("user1", user2);
        friendService.addFriend("user1", user3);
        
        List<FriendDto> followers = friendService.getFollowers(user1);
        
        assertEquals(2, followers.size());
    }
    
    @Test
    void testIsFollowing() {
        assertFalse(friendService.isFollowing(user2.getUserId(), user1));
        
        friendService.addFriend("user2", user1);
        
        assertTrue(friendService.isFollowing(user2.getUserId(), user1));
    }
    
    @Test
    void testGetFriendActivity() {
        friendService.addFriend("user2", user1);
        
        Hike hike = new Hike("Test Hike", "Test Location", "Easy", 5.0, 100.0, user2, 40.0, -120.0);
        hike = hikeRepository.save(hike);
        
        Review review = new Review(hike, user2, 4.5, "Great hike!");
        reviewRepository.save(review);
        
        FriendActivityDto activity = friendService.getFriendActivity(user2.getUserId(), user1);
        
        assertNotNull(activity);
        assertEquals(user2.getUserId(), activity.getUserId());
        assertEquals("user2", activity.getUsername());
        assertFalse(activity.getRecentActivity().isEmpty());
        assertEquals("review", activity.getRecentActivity().get(0).getType());
    }
    
    @Test
    void testGetFriendActivityNotFollowing() {
        assertThrows(RuntimeException.class, () -> {
            friendService.getFriendActivity(user2.getUserId(), user1);
        });
    }
    
    @Test
    void testFriendshipIsOneWay() {
        friendService.addFriend("user2", user1);
        
        assertTrue(friendService.isFollowing(user2.getUserId(), user1));
        assertFalse(friendService.isFollowing(user1.getUserId(), user2));
    }
}