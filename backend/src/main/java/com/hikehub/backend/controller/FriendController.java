package com.hikehub.backend.controller;

import com.hikehub.backend.dto.AddFriendRequest;
import com.hikehub.backend.dto.FriendActivityDto;
import com.hikehub.backend.dto.FriendDto;
import com.hikehub.backend.dto.FriendsListResponseDto;
import com.hikehub.backend.model.User;
import com.hikehub.backend.service.CurrentUserService;
import com.hikehub.backend.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    
    private final FriendService friendService;
    private final CurrentUserService currentUserService;
    
    @Autowired
    public FriendController(FriendService friendService, CurrentUserService currentUserService) {
        this.friendService = friendService;
        this.currentUserService = currentUserService;
    }
    
    @PostMapping
    public ResponseEntity<?> addFriend(@Valid @RequestBody AddFriendRequest request) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            FriendDto response = friendService.addFriend(request.getUsername(), currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            if (e.getMessage().contains("Cannot add yourself") || 
                e.getMessage().contains("Already following")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
    
    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendUserId) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            friendService.removeFriend(friendUserId, currentUser);
            return ResponseEntity.ok(Map.of("message", "Friend removed successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Friendship not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getFriendsList() {
        try {
            User currentUser = currentUserService.getCurrentUser();
            FriendsListResponseDto response = friendService.getFriendsList(currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
    
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers() {
        try {
            User currentUser = currentUserService.getCurrentUser();
            List<FriendDto> response = friendService.getFollowers(currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
    
    @GetMapping("/{friendUserId}/activity")
    public ResponseEntity<?> getFriendActivity(@PathVariable Long friendUserId) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            FriendActivityDto response = friendService.getFriendActivity(friendUserId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("not following")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
    
    @GetMapping("/{friendUserId}/status")
    public ResponseEntity<?> checkFollowingStatus(@PathVariable Long friendUserId) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            boolean isFollowing = friendService.isFollowing(friendUserId, currentUser);
            return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Current user not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
}