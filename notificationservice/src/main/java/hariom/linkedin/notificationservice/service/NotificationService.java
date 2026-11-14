package hariom.linkedin.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    /**
     * Consume user create event.
     * Send welcome notification.
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(
            @Payload Map<String, Object> payload
    ) {
        try {
            Long userId = ((Number) payload.get("id")).longValue();
            String firstName = (String) payload.get("firstName");

            log.info("Sending welcome notification to user: {}", userId);

            sendNotification(
                    userId,
                    "Welcome to Linkedin",
                    String.format("Welcome %s. Your account has bee created.", firstName)
            );

        } catch (Exception e) {
            log.error("Error sending welcome notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection requested event.
     * Send notification to receiver
     */
    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequested(
            @Payload Map<String, Object> payload
    ) {
        try {
            Long receiverId = ((Number) payload.get("receiverId")).longValue();
            Long senderId = ((Number) payload.get("senderId")).longValue();

            log.info("Sending connection requested notification to user: {}", receiverId);

            sendNotification(
                    receiverId,
                    "New Connection Request",
                    String.format("User %s wants to connect with you.", senderId)
            );

        } catch (Exception e) {
            log.error("Error sending connection requested notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection accepted event.
     * Send notification to sender
     */
    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAccepted(
            @Payload Map<String, Object> payload
    ) {
        try {
            Long receiverId = ((Number) payload.get("receiverId")).longValue();
            Long senderId = ((Number) payload.get("senderId")).longValue();

            log.info("Sending connection accepted notification to user: {}", senderId);

            sendNotification(
                    senderId,
                    "Connection Request Accepted",
                    String.format("User %s accepted your connection request", receiverId)
            );

        } catch (Exception e) {
            log.error("Error sending connection accepted notification: {}", e.getMessage());
        }
    }

    /**
     * Consume post liked event.
     * Send notification to author
     */
    @KafkaListener(topics = "post.liked")
    public void consumePostLiked(
            @Payload Map<String, Object> payload
    ) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            Long authorId = ((Number) payload.get("authorId")).longValue();
            Long postId = ((Number) payload.get("id")).longValue();

            log.info("Sending post liked notification to user: {}", authorId);

            sendNotification(
                    authorId,
                    "Someone liked your post",
                    String.format("User %s liked your post %s", userId, postId)
            );

        } catch (Exception e) {
            log.error("Error sending post liked notification: {}", e.getMessage());
        }
    }

    /**
     * Consume post commented event.
     * Send notification to author
     */
    @KafkaListener(topics = "post.commented")
    public void consumePostCommented(
            @Payload Map<String, Object> payload
    ) {
        try {
            Long postAuthorId = ((Number) payload.get("postAuthorId")).longValue();
            Long userId = ((Number) payload.get("authorId")).longValue();
            Long postId = ((Number) payload.get("id")).longValue();

            log.info("Sending post commented notification to user: {}", postAuthorId);

            sendNotification(
                    postAuthorId,
                    "New comment on your post",
                    String.format("User %s commented on your post %s", userId, postId)
            );

        } catch (Exception e) {
            log.error("Error sending post commented notification: {}", e.getMessage());
        }
    }

    private void sendNotification(Long userId, String title, String message) {
        log.info("--------------------------------------------");

        log.info("Notification Sent");
        log.info("To user: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);

        log.info("--------------------------------------------");
    }

}
