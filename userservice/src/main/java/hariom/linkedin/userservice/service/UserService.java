package hariom.linkedin.userservice.service;

import hariom.linkedin.userservice.dto.UserResponse;
import hariom.linkedin.userservice.dto.UserUpdateRequest;
import hariom.linkedin.userservice.entity.Connection;
import hariom.linkedin.userservice.entity.User;
import hariom.linkedin.userservice.enums.ConnectionStatus;
import hariom.linkedin.userservice.repository.ConnectionRepository;
import hariom.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    private final S3Service s3Service;

    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private static final String USER_UPDATED_TOPIC = "user.updated";

    /**
     * Send connection request.
     * Publish event to kafka to be consumed by notification service.
     */
    public String sendConnectionRequest(Long receiverId, Long senderId) {
        boolean connectionExist = connectionRepository.existsBySenderIdAndReceiverId(
                senderId,
                receiverId
        );

        if (connectionExist) {
            throw new RuntimeException("Connection request already sent");
        }

        Connection connection = Connection.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(ConnectionStatus.PENDING)
                .build();

        connectionRepository.save(connection);

        // Publish connection.requested event
        Map<String, Object> connectionRequestedEvent = new HashMap<>();
        connectionRequestedEvent.put("senderId", senderId);
        connectionRequestedEvent.put("receiverId", receiverId);

        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC, senderId, connectionRequestedEvent);

        log.info("Connection request sent: {} -> {}", senderId, receiverId);

        return "Connection request sent";
    }

    /**
     * Accept connection request.
     * Publish event to kafka to be consumed by notification service.
     */
    public String acceptConnectionRequest(Long connectionId) {
        Connection connection = connectionRepository
                .findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.ACCEPTED);

        // Publish connection.accepted event

        Map<String, Object> connectionAcceptedEvent = new HashMap<>();
        connectionAcceptedEvent.put("senderId", connection.getSenderId());
        connectionAcceptedEvent.put("receiverId", connection.getReceiverId());

        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC, connection.getSenderId(), connectionAcceptedEvent);

        log.info("Connection accepted: {}", connection.getId());

        return "Connection accepted";
    }

    /**
     * Get all connection of a user.
     * User can get only accepted connection
     */
    public List<UserResponse> getAllConnection(Long userId) {
        List<Connection> connections = connectionRepository.findBySenderIdAndStatus(
                userId,
                ConnectionStatus.ACCEPTED
        );

        return connections.stream()
                .map(c -> getUserProfile(c.getReceiverId()))
                .toList();
    }

    /**
     * Get user profile
     */
    public UserResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        return mapToUserResponse(user);
    }

    /**
     * Upload profile photo url to S3
     */
    public UserResponse uploadProfilePhoto(Long userId, MultipartFile file) {
        User user = getUserById(userId);

        String photoUrl = s3Service.uploadFile(file, "profiles/" + userId + "/avatar");
        user.setProfilePhotoUrl(photoUrl);

        User updatedUser = userRepository.save(user);

        log.info("Profile photo uploaded for user: {}", user.getId());
        return mapToUserResponse(updatedUser);
    }

    /**
     * Update own profile
     */
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        user.setHeadLine(request.getHeadLine());
        user.setAbout(request.getAbout());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());

        User updatedUser = userRepository.save(user);

        // Publish user.updated event
        // Search service consume this and indexes user

        Map<String, Object> userUpdatedEvent = new HashMap<>();

        userUpdatedEvent.put("id", updatedUser.getId());
        userUpdatedEvent.put("email", updatedUser.getEmail());
        userUpdatedEvent.put("firstName", updatedUser.getFirstName());
        userUpdatedEvent.put("lastName", updatedUser.getLastName());
        userUpdatedEvent.put("headline", updatedUser.getHeadLine());
        userUpdatedEvent.put("location", updatedUser.getLocation());
        userUpdatedEvent.put("skills", updatedUser.getSkills());

        kafkaTemplate.send(USER_UPDATED_TOPIC, updatedUser.getId(), userUpdatedEvent);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Map user entity to user response
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .headLine(user.getHeadLine())
                .about(user.getAbout())
                .location(user.getLocation())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .coverPhotoUrl(user.getCoverPhotoUrl())
                .skills(user.getSkills())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found: " + userId)
        );
    }
}
