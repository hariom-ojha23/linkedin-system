package hariom.linkedin.userservice.controller;

import hariom.linkedin.userservice.dto.UserResponse;
import hariom.linkedin.userservice.dto.UserUpdateRequest;
import hariom.linkedin.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /**
     * Get user profile
     * X-User-Id = requesting user (from gateway)
     * userId in path = target user to fetch
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestingUserId
    ) {
        log.info("Get profile: {} requested by {}", userId, requestingUserId);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Upload profile photo
     * User can only upload their own profile photo
     */
    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @RequestParam("file") MultipartFile file
    ) {
        if (!userId.equals(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.uploadProfilePhoto(userId, file));
    }

    /**
     * Update own profile
     * User can update their own profileString
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        if (!userId.equals(requestingUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                userService.updateProfile(userId, request)
        );
    }

    /**
     * Send connection request
     * Sender Id = X-User-ID (already validated)
     */
    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<String> sendConnectionRequest(
            @PathVariable Long targetUserId,
            @RequestHeader("X-User-Id") Long requestingUserId
    ) {
        return ResponseEntity.ok(
                userService.sendConnectionRequest(targetUserId, requestingUserId)
        );
    }

    /**
     * Accept connection request
     */
    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String> acceptConnection(
            @PathVariable Long connectionId,
            @RequestHeader("X-User-Id") Long requestingUserId
    ) {
        return ResponseEntity.ok(userService.acceptConnectionRequest(connectionId));
    }

    /**
     * Get all connection of a user
     */
    @GetMapping("/connections")
    public ResponseEntity<List<UserResponse>> getAllConnection(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(userService.getAllConnection(userId));
    }
}
