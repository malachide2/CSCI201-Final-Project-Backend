package com.hikehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hikehub.backend.dto.AddFriendRequest;
import com.hikehub.backend.model.Hike;
import com.hikehub.backend.model.Review;
import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.FriendRepository;
import com.hikehub.backend.repository.HikeRepository;
import com.hikehub.backend.repository.ReviewRepository;
import com.hikehub.backend.repository.UserRepository;
import com.hikehub.backend.service.FriendService;
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
class FriendControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private HikeRepository hikeRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private FriendService friendService;
    
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private User testUser;
    private User friendUser;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        friendRepository.deleteAll();
        hikeRepository.deleteAll();
        userRepository.deleteAll();
        
        testUser = new User("test@test.com", "testuser", "hash");
        testUser = userRepository.save(testUser);
        
        friendUser = new User("friend@test.com", "frienduser", "hash");
        friendUser = userRepository.save(friendUser);
    }
    
    @Test
    void testAddFriend() throws Exception {
        AddFriendRequest request = new AddFriendRequest("frienduser");
        
        mockMvc.perform(post("/api/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(friendUser.getUserId()))
                .andExpect(jsonPath("$.username").value("frienduser"));
    }
    
    @Test
    void testAddFriendUserNotFound() throws Exception {
        AddFriendRequest request = new AddFriendRequest("nonexistent");
        
        mockMvc.perform(post("/api/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testAddFriendDuplicate() throws Exception {
        AddFriendRequest request = new AddFriendRequest("frienduser");
        
        mockMvc.perform(post("/api/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/api/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRemoveFriend() throws Exception {
        friendService.addFriend("frienduser", testUser);
        
        mockMvc.perform(delete("/api/friends/{friendUserId}", friendUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend removed successfully"));
    }
    
    @Test
    void testRemoveFriendNotFound() throws Exception {
        mockMvc.perform(delete("/api/friends/{friendUserId}", friendUser.getUserId()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetFriendsList() throws Exception {
        friendService.addFriend("frienduser", testUser);
        
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.totalFriends").value(1))
                .andExpect(jsonPath("$.friends").isArray())
                .andExpect(jsonPath("$.friends[0].username").value("frienduser"));
    }
    
    @Test
    void testGetFriendsListEmpty() throws Exception {
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFriends").value(0))
                .andExpect(jsonPath("$.friends").isArray())
                .andExpect(jsonPath("$.friends").isEmpty());
    }
    
    @Test
    void testGetFollowers() throws Exception {
        friendService.addFriend("testuser", friendUser);
        
        mockMvc.perform(get("/api/friends/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("frienduser"));
    }
    
    @Test
    void testCheckFollowingStatus() throws Exception {
        mockMvc.perform(get("/api/friends/{friendUserId}/status", friendUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowing").value(false));
        
        friendService.addFriend("frienduser", testUser);
        
        mockMvc.perform(get("/api/friends/{friendUserId}/status", friendUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowing").value(true));
    }
    
    @Test
    void testGetFriendActivity() throws Exception {
        friendService.addFriend("frienduser", testUser);
        
        Hike hike = new Hike("Test Hike", "Location", "Easy", 5.0, 100.0, friendUser, 40.0, -120.0);
        hike = hikeRepository.save(hike);
        
        Review review = new Review(hike, friendUser, 4.5, "Great!");
        reviewRepository.save(review);
        
        mockMvc.perform(get("/api/friends/{friendUserId}/activity", friendUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(friendUser.getUserId()))
                .andExpect(jsonPath("$.username").value("frienduser"))
                .andExpect(jsonPath("$.recentActivity").isArray())
                .andExpect(jsonPath("$.recentActivity[0].type").value("review"));
    }
    
    @Test
    void testGetFriendActivityNotFollowing() throws Exception {
        mockMvc.perform(get("/api/friends/{friendUserId}/activity", friendUser.getUserId()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testAddFriendMissingUsername() throws Exception {
        AddFriendRequest request = new AddFriendRequest();
        
        mockMvc.perform(post("/api/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}